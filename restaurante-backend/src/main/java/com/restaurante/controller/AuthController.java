package com.restaurante.controller;

import com.restaurante.dto.LoginRequest;
import com.restaurante.model.Usuario;
import com.restaurante.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // POST /api/auth/login   body: { "usuarioLogin": "admin", "password": "admin123" }
    // Nota: esto es un login simple (sin tokens JWT todavia) pensado para que
    // cada interfaz (Admin/Mesero/Cocina) sepa quien entro y con que rol.
    @PostMapping("/login")
    public ResponseEntityWrapper login(@RequestBody LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsuarioLogin(request.usuarioLogin);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntityWrapper.error("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return ResponseEntityWrapper.error("Usuario inactivo");
        }

        boolean passwordCorrecta = passwordEncoder.matches(request.password, usuario.getPasswordHash());
        if (!passwordCorrecta) {
            return ResponseEntityWrapper.error("Contraseña incorrecta");
        }

        return ResponseEntityWrapper.success(usuario);
    }

    // Envoltorio simple para responder exito/error de forma consistente
    public static class ResponseEntityWrapper {
        public boolean exito;
        public String mensaje;
        public Usuario usuario;

        public static ResponseEntityWrapper success(Usuario usuario) {
            ResponseEntityWrapper r = new ResponseEntityWrapper();
            r.exito = true;
            r.usuario = usuario;
            return r;
        }

        public static ResponseEntityWrapper error(String mensaje) {
            ResponseEntityWrapper r = new ResponseEntityWrapper();
            r.exito = false;
            r.mensaje = mensaje;
            return r;
        }
    }
}