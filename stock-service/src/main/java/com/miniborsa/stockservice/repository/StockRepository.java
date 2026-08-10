package com.miniborsa.stockservice.repository;

import com.miniborsa.stockservice.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
}