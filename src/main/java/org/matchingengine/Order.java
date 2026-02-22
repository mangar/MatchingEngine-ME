package org.matchingengine;

// Classes de Suporte atualizadas para tipos primitivos long
enum Side { BUY, SELL }

public class Order {
    String id;
    long price;            // Representado em escala (ex: 100.50 -> 1005000)
    long totalQuantity;
    long remainingQuantity;
    Side side;

    public Order(String id, long price, long quantity, Side side) {
        this.id = id;
        this.price = price;
        this.totalQuantity = quantity;
        this.remainingQuantity = quantity;
        this.side = side;
    }





}
