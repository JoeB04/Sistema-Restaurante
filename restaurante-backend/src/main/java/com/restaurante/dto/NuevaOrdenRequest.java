package com.restaurante.dto;

// Datos que envia el mesero para abrir una orden nueva en una mesa
public class NuevaOrdenRequest {
    public Integer mesaId;
    public Integer meseroId;
    public Integer numeroComensales;
    public Integer clienteId; // opcional: null si el cliente no se identifica
}