package com.restaurante.controller;

import com.restaurante.model.Zona;
import com.restaurante.repository.ZonaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zonas")
@CrossOrigin(origins = "*")
public class ZonaController {

    private final ZonaRepository zonaRepository;

    public ZonaController(ZonaRepository zonaRepository) {
        this.zonaRepository = zonaRepository;
    }

    @GetMapping
    public List<Zona> listarZonas() {
        return zonaRepository.findAll();
    }

    @PostMapping
    public Zona crearZona(@RequestBody Zona zona) {
        return zonaRepository.save(zona);
    }

    // PUT /api/zonas/{id}/geometria -> se llama al mover o redimensionar una zona en el croquis
    // body: { "posicionX": 20, "posicionY": 20, "ancho": 380, "alto": 180 }
    @PutMapping("/{id}/geometria")
    public Zona actualizarGeometria(@PathVariable Integer id, @RequestBody java.util.Map<String, Integer> body) {
        Zona zona = zonaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada: " + id));
        zona.setPosicionX(body.get("posicionX"));
        zona.setPosicionY(body.get("posicionY"));
        zona.setAncho(Math.max(100, body.get("ancho")));  // minimo 100px de ancho
        zona.setAlto(Math.max(80, body.get("alto")));      // minimo 80px de alto
        return zonaRepository.save(zona);
    }
}