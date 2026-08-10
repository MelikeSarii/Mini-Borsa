package com.example.matchingengine.fix;

import com.example.matchingengine.engine.MatchingEngine;
import com.example.matchingengine.model.Order;

import org.springframework.stereotype.Component;
import quickfix.field.MsgType;
import quickfix.Application;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.ClOrdID;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.Text;
import quickfix.fix44.NewOrderSingle;

@Component
public class FixApplication implements Application {

    private final MatchingEngine matchingEngine;

    public FixApplication(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    @Override
    public void onCreate(SessionID sessionId) {
        System.out.println("FIX Session oluşturuldu: " + sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        System.out.println("FIX Session LOGON oldu: " + sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        System.out.println("FIX Session LOGOUT oldu: " + sessionId);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        System.out.println("ADMIN mesajı gönderildi: " + message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) {
        System.out.println("ADMIN mesajı geldi: " + message);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) {
        System.out.println("FIX mesajı gönderildi: " + message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) {

        System.out.println(">>> fromApp çalıştı: " + message);
        System.out.println(">>> Mesaj tipi: " + message.getClass().getName());

        try {

            // Mesaj tipinin New Order Single olup olmadığını FIX MsgType üzerinden kontrol et
            if ("D".equals(message.getHeader().getString(MsgType.FIELD))) {

                System.out.println(">>> New Order geldi!");

                String symbol =
                        message.getString(Symbol.FIELD);

                int qty =
                        (int) message.getDouble(OrderQty.FIELD);

                char sideValue =
                        message.getChar(Side.FIELD);

                String side;

                if (sideValue == Side.BUY) {
                    side = "BUY";
                } else {
                    side = "SELL";
                }

                String customerName =
                        message.getString(Text.FIELD);

                Double price = null;

                if (message.isSetField(Price.FIELD)) {
                    price = message.getDouble(Price.FIELD);
                }

                Order order = new Order();

                order.setSymbol(symbol);
                order.setQty(qty);
                order.setSide(side);
                order.setCustomerName(customerName);
                order.setPrice(price);

                System.out.println(
                        ">>> Order oluşturuldu: "
                                + symbol
                                + " | "
                                + side
                                + " | "
                                + qty
                                + " | "
                                + price
                );

                matchingEngine.addOrder(order);

            }

        } catch (FieldNotFound e) {

            System.out.println(
                    "FIX mesajında gerekli alan bulunamadı!"
            );

            e.printStackTrace();
        }
    }
}