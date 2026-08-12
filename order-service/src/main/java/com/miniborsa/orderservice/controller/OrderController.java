package com.miniborsa.orderservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.miniborsa.orderservice.model.Order;
import com.miniborsa.orderservice.service.OrderService;
import com.miniborsa.orderservice.service.StockClient;

@RestController
public class OrderController {

    private final OrderService orderService;
    private final StockClient stockClient;

    public OrderController(OrderService orderService, StockClient stockClient) {
        this.orderService = orderService;
        this.stockClient = stockClient;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Merhaba Mini Borsa!";
    }

    @PostMapping("/order")
    public Order createOrder(@RequestBody Order order) throws Exception {
        return orderService.createOrder(order);
    }

    @GetMapping("/order")
    public List<Order> getOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/order/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @DeleteMapping("/order/{id}")
    public void deleteOrder(@PathVariable Long id)throws Exception {
        orderService.deleteOrder(id);
    }

    @PutMapping("/order/{id}")
    public Order replaceOrder(
            @PathVariable Long id,
            @RequestBody Order newOrder) throws Exception {

        return orderService.replaceOrder(
                id,
                newOrder.getQty(),
                newOrder.getPrice()
        );
    }

    @GetMapping("/stocks")
    public String getStocksFromStockService() {
        return stockClient.getStocks();
    }
}