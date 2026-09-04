package com.restaurante.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mesa")
public class Mesa {

    public enum EstadoMesa {
        LIBRE, OCUPADA, EN_PROCESO, POR_PAGAR, RESERVADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(name = "posicion_x", nullable = false)
    private Integer posicionX;

    @Column(name = "posicion_y", nullable = false)
    private Integer posicionY;

    @Column(nullable = false)
    private Integer ancho = 90;

    @Column(nullable = false)
    private Integer alto = 90;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMesa estado = EstadoMesa.LIBRE;

    public Mesa() {
    }

    // Getters y setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public Integer getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(Integer posicionX) {
        this.posicionX = posicionX;
    }

    public Integer getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(Integer posicionY) {
        this.posicionY = posicionY;
    }

    public Integer getAncho() {
        return ancho;
    }

    public void setAncho(Integer ancho) {
        this.ancho = ancho;
    }

    public Integer getAlto() {
        return alto;
    }

    public void setAlto(Integer alto) {
        this.alto = alto;
    }

    public EstadoMesa getEstado() {
        return estado;
    }

    public void setEstado(EstadoMesa estado) {
        this.estado = estado;
    }
}