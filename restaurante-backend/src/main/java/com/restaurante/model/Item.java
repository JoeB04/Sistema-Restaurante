package com.restaurante.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "item")
public class Item {

    public enum EstadoItem {
        PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "orden_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore // evita ciclos infinitos al serializar
    private Orden orden;

    @ManyToOne
    @JoinColumn(name = "plato_id", nullable = false)
    private Plato plato;

    @Column(nullable = false)
    private Integer cantidad = 1;

    private String notas;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_item", nullable = false)
    private EstadoItem estadoItem = EstadoItem.PENDIENTE;

    public Item() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Orden getOrden() {
        return orden;
    }

    public void setOrden(Orden orden) {
        this.orden = orden;
    }

    public Plato getPlato() {
        return plato;
    }

    public void setPlato(Plato plato) {
        this.plato = plato;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public EstadoItem getEstadoItem() {
        return estadoItem;
    }

    public void setEstadoItem(EstadoItem estadoItem) {
        this.estadoItem = estadoItem;
    }
}
