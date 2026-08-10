package com.miniborsa.orderservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class StockClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getStocks() {
        return restTemplate.getForObject(
                "http://localhost:8080/stocks",
                String.class
        );
    }
}