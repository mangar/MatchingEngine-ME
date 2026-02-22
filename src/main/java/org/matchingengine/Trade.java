package org.matchingengine;

import java.time.Instant;

/**
 *
 */
public class Trade {
    final String tradeId;
    final String takerOrderId;
    final String makerOrderId;
    final long quantity;
    final long price;
    final Instant timestamp;
    final Side side; // Lado do AGRESSOR (Taker)

    public Trade(String tradeId, String takerOrderId, String makerOrderId, long quantity, long price, Side side) {
        this.tradeId = tradeId;
        this.takerOrderId = takerOrderId;
        this.makerOrderId = makerOrderId;
        this.quantity = quantity;
        this.price = price;
        this.side = side;
        this.timestamp = Instant.now();
    }

    @Override
    public String toString() {
        return String.format("[%s] TRADE ID: %s | Preço: %d | Qtd: %d | Taker: %s | Maker: %s",
                timestamp, tradeId, price, quantity, takerOrderId, makerOrderId);
    }


    public String getTradeId() {
        return tradeId;
    }

    public String getTakerOrderId() {
        return takerOrderId;
    }

    public String getMakerOrderId() {
        return makerOrderId;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getPrice() {
        return price;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Side getSide() {
        return side;
    }
}
