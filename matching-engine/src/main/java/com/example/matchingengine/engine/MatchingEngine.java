package com.example.matchingengine.engine;
import com.example.matchingengine.model.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.matchingengine.fix.ExecutionReportService;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.example.matchingengine.repository.OrderRepository;

@Service
public class MatchingEngine {

    public MatchingEngine(
            ExecutionReportService executionReportService,
            OrderRepository orderRepository) {

        this.executionReportService = executionReportService;
        this.orderRepository = orderRepository;
    }
    // İki ayrı emir kuyruğumuz var
    private final Queue<Order> buyQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Order> sellQueue = new ConcurrentLinkedQueue<>();
    private final ExecutionReportService executionReportService;
    private final OrderRepository orderRepository;
    // Gelen emri BUY veya SELL kuyruğuna koyuyoruz
    public void addOrder(Order order) {//gelen emti buy sell kuyruğuna ekler

        order.setStatus("NEW");
        orderRepository.save(order);
        if ("BUY".equalsIgnoreCase(order.getSide())) {

            buyQueue.add(order);

            System.out.println(
                    "New BUY Order Received: "
                       +order.getOrderId()
                            +"|"
                            + order.getSymbol()
            );

        } else if ("SELL".equalsIgnoreCase(order.getSide())) {

            sellQueue.add(order);

            System.out.println(
                    "New SELL Order Received: "
                            +order.getOrderId()
                            +"|"
                            + order.getSymbol()
            );

        } else {

            System.out.println("Invalid order side: "
                    + order.getSide());

            return;
        }

        order.setStatus("WAITING");
    }


    // Arka planda sürekli çalışan matching işlemi
    @Scheduled(fixedDelay = 100)
    public void processOrders() {
        matchOrders();
    }


    // BUY ve SELL emirlerini eşleştiriyoruz
    private void matchOrders() {
  //buy ve selli karşıalştırıp eşliyo
        if (buyQueue.isEmpty() || sellQueue.isEmpty()) {
            return;
        }


        // Bir BUY emri seçiyoruz
        for (Order buy : buyQueue) {

            // Uygun bir SELL arıyoruz
            for (Order sell : sellQueue) {
                // Hisseler aynı olmalı
                if (!buy.getSymbol().equalsIgnoreCase(
                        sell.getSymbol())) {

                    continue;
                }

                // Fiyatlar uygun mu?
                if (!isPriceMatch(buy, sell)) {
                    continue;
                }

                System.out.println("Match Found!");

                int matchedQty = Math.min(
                        buy.getQty(),
                        sell.getQty()
                );

                System.out.println(
                        "Symbol: " + buy.getSymbol()
                                + " | Quantity: " + matchedQty
                );


                // Gerçekleşen miktarı düşüyoruz
                buy.setQty(
                        buy.getQty() - matchedQty
                );

                sell.setQty(
                        sell.getQty() - matchedQty
                );
                try {

                    executionReportService.sendExecutionReport(
                            buy,
                            matchedQty
                    );

                    executionReportService.sendExecutionReport(
                            sell,
                            matchedQty
                    );

                } catch (Exception e) {

                    System.out.println(
                            "Execution Report gönderilemedi!"
                    );

                    e.printStackTrace();
                }

                // BUY tamamen gerçekleşti
                if (buy.getQty() == 0) {

                    buy.setStatus("FILLED");

                    buyQueue.remove(buy);

                    System.out.println(
                            "BUY Order Filled"
                    );

                } else {

                    buy.setStatus("PARTIALLY_FILLED");

                    System.out.println(
                            "BUY Order Partially Filled"
                    );

                    System.out.println(
                            "Remaining Quantity: "
                                    + buy.getQty()
                    );
                }


                // SELL tamamen gerçekleşti
                if (sell.getQty() == 0) {

                    sell.setStatus("FILLED");

                    sellQueue.remove(sell);

                    System.out.println(
                            "SELL Order Filled"
                    );

                } else {

                    sell.setStatus("PARTIALLY_FILLED");

                    System.out.println(
                            "SELL Order Partially Filled"
                    );

                    System.out.println(
                            "Remaining Quantity: "
                                    + sell.getQty()
                    );
                }

                // Aynı emir başka emirle tekrar eşleşmesin
                break;
            }
        }
    }

    public Order findOrderById(String orderId)
    {
        for(Order order:buyQueue)
        {
            if(order.getOrderId().equals(orderId))
            {
                return order;
            }
        }
        for(Order order:sellQueue)
        {
            if(order.getOrderId().equals(orderId))
            {
                return order;
            }
        }
        return null;
    }

    public boolean cancelOrder(String orderId)
    {
        Order order=findOrderById(orderId);
                if(order==null)
                {
                    return false;
                }
                if("BUY".equalsIgnoreCase(order.getSide()))
                {
                    buyQueue.remove(order);
                }
                else {
                    sellQueue.remove(order);
                }
                order.setStatus("CANCELLED");
        System.out.println("Order Cancelled: " + orderId);
        return true;
    }

    public boolean replaceOrder(
            String orderId,
            int newQty,
            Double newPrice) {

        Order order = findOrderById(orderId);

        if (order == null) {
            return false;
        }

        order.setQty(newQty);
        order.setPrice(newPrice);

        order.setStatus("WAITING");

        System.out.println(
                "Order Replaced: " + orderId
                        + " | New Qty: " + newQty
                        + " | New Price: " + newPrice
        );

        return true;
    }
    // Market ve Limit emirlerinin fiyat kontrolü
    private boolean isPriceMatch(Order buy, Order sell) {

        // İkisinden biri Market Order ise fiyat kontrolü yok
        if (buy.getPrice() == null ||
                sell.getPrice() == null) {

            return true;
        }

        // BUY fiyatı SELL fiyatına eşit veya yüksek olmalı
        return buy.getPrice() >= sell.getPrice();
    }




}