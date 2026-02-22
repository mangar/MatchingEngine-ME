package org.matchingengine.ingress;

import org.matchingengine.core.Order;
import org.matchingengine.core.OrderBook;
import org.matchingengine.core.Side;
import quickfix.*;
import quickfix.fix44.NewOrderSingle;

/**
 * Adaptador de Entrada (Ingress) via protocolo FIX 4.4.
 * Converte mensagens FIX em ordens para o Matching Engine.
 */

public class FixIngressAdapter  extends MessageCracker implements Application {

        private final OrderBook orderBook;

        public FixIngressAdapter(OrderBook orderBook) {
            this.orderBook = orderBook;
        }

        // Chamado quando uma nova ordem (NewOrderSingle) chega
        @Handler
        public void onMessage(NewOrderSingle message, SessionID sessionID)
                throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {

            // 1. Extrair campos da mensagem FIX
            String orderId = message.getClOrdID().getValue();
            String broker = sessionID.getSenderCompID(); // Identifica a corretora pela sessão
            char sideFix = message.getSide().getValue();
            long quantity = (long) message.getOrderQty().getValue();

            // Converter preço para escala long (ex: R$ 50.00 -> 500000)
            long price = (long) (message.getPrice().getValue() * 10000);

            // 2. Mapear o Lado (Compra/Venda)
//            Side side = (sideFix == Side.BUY) ? Side.BUY : Side.SELL;
            Side side = (sideFix == quickfix.field.Side.BUY) ? Side.BUY : Side.SELL;

            // 3. Injetar no Matching Engine
            Order engineOrder = new Order(orderId, broker, price, quantity, side);

            System.out.printf("[FIX INGRESS] Ordem recebida via FIX: %s de %s%n", orderId, broker);
            orderBook.processOrder(engineOrder);
        }

        // Callbacks obrigatórios da interface Application
        @Override public void onCreate(SessionID id) {}
        @Override public void onLogon(SessionID id) { System.out.println("Logon: " + id); }
        @Override public void onLogout(SessionID id) { System.out.println("Logout: " + id); }
        @Override public void toAdmin(Message msg, SessionID id) {}
        @Override public void fromAdmin(Message msg, SessionID id) {}
        @Override public void toApp(Message msg, SessionID id) {}
        @Override public void fromApp(Message msg, SessionID id) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {

            // Ver a mensagem bruta (Tag=Value) chegando:
            System.out.println(">>> Mensagem FIX recebida no Adapter: " + msg.toString());


            crack(msg, id); // Direciona para o método onMessage correto
        }
    }

