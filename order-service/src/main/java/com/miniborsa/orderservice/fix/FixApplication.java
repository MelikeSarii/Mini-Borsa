package com.miniborsa.orderservice.fix;

import org.springframework.stereotype.Component;
import quickfix.Application;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix44.NewOrderSingle;
import java.util.UUID;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.OrderCancelReplaceRequest;
@Component
public class FixApplication implements Application {

    private final SessionID sessionId =
            new SessionID(
                    "FIX.4.4",
                    "ORDER_SERVICE",
                    "MATCHING_ENGINE"
            );

    @Override
    public void onCreate(SessionID sessionId) {

        System.out.println("FIX Session oluşturuldu: " + sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {

        System.out.println("Matching Engine'e FIX bağlantısı kuruldu!");
    }

    @Override
    public void onLogout(SessionID sessionId) {
        System.out.println("FIX bağlantısı kapandı.");
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        // Login gibi yönetim mesajları
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) {
        // Karşı taraftan gelen yönetim mesajları
    }

    @Override
    public void toApp(Message message, SessionID sessionId) {
        // Bizim Matching Engine'e gönderdiğimiz gerçek emirler
        System.out.println("FIX mesajı gönderildi: " + message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) {
        // Matching Engine'den gelen uygulama mesajları
        System.out.println("FIX mesajı alındı: " + message);
    }


    // Yeni emri FIX mesajına çevirip Matching Engine'e gönderir
    public void sendNewOrder(
            String orderId,
            String symbol,
            int qty,
            String side,
            String customerName,
            Double price) throws Exception {

        // FIX 4.4 New Order Single mesajı
        NewOrderSingle order = new NewOrderSingle();

        // Her emir için benzersiz ID
        order.set(new ClOrdID(orderId));

        // Hisse kodu
        order.set(new Symbol(symbol));

        // BUY veya SELL
        if (side.equalsIgnoreCase("BUY")) {
            order.set(new Side(Side.BUY));
        } else {
            order.set(new Side(Side.SELL));
        }

        // Miktar
        order.set(new OrderQty(qty));

        // Emir işlem şekli
        order.set(new HandlInst('1'));        // Emir türü
        if (price == null) {

            // Fiyat yoksa MARKET
            order.set(new OrdType(OrdType.MARKET));

        } else {

            // Fiyat varsa LIMIT
            order.set(new OrdType(OrdType.LIMIT));
            order.set(new Price(price));
        }

        // Emir zamanı
        order.set(new TransactTime());

        // Müşteri bilgisini Text alanında taşıyoruz
        order.set(new Text(customerName));

        // Matching Engine'e gönder
        Session.sendToTarget(order, sessionId);

        System.out.println("New Order FIX olarak gönderildi: " + order);
    }

    public void sendCancelOrder(
            String originalOrderId,
            String symbol,
            String side) throws Exception
    {
        OrderCancelRequest cancel =new OrderCancelRequest();
        //cancel isteğinin kendi benzersiz idsi
        cancel.set(new ClOrdID(UUID.randomUUID().toString()));
        //iptal edilmek istenen eski emrin idsi
        cancel.set(new OrigClOrdID(originalOrderId));
        //hisse
        cancel.set(new Symbol(symbol));

        if(side.equalsIgnoreCase("BUY"))
        {
            cancel.set(new Side(Side.BUY));
        }
        else
        {
            cancel.set(new Side(Side.SELL));
        }
        cancel.set(new TransactTime());

        Session.sendToTarget(cancel,sessionId);
        System.out.println(
                "Cancel Order FIX olarak gönderildi: "
                        + originalOrderId
        );
    }
    // Replace isteğini FIX mesajına çevirip Matching Engine'e gönderir
    public void sendReplaceOrder(
            String originalOrderId,
            String symbol,
            String side,
            int newQty,
            Double newPrice) throws Exception {

        // FIX 4.4 Order Cancel/Replace Request
        OrderCancelReplaceRequest replace =
                new OrderCancelReplaceRequest();

        // Replace isteğinin kendi benzersiz ID'si
        replace.set(new ClOrdID(UUID.randomUUID().toString()));

        // Değiştirilecek eski emrin ID'si
        replace.set(new OrigClOrdID(originalOrderId));

        // Hisse
        replace.set(new Symbol(symbol));

        // BUY / SELL
        if (side.equalsIgnoreCase("BUY")) {
            replace.set(new Side(Side.BUY));
        } else {
            replace.set(new Side(Side.SELL));
        }

        // Yeni miktar
        replace.set(new OrderQty(newQty));

        // Yeni fiyat
        if (newPrice != null) {
            replace.set(new Price(newPrice));
            replace.set(new OrdType(OrdType.LIMIT));
        } else {
            replace.set(new OrdType(OrdType.MARKET));
        }

        // Emir zamanı
        replace.set(new TransactTime());

        // Matching Engine'e gönder
        Session.sendToTarget(replace, sessionId);

        System.out.println(
                "Replace Order FIX olarak gönderildi: "
                        + originalOrderId
                        + " | New Qty: " + newQty
                        + " | New Price: " + newPrice
        );
    }

}