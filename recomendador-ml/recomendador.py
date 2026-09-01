"""
Motor de recomendacion: filtrado colaborativo basado en usuarios
(user-based collaborative filtering).

Idea central:
1. Armamos una matriz cliente x plato, donde cada celda es cuantas veces
   ese cliente pidio ese plato (feedback IMPLICITO, no un rating explicito).
2. Calculamos que tan parecidos son dos clientes comparando sus filas
   en esa matriz (similitud coseno).
3. Para un cliente dado, buscamos sus clientes mas parecidos, y le
   recomendamos platos que ellos pidieron pero el todavia no.

Si el cliente no tiene suficiente historial (o es nuevo), no hay con que
comparar -> usamos un fallback simple: los platos mas populares en general.
Este es el problema clasico de "cold start" en sistemas de recomendacion.
"""

import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity

from db import obtener_engine

MINIMO_CLIENTES_PARA_SIMILITUD = 2  # con menos de esto, no tiene sentido comparar


def _obtener_matriz_cliente_plato():
    """
    Devuelve un DataFrame con:
      - filas: cliente_id
      - columnas: plato_id
      - valores: cantidad total pedida (suma de todas sus ordenes)
    Solo se cuentan ordenes con cliente identificado, e items no cancelados.
    """
    query = """
        SELECT o.cliente_id, i.plato_id, SUM(i.cantidad) AS total
        FROM item i
        JOIN orden o ON i.orden_id = o.id
        WHERE o.cliente_id IS NOT NULL
          AND i.estado_item != 'CANCELADO'
        GROUP BY o.cliente_id, i.plato_id
    """
    df = pd.read_sql(query, obtener_engine())

    if df.empty:
        return pd.DataFrame()

    matriz = df.pivot_table(
        index="cliente_id", columns="plato_id", values="total", fill_value=0
    )
    return matriz


def _obtener_platos_populares(top_n, excluir_platos_ids=None):
    """Fallback: los platos mas pedidos en todo el restaurante (cold start)."""
    query = """
        SELECT i.plato_id, p.nombre, SUM(i.cantidad) AS total_pedidos
        FROM item i
        JOIN plato p ON i.plato_id = p.id
        WHERE i.estado_item != 'CANCELADO'
        GROUP BY i.plato_id, p.nombre
        ORDER BY total_pedidos DESC
    """
    df = pd.read_sql(query, obtener_engine())

    if excluir_platos_ids:
        df = df[~df["plato_id"].isin(excluir_platos_ids)]

    top = df.head(top_n)
    return [
        {"platoId": int(row.plato_id), "nombre": row.nombre, "score": None}
        for row in top.itertuples()
    ]


def _obtener_nombres_platos(plato_ids):
    """Trae los nombres de una lista de platos (para armar la respuesta final)."""
    if not plato_ids:
        return {}
    ids_texto = ",".join(str(int(pid)) for pid in plato_ids)
    query = f"SELECT id, nombre FROM plato WHERE id IN ({ids_texto})"
    df = pd.read_sql(query, obtener_engine())
    return dict(zip(df["id"], df["nombre"]))


def recomendar_para_cliente(cliente_id, top_n=5, cantidad_similares=3):
    """
    Punto de entrada principal. Devuelve una lista de platos recomendados
    para un cliente, junto con el metodo usado ("colaborativo" o "popularidad").
    """
    matriz = _obtener_matriz_cliente_plato()

    # Caso 1: no hay suficientes datos en todo el sistema todavia
    if matriz.empty or len(matriz) < MINIMO_CLIENTES_PARA_SIMILITUD:
        return {
            "metodo": "popularidad",
            "motivo": "No hay suficiente historial de clientes todavia para comparar gustos.",
            "recomendaciones": _obtener_platos_populares(top_n),
        }

    # Caso 2: el cliente es nuevo / no tiene pedidos registrados (cold start individual)
    if cliente_id not in matriz.index:
        return {
            "metodo": "popularidad",
            "motivo": "Este cliente todavia no tiene historial de pedidos.",
            "recomendaciones": _obtener_platos_populares(top_n),
        }

    # ---------- Caso normal: calculamos similitud coseno ----------
    similitudes = cosine_similarity(matriz)
    similitudes_df = pd.DataFrame(similitudes, index=matriz.index, columns=matriz.index)

    # Similitud del cliente objetivo con todos los demas (menos el mismo)
    puntajes_similitud = similitudes_df[cliente_id].drop(cliente_id)
    similares = puntajes_similitud.sort_values(ascending=False).head(cantidad_similares)

    # Si ni el cliente mas parecido tiene similitud (todo en 0), tambien caemos a popularidad
    if similares.empty or similares.iloc[0] <= 0:
        return {
            "metodo": "popularidad",
            "motivo": "No se encontraron clientes con gustos parecidos todavia.",
            "recomendaciones": _obtener_platos_populares(top_n),
        }

    # Platos que el cliente YA pidio (para no recomendarle lo mismo)
    platos_ya_pedidos = set(matriz.loc[cliente_id][matriz.loc[cliente_id] > 0].index)

    # Puntaje ponderado: para cada plato, sumamos (similitud * cuanto lo pidio ese similar)
    puntaje_platos = pd.Series(dtype=float)
    for otro_cliente_id, similitud in similares.items():
        preferencias_otro = matriz.loc[otro_cliente_id]
        puntaje_platos = puntaje_platos.add(preferencias_otro * similitud, fill_value=0)

    # Quitamos los que ya probo, y ordenamos de mayor a menor puntaje
    puntaje_platos = puntaje_platos.drop(labels=platos_ya_pedidos, errors="ignore")
    puntaje_platos = puntaje_platos[puntaje_platos > 0].sort_values(ascending=False)

    if puntaje_platos.empty:
        return {
            "metodo": "popularidad",
            "motivo": "Los clientes parecidos no tienen platos nuevos que recomendar.",
            "recomendaciones": _obtener_platos_populares(top_n, excluir_platos_ids=platos_ya_pedidos),
        }

    top_platos = puntaje_platos.head(top_n)
    nombres = _obtener_nombres_platos(top_platos.index.tolist())

    recomendaciones = [
        {
            "platoId": int(plato_id),
            "nombre": nombres.get(plato_id, f"Plato #{plato_id}"),
            "score": round(float(score), 3),
        }
        for plato_id, score in top_platos.items()
    ]

    return {
        "metodo": "colaborativo",
        "motivo": f"Basado en {len(similares)} cliente(s) con gustos parecidos.",
        "recomendaciones": recomendaciones,
    }
