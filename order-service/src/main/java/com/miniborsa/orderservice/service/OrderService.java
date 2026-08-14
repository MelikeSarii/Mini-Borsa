package com.miniborsa.orderservice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import java.util.UUID;
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
		String orderId = UUID.randomUUID().toString();
		order.setOrderId(orderId);
		fixApplication.sendNewOrder(
				order.getOrderId(),
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

	// Emri FIX üzerinden replace eder
	public Order replaceOrder(
			Long id,
			int newQty,
			Double newPrice) throws Exception {

		Order order = orderRepository.findById(id).orElse(null);

		if (order == null) {
			return null;
		}

		// Matching Engine'e Replace isteği gönder
		fixApplication.sendReplaceOrder(
				order.getOrderId(),
				order.getSymbol(),
				order.getSide(),
				newQty,
				newPrice
		);

		// Kendi veritabanımızdaki emri de güncelle
		order.setQty(newQty);
		order.setPrice(newPrice);

		return orderRepository.save(order);
	}

	// Emri sil
	public void deleteOrder(Long id) throws Exception {

		Order order = orderRepository.findById(id).orElse(null);

		if (order == null) {
			return;
		}

		// Eski kayıtlarda FIX orderId yoksa
		// sadece H2 kaydını sil
		if (order.getOrderId() == null || order.getOrderId().isBlank()) {

			orderRepository.deleteById(id);
			return;
		}

		// Yeni kayıtlarda FIX üzerinden Cancel gönder
		fixApplication.sendCancelOrder(
				order.getOrderId(),
				order.getSymbol(),
				order.getSide()
		);

		orderRepository.deleteById(id);
	}
}