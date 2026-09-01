"""
Conexion a la misma base de datos MySQL que usa el backend Spring Boot.
El microservicio de Python SOLO LEE datos (nunca escribe), asi que no hay
riesgo de que interfiera con la logica de negocio que ya vive en Java.
"""

from sqlalchemy import create_engine

# Mismos datos que en application.properties del backend
DB_HOST = "localhost"
DB_PORT = 3306
DB_NAME = "restaurante_db"
DB_USER = "root"
DB_PASSWORD = ""  # igual que en XAMPP, vacio por defecto

_engine = None


def obtener_engine():
    """Crea (una sola vez) y reutiliza la conexion a la base de datos."""
    global _engine
    if _engine is None:
        url = f"mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
        _engine = create_engine(url)
    return _engine
