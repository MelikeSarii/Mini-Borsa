package com.example.matchingengine.fix;

import com.example.matchingengine.engine.MatchingEngine;
import com.example.matchingengine.model.Order;
import quickfix.field.OrderQty;
import quickfix.field.OrdType;
import quickfix.field.TransactTime;
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
import quickfix.field.OrigClOrdID;
import quickfix.fix44.OrderCancelRequest;
@Component//fix mesajını karşılay sınıf
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
      //matching engine fix üzerinden mesaj geldiğinde burası
        System.out.println(">>> fromApp çalıştı: " + message);
        System.out.println(">>> Mesaj tipi: " + message.getClass().getName());

        try {
            String msgType = message.getHeader().getString(MsgType.FIELD);


            // Mesaj tipinin New Order Single olup olmadığını FIX MsgType üzerinden kontrol et
            if ("D".equals(msgType)) {

                System.out.println(">>> New Order geldi!");

                String orderId=message.getString((ClOrdID.FIELD));
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

                order.setOrderId((orderId));
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
            else if("F".equals(msgType))
            {
                  String originalOrderId=message.getString((OrigClOrdID.FIELD));
                  System.out.println(">>> Cancel Order geldi: " + originalOrderId);
                    Order order=matchingEngine.findOrderById((originalOrderId));

                  if(order!=null)
                  {
                      matchingEngine.cancelOrder(originalOrderId);
                  }
                  else
                  {
                      System.out.println(">>> Cancel edilecek emir bulunamadı: "
                              + originalOrderId);
                  }
            }

            else if ("G".equals(msgType)) {

                System.out.println(">>> Replace Order geldi!");

                String originalOrderId =
                        message.getString(OrigClOrdID.FIELD);

                int newQty =
                        (int) message.getDouble(OrderQty.FIELD);

                Double newPrice = null;

                if (message.isSetField(Price.FIELD)) {
                    newPrice = message.getDouble(Price.FIELD);
                }

                System.out.println(
                        ">>> Replace edilen emir: "
                                + originalOrderId
                                + " | Yeni Qty: "
                                + newQty
                                + " | Yeni Price: "
                                + newPrice
                );

                boolean replaced =
                        matchingEngine.replaceOrder(
                                originalOrderId,
                                newQty,
                                newPrice
                        );

                if (replaced) {
                    System.out.println(
                            ">>> Order Replace edildi: "
                                    + originalOrderId
                    );
                } else {
                    System.out.println(
                            ">>> Replace edilecek emir bulunamadı: "
                                    + originalOrderId
                    );
                }
            }

        } catch (FieldNotFound e) {

            System.out.println(
                    "FIX mesajında gerekli alan bulunamadı!"
            );

            e.printStackTrace();
        }
    }
}