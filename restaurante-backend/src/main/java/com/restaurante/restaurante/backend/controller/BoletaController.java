package com.restaurante.controller;

import com.restaurante.dto.GenerarBoletaRequest;
import com.restaurante.model.*;
import com.restaurante.repository.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class BoletaController {

    private final BoletaRepository boletaRepository;
    private final OrdenRepository ordenRepository;
    private final MesaRepository mesaRepository;

    public BoletaController(BoletaRepository boletaRepository, OrdenRepository ordenRepository,
                             MesaRepository mesaRepository) {
        this.boletaRepository = boletaRepository;
        this.ordenRepository = ordenRepository;
        this.mesaRepository = mesaRepository;
    }

    // GET /api/boletas
    @GetMapping("/api/boletas")
    public List<Boleta> listarBoletas() {
        return boletaRepository.findAll();
    }

    // POST /api/ordenes/{ordenId}/boleta -> genera la boleta y cierra la orden
    // body: { "metodoPago": "EFECTIVO" }
    @PostMapping("/api/ordenes/{ordenId}/boleta")
    public Boleta generarBoleta(@PathVariable Integer ordenId, @RequestBody GenerarBoletaRequest request) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + ordenId));

        BigDecimal subtotal = orden.getItems().stream()
                .filter(i -> i.getEstadoItem() != Item.EstadoItem.CANCELADO)
                .map(i -> i.getPrecioUnitario().multiply(BigDecimal.valueOf(i.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Boleta boleta = new Boleta();
        boleta.setOrden(orden);
        boleta.setSubtotal(subtotal);
        boleta.setTotal(subtotal); // aquí se sumarían impuestos/propina si aplica
        boleta.setMetodoPago(Boleta.MetodoPago.valueOf(request.metodoPago));

        orden.setEstado(Orden.EstadoOrden.CERRADA);
        orden.setFechaCierre(LocalDateTime.now());
        ordenRepository.save(orden);

        Mesa mesa = orden.getMesa();
        mesa.setEstado(Mesa.EstadoMesa.LIBRE);
        mesaRepository.save(mesa);

        return boletaRepository.save(boleta);
    }
}
