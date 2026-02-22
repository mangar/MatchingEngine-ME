package org.matchingengine.ingress.clients;

import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.NewOrderSingle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.UUID;

public class MultiFixClientManager {

    private static final Map<Corretora, SocketInitiator> activeClients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        // Exemplo: Subindo todos os clientes listados no Enum
        for (Corretora c : Corretora.values()) {
            startClient(c);
        }

        Thread.sleep(1000); // Aguarda logons


        double buyPrice = 50.00;
        double sellPrice = 51.00;
        int quantity = 100;
        String symbol = "PETR4";

        for (Corretora corretora : Corretora.values()) {
            // 1. Criar Ordem de Compra (Inicia em 50.00 e subtrai 0.10)
            sendOrder(corretora, symbol, Side.BUY, buyPrice, quantity);

            // 2. Criar Ordem de Venda (Inicia em 51.00 e soma 0.10)
            sendOrder(corretora, symbol, Side.SELL, sellPrice, quantity);

            // Atualiza os preços para a próxima corretora no loop
            buyPrice -= 0.10;
            sellPrice += 0.10;

            // Pequeno sleep para não sobrecarregar o log e permitir visualização
            Thread.sleep(100);
        }



//        // Exemplo de envio dinâmico: BTG envia uma ordem
//        sendOrder(Corretora.BTG, "PETR4", Side.BUY, 50.00, 100);
//        sendOrder(Corretora.ITAU, "PETR4", Side.BUY, 50.10, 100);
//        sendOrder(Corretora.NECTON, "PETR4", Side.BUY, 50.20, 100);
//
//        // Exemplo de envio dinâmico: UBS envia uma ordem no mesmo Book
//        sendOrder(Corretora.UBS, "PETR4", Side.SELL, 51.00, 200);
//        sendOrder(Corretora.XP_INVEST, "PETR4", Side.SELL, 51.90, 200);
//        sendOrder(Corretora.NECTON, "PETR4", Side.SELL, 51.80, 200);

        Thread.sleep(1000);
        stopAll();
    }

    private static void startClient(Corretora corretora) throws Exception {
        Application clientApp = new Application() {
            @Override public void onLogon(SessionID id) { System.out.println("[" + corretora + "] Logon OK: " + id); }
            @Override public void fromApp(Message msg, SessionID id) { System.out.println("[" + corretora + "] Recebido: " + msg); }
            // ... implementar outros métodos vazios ...
            @Override public void onCreate(SessionID id) {}
            @Override public void onLogout(SessionID id) {}
            @Override public void toAdmin(Message msg, SessionID id) {}
            @Override public void fromAdmin(Message msg, SessionID id) {}
            @Override public void toApp(Message msg, SessionID id) {}
        };

        SocketInitiator initiator = FixClientFactory.createInitiator(corretora, clientApp);
        initiator.start();
        activeClients.put(corretora, initiator);
    }

    public static void sendOrder(Corretora corretora, String symbol, char side, double price, int qty) throws Exception {
        SocketInitiator initiator = activeClients.get(corretora);
        if (initiator == null || initiator.getSessions().isEmpty()) return;

        SessionID sessionID = initiator.getSessions().get(0);

        NewOrderSingle order = new NewOrderSingle(
                new ClOrdID(corretora.getCode() + "_" + System.currentTimeMillis()), // + "_" + UUID.randomUUID().toString()),
                new Side(side),
                new TransactTime(),
                new OrdType(OrdType.LIMIT)
        );
        order.set(new Symbol(symbol));
        order.set(new Price(price));
        order.set(new OrderQty(qty));

        Session.sendToTarget(order, sessionID);
        System.out.println(">>> " + corretora + " enviou ordem de " + (side == Side.BUY ? "Compra" : "Venda"));
    }

    private static void stopAll() {
        activeClients.values().forEach(SocketInitiator::stop);
    }
}