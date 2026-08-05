package com.restaurante.admin.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.admin.model.BoletaDTO;
import com.restaurante.admin.model.CategoriaDTO;
import com.restaurante.admin.model.ElementoDTO;
import com.restaurante.admin.model.LoginResponseDTO;
import com.restaurante.admin.model.MesaDTO;
import com.restaurante.admin.model.OrdenDTO;
import com.restaurante.admin.model.PlatoDTO;
import com.restaurante.admin.model.ZonaDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ApiClient {

    // Cambia esto si tu backend corre en otro host/puerto
    private static final String BASE_URL = "http://localhost:8080";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            // El JSON del backend trae mas campos de los que mapeamos en los DTOs;
            // con esto Jackson simplemente los ignora en vez de tirar error.
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // ---------- Helpers genericos ----------

    private String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Error HTTP " + response.statusCode() + " en " + path + ": " + response.body());
        }
        return response.body();
    }

    private String post(String path, Object body) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Error HTTP " + response.statusCode() + " en " + path + ": " + response.body());
        }
        return response.body();
    }

    private String put(String path, Object body) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Error HTTP " + response.statusCode() + " en " + path + ": " + response.body());
        }
        return response.body();
    }

    private String delete(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .DELETE()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Error HTTP " + response.statusCode() + " en " + path + ": " + response.body());
        }
        return response.body();
    }

    private <T> List<T> parseList(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    // ---------- Autenticacion ----------

    public LoginResponseDTO login(String usuarioLogin, String password) throws Exception {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("usuarioLogin", usuarioLogin);
        body.put("password", password);
        String json = post("/api/auth/login", body);
        return objectMapper.readValue(json, LoginResponseDTO.class);
    }

    // ---------- Mesas ----------

    public List<MesaDTO> obtenerMesas() throws Exception {
        return parseList(get("/api/mesas"), MesaDTO.class);
    }

    public void crearMesa(int numero, int capacidad, int zonaId, int posicionX, int posicionY) throws Exception {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("numero", numero);
        body.put("capacidad", capacidad);
        body.put("zonaId", zonaId);
        body.put("posicionX", posicionX);
        body.put("posicionY", posicionY);
        post("/api/mesas", body);
    }

    public void moverMesa(Integer mesaId, int posicionX, int posicionY) throws Exception {
        java.util.Map<String, Integer> body = new java.util.HashMap<>();
        body.put("posicionX", posicionX);
        body.put("posicionY", posicionY);
        put("/api/mesas/" + mesaId + "/posicion", body);
    }

    public void cambiarTamanoMesa(Integer mesaId, int ancho, int alto) throws Exception {
        java.util.Map<String, Integer> body = new java.util.HashMap<>();
        body.put("ancho", ancho);
        body.put("alto", alto);
        put("/api/mesas/" + mesaId + "/tamano", body);
    }

    public void editarMesa(Integer mesaId, int numero, int capacidad, int zonaId) throws Exception {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("numero", numero);
        body.put("capacidad", capacidad);
        body.put("zonaId", zonaId);
        put("/api/mesas/" + mesaId, body);
    }

    public void eliminarMesa(Integer mesaId) throws Exception {
        delete("/api/mesas/" + mesaId);
    }

    // ---------- Zonas ----------

    public List<ZonaDTO> obtenerZonas() throws Exception {
        return parseList(get("/api/zonas"), ZonaDTO.class);
    }

    public void actualizarGeometriaZona(Integer zonaId, int posicionX, int posicionY, int ancho, int alto) throws Exception {
        java.util.Map<String, Integer> body = new java.util.HashMap<>();
        body.put("posicionX", posicionX);
        body.put("posicionY", posicionY);
        body.put("ancho", ancho);
        body.put("alto", alto);
        put("/api/zonas/" + zonaId + "/geometria", body);
    }

    // ---------- Elementos decorativos (Entrada, Cocina, Barra, etc.) ----------

    public List<ElementoDTO> obtenerElementos() throws Exception {
        return parseList(get("/api/elementos"), ElementoDTO.class);
    }

    public void crearElemento(String nombre, String icono, int posicionX, int posicionY) throws Exception {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("nombre", nombre);
        body.put("icono", icono);
        body.put("posicionX", posicionX);
        body.put("posicionY", posicionY);
        body.put("ancho", 90);
        body.put("alto", 50);
        post("/api/elementos", body);
    }

    public void editarElemento(Integer elementoId, String nombre, String icono) throws Exception {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("nombre", nombre);
        body.put("icono", icono);
        put("/api/elementos/" + elementoId, body);
    }

    public void actualizarGeometriaElemento(Integer elementoId, int posicionX, int posicionY, int ancho, int alto) throws Exception {
        java.util.Map<String, Integer> body = new java.util.HashMap<>();
        body.put("posicionX", posicionX);
        body.put("posicionY", posicionY);
        body.put("ancho", ancho);
        body.put("alto", alto);
        put("/api/elementos/" + elementoId + "/geometria", body);
    }

    public void eliminarElemento(Integer elementoId) throws Exception {
        delete("/api/elementos/" + elementoId);
    }

    // ---------- Ordenes ----------

    public List<OrdenDTO> obtenerOrdenesDeMesa(Integer mesaId) throws Exception {
        return parseList(get("/api/ordenes/mesa/" + mesaId), OrdenDTO.class);
    }

    // ---------- Categorias ----------

    public List<CategoriaDTO> obtenerCategorias() throws Exception {
        return parseList(get("/api/categorias"), CategoriaDTO.class);
    }

    public CategoriaDTO crearCategoria(String nombre) throws Exception {
        CategoriaDTO nueva = new CategoriaDTO();
        nueva.nombre = nombre;
        String json = post("/api/categorias", nueva);
        return objectMapper.readValue(json, CategoriaDTO.class);
    }

    // ---------- Platos ----------

    public List<PlatoDTO> obtenerPlatos() throws Exception {
        return parseList(get("/api/platos"), PlatoDTO.class);
    }

    public PlatoDTO crearPlato(String nombre, String descripcion, double precio, Integer categoriaId) throws Exception {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("nombre", nombre);
        body.put("descripcion", descripcion);
        body.put("precio", precio);
        java.util.Map<String, Object> categoria = new java.util.HashMap<>();
        categoria.put("id", categoriaId);
        body.put("categoria", categoria);

        String json = post("/api/platos", body);
        return objectMapper.readValue(json, PlatoDTO.class);
    }

    public void editarPlato(Integer platoId, String nombre, String descripcion, double precio, Integer categoriaId) throws Exception {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("nombre", nombre);
        body.put("descripcion", descripcion);
        body.put("precio", precio);
        java.util.Map<String, Object> categoria = new java.util.HashMap<>();
        categoria.put("id", categoriaId);
        body.put("categoria", categoria);
        put("/api/platos/" + platoId, body);
    }

    public void cambiarDisponibilidad(Integer platoId, boolean disponible) throws Exception {
        java.util.Map<String, Boolean> body = new java.util.HashMap<>();
        body.put("disponible", disponible);
        put("/api/platos/" + platoId + "/disponibilidad", body);
    }

    public void eliminarPlato(Integer platoId) throws Exception {
        delete("/api/platos/" + platoId);
    }

    // ---------- Boletas (reportes) ----------

    public List<BoletaDTO> obtenerBoletas() throws Exception {
        return parseList(get("/api/boletas"), BoletaDTO.class);
    }
}