package com.miniborsa.orderservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.miniborsa.orderservice.model.Order;
import com.miniborsa.orderservice.service.OrderService;
@RestController// bu sınıfın api olduğunu söyler
public class OrderController {
	private final OrderService orderService;

	public OrderController(OrderService orderService) {
	    this.orderService = orderService;
	}

    @GetMapping("/hello")//biri GET/hello isteği atarsa bu metot çalışır
    public String hello() {//cevap olarak bunu gönderir
        return "Merhaba Mini Borsa!";
    }
    @PostMapping("/order")
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);

    }

}