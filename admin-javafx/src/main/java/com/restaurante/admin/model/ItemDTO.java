package com.restaurante.admin.model;

public class ItemDTO {
    public Integer id;
    public Integer cantidad;
    public String notas;
    public Double precioUnitario;
    public String estadoItem; // PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO
    public PlatoDTO plato;
}
