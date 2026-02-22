package org.matchingengine.ingress;

import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.NewOrderSingle;

import java.io.InputStream;

public class MainFixClientSimulator {

    public static void main(String[] args) throws Exception {
        // Configurações do Cliente (Initiator)
        InputStream configStream = MainFixClientSimulator.class.getClassLoader().getResourceAsStream("fix-client.cfg");
        SessionSettings settings = new SessionSettings(configStream);
//        SessionSettings settings = new SessionSettings("fix-client.cfg");

        Application application = new Application() {
            @Override public void fromApp(Message msg, SessionID id) { System.out.println("Recebido da Bolsa: " + msg); }
            @Override public void onLogon(SessionID id) { System.out.println("Logon realizado na Bolsa!"); }
            // ... implementar outros métodos vazios ...
            @Override public void onCreate(SessionID id) {}
            @Override public void onLogout(SessionID id) {}
            @Override public void toAdmin(Message msg, SessionID id) {}
            @Override public void fromAdmin(Message msg, SessionID id) {}
            @Override public void toApp(Message msg, SessionID id) {}
        };

        FileStoreFactory storeFactory = new FileStoreFactory(settings);
        DefaultMessageFactory messageFactory = new DefaultMessageFactory();
        SocketInitiator initiator = new SocketInitiator(application, storeFactory, settings, messageFactory);

        initiator.start();
        Thread.sleep(2000); // Aguarda o logon

        // ENVIANDO UMA ORDEM DE COMPRA
        SessionID sessionID = (SessionID) initiator.getSessions().get(0);
        NewOrderSingle order = new NewOrderSingle(
                new ClOrdID("CLIENT_ORD_001"),
                new Side(Side.BUY),
                new TransactTime(),
                new OrdType(OrdType.LIMIT)
        );
        order.set(new Price(50.00));
        order.set(new OrderQty(100));
        order.set(new Symbol("PETR4"));

        Session.sendToTarget(order, sessionID);
        System.out.println("Ordem de Compra enviada para a SNA-52!");

        Thread.sleep(5000);
        initiator.stop();
    }
}
