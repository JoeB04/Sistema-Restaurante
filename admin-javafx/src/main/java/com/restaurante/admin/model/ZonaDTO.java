package com.restaurante.admin.model;

public class ZonaDTO {
    public Integer id;
    public String nombre;
    public Integer posicionX;
    public Integer posicionY;
    public Integer ancho;
    public Integer alto;

    @Override
    public String toString() {
        // Para que se vea bien dentro de un ComboBox
        return nombre;
    }
}