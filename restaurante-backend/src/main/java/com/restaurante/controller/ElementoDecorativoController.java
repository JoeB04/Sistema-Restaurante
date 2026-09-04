package com.restaurante.controller;

import com.restaurante.model.ElementoDecorativo;
import com.restaurante.repository.ElementoDecorativoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/elementos")
@CrossOrigin(origins = "*")
public class ElementoDecorativoController {

    private final ElementoDecorativoRepository repository;

    public ElementoDecorativoController(ElementoDecorativoRepository repository) {
        this.repository = repository;
    }

    // GET /api/elementos
    @GetMapping
    public List<ElementoDecorativo> listar() {
        return repository.findAll();
    }

    // POST /api/elementos  body: { "nombre": "Barra", "icono": "🍹", "posicionX": 30, "posicionY": 30, "ancho": 90, "alto": 50 }
    @PostMapping
    public ElementoDecorativo crear(@RequestBody ElementoDecorativo elemento) {
        return repository.save(elemento);
    }

    // PUT /api/elementos/{id}  body: { "nombre": "Barra", "icono": "🍹" }
    @PutMapping("/{id}")
    public ElementoDecorativo editar(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        ElementoDecorativo elemento = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Elemento no encontrado: " + id));
        elemento.setNombre(body.get("nombre"));
        elemento.setIcono(body.get("icono"));
        return repository.save(elemento);
    }

    // PUT /api/elementos/{id}/geometria  body: { "posicionX": 30, "posicionY": 30, "ancho": 90, "alto": 50 }
    @PutMapping("/{id}/geometria")
    public ElementoDecorativo actualizarGeometria(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        ElementoDecorativo elemento = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Elemento no encontrado: " + id));
        elemento.setPosicionX(body.get("posicionX"));
        elemento.setPosicionY(body.get("posicionY"));
        elemento.setAncho(Math.max(30, body.get("ancho")));
        elemento.setAlto(Math.max(30, body.get("alto")));
        return repository.save(elemento);
    }

    // DELETE /api/elementos/{id}
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        repository.deleteById(id);
    }
}