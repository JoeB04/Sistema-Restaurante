package com.restaurante.repository;

import com.restaurante.model.Zona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZonaRepository extends JpaRepository<Zona, Integer> {
}