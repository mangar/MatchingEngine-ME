package org.matchingengine.ingress;

import org.matchingengine.core.OrderBook;

public class MainFixServer {


    public static void main(String[] args) {
        try {
            // 1. Instancia o motor de cruzamento
            OrderBook orderBook = new OrderBook();

            // 2. Inicia o Servidor FIX
            FixServer server = new FixServer();
            server.start(orderBook);

            System.out.println(">>> EXCHANGE SNA-52 ESTÁ ONLINE E AGUARDANDO ORDENS...");

            // Mantém o programa rodando enquanto o servidor estiver ativo
            Thread.sleep(Long.MAX_VALUE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}