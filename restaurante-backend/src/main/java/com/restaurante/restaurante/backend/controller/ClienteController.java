package com.restaurante.controller;

import com.restaurante.model.Cliente;
import com.restaurante.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final RestTemplate restTemplate;

    // URL del microservicio de Python (configurable en application.properties)
    @Value("${ml.service.url}")
    private String mlServiceUrl;

    public ClienteController(ClienteRepository clienteRepository, RestTemplate restTemplate) {
        this.clienteRepository = clienteRepository;
        this.restTemplate = restTemplate;
    }

    // GET /api/clientes -> lista completa (para el Admin, si hace falta)
    @GetMapping
    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    // GET /api/clientes/buscar?nombre=carlos -> usado por el mesero al tomar la orden
    @GetMapping("/buscar")
    public List<Cliente> buscar(@RequestParam String nombre) {
        return clienteRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // POST /api/clientes  body: { "nombre": "Carlos Mendoza", "telefono": "999111222" }
    // Usado cuando el mesero atiende a un cliente nuevo que no existe todavia
    @PostMapping
    public Cliente crear(@RequestBody Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    // GET /api/clientes/{id}/recomendaciones -> le pregunta al microservicio de Python
    // y le pasa la respuesta tal cual a quien lo llamo (Mesero, Admin, etc.)
    @GetMapping("/{id}/recomendaciones")
    public Map<String, Object> obtenerRecomendaciones(@PathVariable Integer id) {
        // Verificamos que el cliente exista antes de molestar al servicio de Python
        clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + id));

        String url = mlServiceUrl + "/recomendar/" + id;
        try {
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo conectar con el servicio de recomendaciones. "
                    + "¿Está corriendo 'python app.py'? Detalle: " + ex.getMessage());
        }
    }
}