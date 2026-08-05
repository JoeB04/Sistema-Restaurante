package com.restaurante.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orden")
public class Orden {

    public enum EstadoOrden {
        ABIERTA, ENVIADA, EN_PREPARACION, LISTA, ENTREGADA, CERRADA, CANCELADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    @ManyToOne
    @JoinColumn(name = "mesero_id", nullable = false)
    private Usuario mesero;

    @Column(name = "numero_comensales")
    private Integer numeroComensales = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado = EstadoOrden.ABIERTA;

    @Column(name = "fecha_apertura", updatable = false, insertable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items = new ArrayList<>();

    public Orden() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public Usuario getMesero() {
        return mesero;
    }

    public void setMesero(Usuario mesero) {
        this.mesero = mesero;
    }

    public Integer getNumeroComensales() {
        return numeroComensales;
    }

    public void setNumeroComensales(Integer numeroComensales) {
        this.numeroComensales = numeroComensales;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
