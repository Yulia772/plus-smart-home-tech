package ru.yandex.practicum.shoppingstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.interactionapi.store.ProductCategory;
import ru.yandex.practicum.shoppingstore.model.Product;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByProductCategory(
            ProductCategory productCategory,
            Pageable pageable
    );

    List<Product> findAllByProductIdIn(Set<UUID> productIds);
}
