package com.restaurante.admin.model;

import java.util.List;

public class OrdenDTO {
    public Integer id;
    public Integer numeroComensales;
    public String estado; // ABIERTA, ENVIADA, EN_PREPARACION, LISTA, ENTREGADA, CERRADA, CANCELADA
    public MesaDTO mesa;
    public List<ItemDTO> items;
}
