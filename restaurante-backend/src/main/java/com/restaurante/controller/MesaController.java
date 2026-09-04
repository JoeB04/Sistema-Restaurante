package com.restaurante.controller;

import com.restaurante.model.Mesa;
import com.restaurante.model.Zona;
import com.restaurante.repository.MesaRepository;
import com.restaurante.repository.ZonaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mesas")
@CrossOrigin(origins = "*") // por ahora abierto; luego lo restringimos
public class MesaController {

    private final MesaRepository mesaRepository;
    private final ZonaRepository zonaRepository;

    public MesaController(MesaRepository mesaRepository, ZonaRepository zonaRepository) {
        this.mesaRepository = mesaRepository;
        this.zonaRepository = zonaRepository;
    }

    // GET http://localhost:8080/api/mesas
    @GetMapping
    public List<Mesa> listarMesas() {
        return mesaRepository.findAll();
    }

    // GET http://localhost:8080/api/mesas/1
    @GetMapping("/{id}")
    public Mesa obtenerMesa(@PathVariable Integer id) {
        return mesaRepository.findById(id).orElse(null);
    }

    // POST /api/mesas -> Admin agrega una mesa nueva al croquis
    // body: { "numero": 6, "capacidad": 4, "zonaId": 1, "posicionX": 300, "posicionY": 200 }
    @PostMapping
    public Mesa crearMesa(@RequestBody Map<String, Object> body) {
        Integer zonaId = (Integer) body.get("zonaId");
        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada: " + zonaId));

        Mesa mesa = new Mesa();
        mesa.setZona(zona);
        mesa.setNumero((Integer) body.get("numero"));
        mesa.setCapacidad((Integer) body.get("capacidad"));
        mesa.setPosicionX(body.get("posicionX") != null ? (Integer) body.get("posicionX") : 20);
        mesa.setPosicionY(body.get("posicionY") != null ? (Integer) body.get("posicionY") : 20);
        mesa.setEstado(Mesa.EstadoMesa.LIBRE);

        return mesaRepository.save(mesa);
    }

    // PUT /api/mesas/{id} -> editar numero, capacidad y/o zona de una mesa existente
    // body: { "numero": 6, "capacidad": 4, "zonaId": 2 }
    @PutMapping("/{id}")
    public Mesa editarMesa(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada: " + id));

        mesa.setNumero((Integer) body.get("numero"));
        mesa.setCapacidad((Integer) body.get("capacidad"));

        Integer zonaId = (Integer) body.get("zonaId");
        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada: " + zonaId));
        mesa.setZona(zona);

        return mesaRepository.save(mesa);
    }

    // PUT /api/mesas/{id}/posicion -> se llama cuando el Admin arrastra una mesa en el croquis
    // body: { "posicionX": 340, "posicionY": 210 }
    @PutMapping("/{id}/posicion")
    public Mesa actualizarPosicion(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada: " + id));
        mesa.setPosicionX(body.get("posicionX"));
        mesa.setPosicionY(body.get("posicionY"));
        return mesaRepository.save(mesa);
    }

    // PUT /api/mesas/{id}/tamano -> se llama cuando el Admin redimensiona una mesa en el croquis
    // body: { "ancho": 120, "alto": 100 }
    @PutMapping("/{id}/tamano")
    public Mesa actualizarTamano(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada: " + id));
        mesa.setAncho(Math.max(50, body.get("ancho"))); // minimo 50px para que no desaparezca
        mesa.setAlto(Math.max(50, body.get("alto")));
        return mesaRepository.save(mesa);
    }

    // DELETE /api/mesas/{id}
    @DeleteMapping("/{id}")
    public void eliminarMesa(@PathVariable Integer id) {
        try {
            mesaRepository.deleteById(id);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new RuntimeException("No se puede eliminar: esta mesa tiene órdenes registradas en su historial.");
        }
    }
}