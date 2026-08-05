package com.restaurante.admin.model;

public class CategoriaDTO {
    public Integer id;
    public String nombre;
    public Integer ordenVisual;

    @Override
    public String toString() {
        // Para que se vea bien dentro de un ComboBox
        return nombre;
    }
}
