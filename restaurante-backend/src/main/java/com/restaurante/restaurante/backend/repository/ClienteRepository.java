package com.restaurante.repository;

import com.restaurante.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    // Busqueda simple por nombre, usada cuando el mesero busca un cliente existente
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
}
