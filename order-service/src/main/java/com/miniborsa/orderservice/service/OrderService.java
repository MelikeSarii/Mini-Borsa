package com.miniborsa.orderservice.service;

import org.springframework.stereotype.Service;

import com.miniborsa.orderservice.model.Order;

@Service //springe by bi servis sınfııdır diyo 
public class OrderService {

    public Order createOrder(Order order) {

        System.out.println("Yeni emir geldi : " + order.getSymbol());

        return order;
    }

}
