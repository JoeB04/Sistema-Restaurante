package com.restaurante.controller;

import com.restaurante.dto.CambiarEstadoRequest;
import com.restaurante.dto.NuevaOrdenRequest;
import com.restaurante.dto.NuevoItemRequest;
import com.restaurante.model.*;
import com.restaurante.repository.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin(origins = "*")
public class OrdenController {

    private final OrdenRepository ordenRepository;
    private final ItemRepository itemRepository;
    private final MesaRepository mesaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlatoRepository platoRepository;

    public OrdenController(OrdenRepository ordenRepository, ItemRepository itemRepository,
                            MesaRepository mesaRepository, UsuarioRepository usuarioRepository,
                            PlatoRepository platoRepository) {
        this.ordenRepository = ordenRepository;
        this.itemRepository = itemRepository;
        this.mesaRepository = mesaRepository;
        this.usuarioRepository = usuarioRepository;
        this.platoRepository = platoRepository;
    }

    // GET /api/ordenes/{id}
    @GetMapping("/{id}")
    public Orden obtenerOrden(@PathVariable Integer id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + id));
    }

    // GET /api/ordenes/mesa/{mesaId}  -> historial de ordenes de una mesa
    @GetMapping("/mesa/{mesaId}")
    public List<Orden> ordenesDeMesa(@PathVariable Integer mesaId) {
        return ordenRepository.findByMesaId(mesaId);
    }

    // POST /api/ordenes  -> el mesero abre una orden nueva al sentar comensales
    // body: { "mesaId": 1, "meseroId": 2, "numeroComensales": 4 }
    @PostMapping
    public Orden crearOrden(@RequestBody NuevaOrdenRequest request) {
        Mesa mesa = mesaRepository.findById(request.mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada: " + request.mesaId));
        Usuario mesero = usuarioRepository.findById(request.meseroId)
                .orElseThrow(() -> new RuntimeException("Mesero no encontrado: " + request.meseroId));

        Orden orden = new Orden();
        orden.setMesa(mesa);
        orden.setMesero(mesero);
        orden.setNumeroComensales(request.numeroComensales != null ? request.numeroComensales : 1);
        orden.setEstado(Orden.EstadoOrden.ABIERTA);

        // La mesa pasa a OCUPADA en cuanto se abre una orden
        mesa.setEstado(Mesa.EstadoMesa.OCUPADA);
        mesaRepository.save(mesa);

        return ordenRepository.save(orden);
    }

    // POST /api/ordenes/{id}/items  -> agregar un plato a la orden
    // body: { "platoId": 3, "cantidad": 2, "notas": "sin ají" }
    @PostMapping("/{id}/items")
    public Orden agregarItem(@PathVariable Integer id, @RequestBody NuevoItemRequest request) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + id));
        Plato plato = platoRepository.findById(request.platoId)
                .orElseThrow(() -> new RuntimeException("Plato no encontrado: " + request.platoId));

        if (!Boolean.TRUE.equals(plato.getDisponible())) {
            throw new RuntimeException("El plato '" + plato.getNombre() + "' no está disponible");
        }

        Item item = new Item();
        item.setOrden(orden);
        item.setPlato(plato);
        item.setCantidad(request.cantidad != null ? request.cantidad : 1);
        item.setNotas(request.notas);
        item.setPrecioUnitario(plato.getPrecio()); // copiamos el precio actual
        item.setEstadoItem(Item.EstadoItem.PENDIENTE);

        orden.getItems().add(item);
        return ordenRepository.save(orden);
    }

    // PUT /api/ordenes/{id}/estado  -> body: { "estado": "ENVIADA" }
    // Se usa cuando el mesero envía la orden a cocina, o para marcar CERRADA/CANCELADA
    @PutMapping("/{id}/estado")
    public Orden cambiarEstadoOrden(@PathVariable Integer id, @RequestBody CambiarEstadoRequest request) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + id));
        Orden.EstadoOrden nuevoEstado = Orden.EstadoOrden.valueOf(request.estado);
        orden.setEstado(nuevoEstado);

        // Sincronizamos el estado visual de la mesa según el estado de la orden
        Mesa mesa = orden.getMesa();
        switch (nuevoEstado) {
            case ENVIADA, EN_PREPARACION -> mesa.setEstado(Mesa.EstadoMesa.EN_PROCESO);
            case LISTA, ENTREGADA -> mesa.setEstado(Mesa.EstadoMesa.POR_PAGAR);
            case CERRADA, CANCELADA -> mesa.setEstado(Mesa.EstadoMesa.LIBRE);
            default -> {
            }
        }
        mesaRepository.save(mesa);

        return ordenRepository.save(orden);
    }
}
