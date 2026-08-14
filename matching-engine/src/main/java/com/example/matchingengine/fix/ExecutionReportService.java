package com.example.matchingengine.fix;

import org.springframework.stereotype.Component;
import quickfix.field.OrderID;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.Side;
import quickfix.fix44.ExecutionReport;
import quickfix.field.Symbol;
import com.example.matchingengine.model.Order;

@Component
public class ExecutionReportService {

    private SessionID sessionId;

    // Matching Engine'in hangi FIX oturumuna cevap göndereceğini tutuyoruz
    public void setSessionId(SessionID sessionId) {
        this.sessionId = sessionId;
    }

    // Eşleşme sonucunu Order Service'e FIX ile gönderir
    public void sendExecutionReport(
            Order order,
            int matchedQty) throws Exception {

        if (sessionId == null) {
            System.out.println(
                    "Execution Report gönderilemedi: FIX session yok."
            );
            return;
        }

        int remainingQty = order.getQty();

        char execType;
        char orderStatus;

        if (remainingQty == 0) {

            execType = ExecType.FILL;
            orderStatus = OrdStatus.FILLED;

        } else {

            execType = ExecType.PARTIAL_FILL;
            orderStatus = OrdStatus.PARTIALLY_FILLED;
        }

        ExecutionReport report =
                new ExecutionReport();

        // Hangi emir?
        report.set(
                new ClOrdID(order.getOrderId())
        );
        // FIX Order ID
        report.set(
                new OrderID(order.getOrderId())
        );
        report.set(
                new Symbol(order.getSymbol())
        );

        // Execution Report'un kendi ID'si
        report.set(
                new ExecID(java.util.UUID.randomUUID().toString())
        );

        // Fill mi partial fill mi?
        report.set(
                new ExecType(execType)
        );

        // Emrin durumu
        report.set(
                new OrdStatus(orderStatus)
        );

        // BUY / SELL
        if ("BUY".equalsIgnoreCase(order.getSide())) {
            report.set(new Side(Side.BUY));
        } else {
            report.set(new Side(Side.SELL));
        }

        // Toplam gerçekleşen miktar
        report.set(
                new CumQty(matchedQty)
        );

        // Kalan miktar
        report.set(
                new LeavesQty(remainingQty)
        );

        // Ortalama fiyat
        if (order.getPrice() != null) {
            report.set(
                    new AvgPx(
                            order.getPrice() != null
                                    ? order.getPrice()
                                    : 0.0
                    )
            );
        }

        Session.sendToTarget(
                report,
                sessionId
        );

        System.out.println(
                "Execution Report gönderildi: "
                        + order.getOrderId()
                        + " | Matched: "
                        + matchedQty
                        + " | Remaining: "
                        + remainingQty
        );
    }
}