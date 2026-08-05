package com.restaurante.repository;

import com.restaurante.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MesaRepository extends JpaRepository<Mesa, Integer> {
    // JpaRepository ya nos da: findAll(), findById(), save(), delete()...
    // Métodos personalizados se agregan aquí después, ej:
    // List<Mesa> findByEstado(Mesa.EstadoMesa estado);
}
