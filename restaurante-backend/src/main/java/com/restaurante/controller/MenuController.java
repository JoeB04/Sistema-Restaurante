package com.restaurante.controller;

import com.restaurante.model.Categoria;
import com.restaurante.model.Plato;
import com.restaurante.repository.CategoriaRepository;
import com.restaurante.repository.PlatoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class MenuController {

    private final CategoriaRepository categoriaRepository;
    private final PlatoRepository platoRepository;

    public MenuController(CategoriaRepository categoriaRepository, PlatoRepository platoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.platoRepository = platoRepository;
    }

    // ---------- CATEGORIAS ----------

    // GET /api/categorias
    @GetMapping("/api/categorias")
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    // POST /api/categorias  (usado por el Admin para crear el menu)
    @PostMapping("/api/categorias")
    public Categoria crearCategoria(@RequestBody Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    // ---------- PLATOS ----------

    // GET /api/platos  -> todos (para el Admin)
    @GetMapping("/api/platos")
    public List<Plato> listarPlatos() {
        return platoRepository.findAll();
    }

    // GET /api/platos/disponibles -> solo disponibles (para el Mesero al armar la orden)
    @GetMapping("/api/platos/disponibles")
    public List<Plato> listarPlatosDisponibles() {
        return platoRepository.findByDisponibleTrue();
    }

    // POST /api/platos (Admin crea un plato nuevo)
    @PostMapping("/api/platos")
    public Plato crearPlato(@RequestBody Plato plato) {
        return platoRepository.save(plato);
    }

    // PUT /api/platos/{id} (Admin edita nombre/descripcion/precio/categoria de un plato)
    @PutMapping("/api/platos/{id}")
    public Plato editarPlato(@PathVariable Integer id, @RequestBody Plato datosNuevos) {
        Plato plato = platoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plato no encontrado: " + id));

        plato.setNombre(datosNuevos.getNombre());
        plato.setDescripcion(datosNuevos.getDescripcion());
        plato.setPrecio(datosNuevos.getPrecio());
        if (datosNuevos.getCategoria() != null && datosNuevos.getCategoria().getId() != null) {
            Categoria categoria = categoriaRepository.findById(datosNuevos.getCategoria().getId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            plato.setCategoria(categoria);
        }

        return platoRepository.save(plato);
    }

    // DELETE /api/platos/{id}
    @DeleteMapping("/api/platos/{id}")
    public void eliminarPlato(@PathVariable Integer id) {
        platoRepository.deleteById(id);
    }

    // PUT /api/platos/{id}/disponibilidad  body: { "disponible": true|false }
    // Usado por Cocina para marcar un plato agotado/disponible
    @PutMapping("/api/platos/{id}/disponibilidad")
    public Plato cambiarDisponibilidad(@PathVariable Integer id, @RequestBody java.util.Map<String, Boolean> body) {
        Plato plato = platoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plato no encontrado: " + id));
        plato.setDisponible(body.get("disponible"));
        return platoRepository.save(plato);
    }
}