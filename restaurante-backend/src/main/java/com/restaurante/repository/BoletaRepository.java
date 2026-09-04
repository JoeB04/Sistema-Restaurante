package com.restaurante.repository;

import com.restaurante.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoletaRepository extends JpaRepository<Boleta, Integer> {
}
