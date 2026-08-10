package com.miniborsa.orderservice.fix;

import org.springframework.stereotype.Component;
import quickfix.Application;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix44.NewOrderSingle;
import java.util.UUID;
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
            String symbol,
            int qty,
            String side,
            String customerName,
            Double price) throws Exception {

        // FIX 4.4 New Order Single mesajı
        NewOrderSingle order = new NewOrderSingle();

        // Her emir için benzersiz ID
        order.set(new ClOrdID(UUID.randomUUID().toString()));

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
}