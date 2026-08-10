package com.miniborsa.orderservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.miniborsa.orderservice.fix.FixApplication;
import com.miniborsa.orderservice.model.Order;
import com.miniborsa.orderservice.repository.OrderRepository;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final FixApplication fixApplication;

	public OrderService(OrderRepository orderRepository,
	                    FixApplication fixApplication) {
		this.orderRepository = orderRepository;
		this.fixApplication = fixApplication;
	}

	// Yeni emir geldiğinde FIX üzerinden Matching Engine'e gönderiyoruz
	public Order createOrder(Order order) throws Exception {

		fixApplication.sendNewOrder(
				order.getSymbol(),
				order.getQty(),
				order.getSide(),
				order.getCustomerName(),
				order.getPrice()
		);

		// Emri kendi veritabanımıza da kaydediyoruz
		orderRepository.save(order);

		System.out.println("Yeni emir geldi: " + order.getSymbol());

		return order;
	}

	// Tüm emirleri getir
	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}

	// ID'ye göre emir getir
	public Order getOrderById(Long id) {
		return orderRepository.findById(id).orElse(null);
	}

	// Emri güncelle
	public Order updateOrder(Long id, Order newOrder) {

		Order order = orderRepository.findById(id).orElse(null);

		if (order != null) {
			order.setCustomerName(newOrder.getCustomerName());
			order.setSymbol(newOrder.getSymbol());
			order.setQty(newOrder.getQty());
			order.setPrice(newOrder.getPrice());
			order.setSide(newOrder.getSide());

			return orderRepository.save(order);
		}

		return null;
	}

	// Emri sil
	public void deleteOrder(Long id) {
		orderRepository.deleteById(id);
	}
}