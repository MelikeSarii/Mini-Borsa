package com.miniborsa.stockservice.controller;
import java.util.List;
import com.miniborsa.stockservice.model.Stock;
import com.miniborsa.stockservice.service.StockService;
import org.springframework.web.bind.annotation.*;

@RestController
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/stocks")
    public Stock createStock(@RequestBody Stock stock) {
        return stockService.saveStock(stock);
    }
    @GetMapping("/stocks")
    public List<Stock> getAllStocks() {
        return stockService.getAllStocks();
    }

    @PutMapping("/stocks/{id}")
    public Stock updateStock(@PathVariable Long id, @RequestBody Stock stock) {
        return stockService.updateStock(id, stock);
    }

    @DeleteMapping("/stocks/{id}")
    public void deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
    }

}