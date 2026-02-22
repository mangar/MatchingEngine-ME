package org.matchingengine;

// Classes de Suporte atualizadas para tipos primitivos long
enum Side { BUY, SELL }

public class Order {
    String id;
    String broker;         // Nome ou código da Corretora
    long price;
    long totalQuantity;
    long remainingQuantity;
    Side side;

    public Order(String id, String broker, long price, long quantity, Side side) {
        this.id = id;
        this.broker = broker;
        this.price = price;
        this.totalQuantity = quantity;
        this.remainingQuantity = quantity;
        this.side = side;
    }

}
