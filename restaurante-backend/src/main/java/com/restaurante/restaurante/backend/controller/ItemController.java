package com.restaurante.controller;

import com.restaurante.dto.CambiarEstadoRequest;
import com.restaurante.model.Item;
import com.restaurante.repository.ItemRepository;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    private final ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    // GET /api/items/orden/{ordenId} -> lo que Cocina necesita ver de una orden
    @GetMapping("/orden/{ordenId}")
    public List<Item> itemsDeOrden(@PathVariable Integer ordenId) {
        return itemRepository.findByOrdenId(ordenId);
    }

    // GET /api/items/cola -> TODOS los platos pendientes o en preparacion, de TODAS las mesas.
    // Devolvemos campos sueltos (no las entidades completas) para evitar problemas de
    // referencias circulares y mandar solo lo que Cocina necesita ver.
    @GetMapping("/cola")
    public List<Map<String, Object>> colaDePedidos() {
        List<Item> items = itemRepository.findByEstadoItemIn(
                List.of(Item.EstadoItem.PENDIENTE, Item.EstadoItem.EN_PREPARACION));

        return items.stream().map(item -> {
            Map<String, Object> mapa = new LinkedHashMap<>();
            mapa.put("itemId", item.getId());
            mapa.put("platoNombre", item.getPlato().getNombre());
            mapa.put("cantidad", item.getCantidad());
            mapa.put("notas", item.getNotas());
            mapa.put("estadoItem", item.getEstadoItem().name());
            mapa.put("ordenId", item.getOrden().getId());
            mapa.put("mesaNumero", item.getOrden().getMesa().getNumero());
            return mapa;
        }).collect(Collectors.toList());
    }

    // PUT /api/items/{id}/estado  body: { "estado": "LISTO" }
    // Cocina usa esto para: PENDIENTE -> EN_PREPARACION -> LISTO
    // El mesero usa esto para: LISTO -> ENTREGADO
    @PutMapping("/{id}/estado")
    public Item cambiarEstadoItem(@PathVariable Integer id, @RequestBody CambiarEstadoRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item no encontrado: " + id));
        item.setEstadoItem(Item.EstadoItem.valueOf(request.estado));
        return itemRepository.save(item);
    }

    // DELETE /api/items/{id} -> el mesero quita un plato de la orden
    // Regla de negocio: solo se puede quitar si cocina AUN NO lo ha empezado a preparar
    // (si ya esta EN_PREPARACION, LISTO o ENTREGADO, ya se cocino/sirvio y no se puede deshacer)
    @DeleteMapping("/{id}")
    public void eliminarItem(@PathVariable Integer id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item no encontrado: " + id));

        if (item.getEstadoItem() != Item.EstadoItem.PENDIENTE) {
            throw new RuntimeException("No se puede quitar: cocina ya empezó a preparar este plato (estado: "
                    + item.getEstadoItem() + ").");
        }

        itemRepository.delete(item);
    }
}