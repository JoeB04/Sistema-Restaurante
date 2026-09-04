package com.restaurante.repository;

import com.restaurante.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findByOrdenId(Integer ordenId);
    List<Item> findByEstadoItemIn(List<Item.EstadoItem> estados);
}