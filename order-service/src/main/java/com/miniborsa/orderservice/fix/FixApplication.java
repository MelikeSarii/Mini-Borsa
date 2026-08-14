package com.miniborsa.orderservice.fix;

import com.miniborsa.orderservice.model.Order;
import com.miniborsa.orderservice.repository.OrderRepository;

import org.springframework.stereotype.Component;

import quickfix.Application;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;

import quickfix.field.ClOrdID;
import quickfix.field.LeavesQty;
import quickfix.field.MsgType;
import quickfix.field.OrdStatus;
import quickfix.field.OrigClOrdID;
import quickfix.field.OrderQty;
import quickfix.field.OrdType;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.Text;
import quickfix.field.TransactTime;

import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.OrderCancelReplaceRequest;

import java.util.UUID;

@Component
public class FixApplication implements Application {

    private final OrderRepository orderRepository;

    private final SessionID sessionId =
            new SessionID(
                    "FIX.4.4",
                    "ORDER_SERVICE",
                    "MATCHING_ENGINE"
            );

    public FixApplication(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void onCreate(SessionID sessionId) {
        System.out.println(
                "FIX Session oluşturuldu: " + sessionId
        );
    }

    @Override
    public void onLogon(SessionID sessionId) {
        System.out.println(
                "Matching Engine'e FIX bağlantısı kuruldu!"
        );
    }

    @Override
    public void onLogout(SessionID sessionId) {
        System.out.println(
                "FIX bağlantısı kapandı."
        );
    }

    @Override
    public void toAdmin(
            Message message,
            SessionID sessionId) {

        // Login gibi yönetim mesajları
    }

    @Override
    public void fromAdmin(
            Message message,
            SessionID sessionId) {

        // Karşı taraftan gelen yönetim mesajları
    }

    @Override
    public void toApp(
            Message message,
            SessionID sessionId) {

        System.out.println(
                "FIX mesajı gönderildi: " + message
        );
    }

    @Override
    public void fromApp(
            Message message,
            SessionID sessionId) {

        System.out.println(
                "FIX mesajı alındı: " + message
        );

        try {

            String msgType =
                    message.getHeader()
                            .getString(MsgType.FIELD);

            /*
             * 35=D
             * NEW ORDER
             */
            if ("D".equals(msgType)) {

                System.out.println(
                        ">>> New Order mesajı geldi!"
                );

            }

            /*
             * 35=8
             * EXECUTION REPORT
             */
            else if ("8".equals(msgType)) {

                System.out.println(
                        ">>> Execution Report geldi!"
                );

                String orderId =
                        message.getString(ClOrdID.FIELD);

                Order order =
                        orderRepository
                                .findByOrderId(orderId)
                                .orElse(null);

                if (order == null) {

                    System.out.println(
                            ">>> Execution Report için emir bulunamadı: "
                                    + orderId
                    );

                    return;
                }

                /*
                 * Kalan miktarı al
                 */
                if (message.isSetField(LeavesQty.FIELD)) {

                    int remainingQty =
                            (int) message.getDouble(
                                    LeavesQty.FIELD
                            );

                    order.setQty(remainingQty);
                }

                /*
                 * Emir durumunu al
                 */
                if (message.isSetField(OrdStatus.FIELD)) {

                    char status =
                            message.getChar(OrdStatus.FIELD);

                    if (status == OrdStatus.FILLED) {

                        order.setStatus("FILLED");

                    } else if (
                            status ==
                                    OrdStatus.PARTIALLY_FILLED) {

                        order.setStatus(
                                "PARTIALLY_FILLED"
                        );
                    }
                }

                orderRepository.save(order);

                System.out.println(
                        ">>> Order güncellendi: "
                                + order.getOrderId()
                                + " | Qty: "
                                + order.getQty()
                                + " | Status: "
                                + order.getStatus()
                );
            }

            /*
             * Diğer FIX mesajları
             */
            else {

                System.out.println(
                        ">>> Gelen FIX mesaj tipi: "
                                + msgType
                );
            }

        } catch (FieldNotFound e) {

            System.out.println(
                    "FIX mesajında gerekli alan bulunamadı!"
            );

            e.printStackTrace();
        }
    }


    // =========================================================
    // NEW ORDER
    // =========================================================

    public void sendNewOrder(
            String orderId,
            String symbol,
            int qty,
            String side,
            String customerName,
            Double price) throws Exception {

        NewOrderSingle order =
                new NewOrderSingle();

        // Order ID
        order.set(
                new ClOrdID(orderId)
        );

        // Hisse
        order.set(
                new Symbol(symbol)
        );

        // BUY / SELL
        if (side.equalsIgnoreCase("BUY")) {

            order.set(
                    new Side(Side.BUY)
            );

        } else {

            order.set(
                    new Side(Side.SELL)
            );
        }

        // Miktar
        order.set(
                new OrderQty(qty)
        );

        // İşlem şekli
        order.set(
                new quickfix.field.HandlInst('1')
        );

        // Market / Limit
        if (price == null) {

            order.set(
                    new OrdType(OrdType.MARKET)
            );

        } else {

            order.set(
                    new OrdType(OrdType.LIMIT)
            );

            order.set(
                    new Price(price)
            );
        }

        // Zaman
        order.set(
                new TransactTime()
        );

        // Müşteri
        order.set(
                new Text(customerName)
        );

        // Gönder
        Session.sendToTarget(
                order,
                sessionId
        );

        System.out.println(
                "New Order FIX olarak gönderildi: "
                        + order
        );
    }


    // =========================================================
    // CANCEL ORDER
    // =========================================================

    public void sendCancelOrder(
            String originalOrderId,
            String symbol,
            String side) throws Exception {

        OrderCancelRequest cancel =
                new OrderCancelRequest();

        // Cancel isteğinin kendi ID'si
        cancel.set(
                new ClOrdID(
                        UUID.randomUUID()
                                .toString()
                )
        );

        // İptal edilecek eski emrin ID'si
        System.out.println(
                "ORIGINAL ORDER ID = ["
                        + originalOrderId
                        + "]"
        );

        cancel.set(
                new OrigClOrdID(
                        originalOrderId
                )
        );

        // Hisse
        cancel.set(
                new Symbol(symbol)
        );

        // BUY / SELL
        if (side.equalsIgnoreCase("BUY")) {

            cancel.set(
                    new Side(Side.BUY)
            );

        } else {

            cancel.set(
                    new Side(Side.SELL)
            );
        }

        // Zaman
        cancel.set(
                new TransactTime()
        );

        // Gönder
        Session.sendToTarget(
                cancel,
                sessionId
        );

        System.out.println(
                "Cancel Order FIX olarak gönderildi: "
                        + originalOrderId
        );
    }


    // =========================================================
    // REPLACE ORDER
    // =========================================================

    public void sendReplaceOrder(
            String originalOrderId,
            String symbol,
            String side,
            int newQty,
            Double newPrice) throws Exception {

        OrderCancelReplaceRequest replace =
                new OrderCancelReplaceRequest();

        // Replace isteğinin kendi ID'si
        replace.set(
                new ClOrdID(
                        UUID.randomUUID()
                                .toString()
                )
        );

        // Eski emrin ID'si
        replace.set(
                new OrigClOrdID(
                        originalOrderId
                )
        );

        // Hisse
        replace.set(
                new Symbol(symbol)
        );

        // BUY / SELL
        if (side.equalsIgnoreCase("BUY")) {

            replace.set(
                    new Side(Side.BUY)
            );

        } else {

            replace.set(
                    new Side(Side.SELL)
            );
        }

        // Yeni miktar
        replace.set(
                new OrderQty(newQty)
        );

        // Yeni fiyat
        if (newPrice == null) {

            replace.set(
                    new OrdType(OrdType.MARKET)
            );

        } else {

            replace.set(
                    new OrdType(OrdType.LIMIT)
            );

            replace.set(
                    new Price(newPrice)
            );
        }

        // Zaman
        replace.set(
                new TransactTime()
        );

        // Gönder
        Session.sendToTarget(
                replace,
                sessionId
        );

        System.out.println(
                "Replace Order FIX olarak gönderildi: "
                        + originalOrderId
                        + " | New Qty: "
                        + newQty
                        + " | New Price: "
                        + newPrice
        );
    }
}