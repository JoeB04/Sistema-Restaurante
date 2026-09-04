package com.restaurante.repository;

import com.restaurante.model.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlatoRepository extends JpaRepository<Plato, Integer> {
    List<Plato> findByDisponibleTrue();
}
