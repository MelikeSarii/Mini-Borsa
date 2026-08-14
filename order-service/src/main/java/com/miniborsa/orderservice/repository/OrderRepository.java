package com.miniborsa.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.miniborsa.orderservice.model.Order;
import java.util.Optional;
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
}