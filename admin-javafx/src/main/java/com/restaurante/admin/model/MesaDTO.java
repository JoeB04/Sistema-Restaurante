package com.restaurante.admin.model;

// Representa el JSON que devuelve GET /api/mesas
// Los nombres de campos deben coincidir con el JSON del backend (Jackson los mapea solo)
public class MesaDTO {
    public Integer id;
    public ZonaDTO zona;
    public Integer numero;
    public Integer capacidad;
    public Integer posicionX;
    public Integer posicionY;
    public Integer ancho;
    public Integer alto;
    public String estado; // LIBRE, OCUPADA, EN_PROCESO, POR_PAGAR, RESERVADA
}