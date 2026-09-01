"""
Microservicio de recomendaciones.

Corre por separado del backend Java (puerto distinto), y Spring Boot le
pregunta a este servicio cuando necesita recomendaciones para un cliente.
"""

from flask import Flask, jsonify
from flask_cors import CORS

from recomendador import recomendar_para_cliente

app = Flask(__name__)
CORS(app)  # permite que Spring Boot (u otros clientes) lo consuman sin problema


@app.route("/")
def inicio():
    return jsonify({"servicio": "recomendador-ml", "estado": "activo"})


# GET /recomendar/5?top=5
@app.route("/recomendar/<int:cliente_id>")
def recomendar(cliente_id):
    from flask import request
    top_n = request.args.get("top", default=5, type=int)

    try:
        resultado = recomendar_para_cliente(cliente_id, top_n=top_n)
        resultado["clienteId"] = cliente_id
        return jsonify(resultado)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    # Puerto 5000, distinto al 8080 del backend Java
    app.run(host="0.0.0.0", port=5000, debug=True)
