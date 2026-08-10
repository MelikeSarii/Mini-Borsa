package com.miniborsa.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.miniborsa.orderservice.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}