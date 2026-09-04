package com.restaurante.controller;

import com.restaurante.model.Usuario;
import com.restaurante.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // GET /api/usuarios
    // NOTA: esto es temporal para probar. Cuando armemos el login real,
    // esto se reemplaza por un endpoint de autenticación (POST /api/auth/login)
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}
