package ru.yandex.practicum.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.order.model.OrderEntity;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    @EntityGraph(attributePaths = {"products"})
    Page<OrderEntity> findByUsername(String username, Pageable pageable);

    @EntityGraph(attributePaths = {"products", "toAddress"})
    Optional<OrderEntity> findByOrderId(UUID orderId);
}
