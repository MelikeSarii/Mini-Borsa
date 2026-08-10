package com.miniborsa.stockservice.service;

import com.miniborsa.stockservice.model.Stock;
import com.miniborsa.stockservice.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public Stock saveStock(Stock stock) {
        return stockRepository.save(stock);
    }
    public Stock updateStock(Long id, Stock stock) {
        Stock existing = stockRepository.findById(id).orElseThrow();

        existing.setName(stock.getName());
        existing.setPrice(stock.getPrice());

        return stockRepository.save(existing);
    }

    public void deleteStock(Long id) {
        stockRepository.deleteById(id);
    }
}