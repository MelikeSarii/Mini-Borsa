package com.example.matchingengine.controller;

import com.example.matchingengine.engine.MatchingEngine;
import com.example.matchingengine.model.Order;
import org.springframework.web.bind.annotation.*;

@RestController//rest api kısmı
//http isteklerini karşılmak için kullanılır
public class MatchingController {

    private final MatchingEngine matchingEngine;

    public MatchingController(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }


    // Şimdilik Postman'dan emir gönderip motoru test ediyoruz
    @PostMapping("/orders")
    public String receiveOrder(@RequestBody Order order) {

        matchingEngine.addOrder(order);

        return "Order received";
    }
}