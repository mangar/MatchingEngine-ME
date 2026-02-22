package org.matchingengine;

import java.time.Instant;

/**
 *
 */
public class Trade {
    private final String tradeId;
    private final String takerOrderId;
    private final String makerOrderId;
    private final String takerBroker;
    private final String makerBroker;
    private final long quantity;
    private final long price;
    private final Side side; // Lado do agressor
    private final Instant timestamp;

    public Trade(String tradeId, String takerOrderId, String makerOrderId,
                 String takerBroker, String makerBroker,
                 long quantity, long price, Side side) {
        this.tradeId = tradeId;
        this.takerOrderId = takerOrderId;
        this.makerOrderId = makerOrderId;
        this.takerBroker = takerBroker;
        this.makerBroker = makerBroker;
        this.quantity = quantity;
        this.price = price;
        this.side = side;
        this.timestamp = Instant.now();
    }

    @Override
    public String toString() {
        return String.format("[%s] TRADE %s | %d @ %d | Taker: %s (%s) | Maker: %s (%s) | Side: %s",
                timestamp, tradeId, quantity, price,
                takerOrderId, takerBroker, makerOrderId, makerBroker, side);
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

    public String getTakerBroker() {
        return takerBroker;
    }

    public String getMakerBroker() {
        return makerBroker;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getPrice() {
        return price;
    }

    public Side getSide() {
        return side;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
