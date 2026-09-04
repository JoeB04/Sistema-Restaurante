package com.restaurante.repository;

import com.restaurante.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Integer> {
    List<Orden> findByMesaId(Integer mesaId);
}
