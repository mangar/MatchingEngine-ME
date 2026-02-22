package org.matchingengine;

import java.util.*;


/**
 * Representação otimizada de um Order Book para um Matching Engine.
 * 1. Preços e quantidades utilizam 'long' para evitar erros de ponto flutuante.
 * 2. ArrayDeque substitui LinkedList para maior eficiência de memória e cache.
 */
public class OrderBook {

    // Níveis de preço de Venda (Asks): Menor preço tem prioridade
    // Preço (long) mapeia para uma fila de ordens (ArrayDeque)
    private final TreeMap<Long, ArrayDeque<Order>> askLevels = new TreeMap<>();

    // Níveis de preço de Compra (Bids): Maior preço tem prioridade
    private final TreeMap<Long, ArrayDeque<Order>> bidLevels = new TreeMap<>(Collections.reverseOrder());

    // Times and Trades - Histórico de negócios...
    // Utilizados no executeTrade()
    private final List<Trade> tradeHistory = new ArrayList<>();
    private long tradeCounter = 0;

    /**
     * Processa uma nova ordem recebida.
     */
    public void processOrder(Order newOrder) {
        if (newOrder.side == Side.BUY) {
            match(newOrder, askLevels);
            if (newOrder.remainingQuantity > 0) {
                addOrderToLevels(newOrder, bidLevels);
            }
        } else {
            match(newOrder, bidLevels);
            if (newOrder.remainingQuantity > 0) {
                addOrderToLevels(newOrder, askLevels);
            }
        }


        this.printBookState();

    }

    /**
     * Algoritmo de cruzamento (Matching)
     */
    private void match(Order newOrder, TreeMap<Long, ArrayDeque<Order>> counterLevels) {
        Iterator<Map.Entry<Long, ArrayDeque<Order>>> it = counterLevels.entrySet().iterator();

        while (it.hasNext() && newOrder.remainingQuantity > 0) {
            Map.Entry<Long, ArrayDeque<Order>> entry = it.next();
            long bestPrice = entry.getKey();

            // Verifica se o preço é compatível (Lógica de inteiros)
            if ((newOrder.side == Side.BUY && newOrder.price >= bestPrice) ||
                    (newOrder.side == Side.SELL && newOrder.price <= bestPrice)) {

                ArrayDeque<Order> ordersAtLevel = entry.getValue();

                while (!ordersAtLevel.isEmpty() && newOrder.remainingQuantity > 0) {
                    Order restingOrder = ordersAtLevel.peek();
                    long matchQty = Math.min(newOrder.remainingQuantity, restingOrder.remainingQuantity);

                    // Execução do Trade
                    executeTrade(newOrder, restingOrder, matchQty, bestPrice);

                    newOrder.remainingQuantity -= matchQty;
                    restingOrder.remainingQuantity -= matchQty;

                    if (restingOrder.remainingQuantity == 0) {
                        ordersAtLevel.poll(); // Remove ordem totalmente preenchida
                    }
                }

                if (ordersAtLevel.isEmpty()) {
                    it.remove(); // Remove nível de preço vazio
                }
            } else {
                break; // Preços não são mais compatíveis
            }
        }
    }

    private void addOrderToLevels(Order order, TreeMap<Long, ArrayDeque<Order>> levels) {
        levels.computeIfAbsent(order.price, k -> new ArrayDeque<>()).add(order);
    }

    private void executeTrade(Order taker, Order maker, long qty, long price) {
        // Exibindo o preço formatado (dividindo pela escala de 10.000 se necessário)
        System.out.printf("TRADE: %d unidades a %d (Taker: %s, Maker: %s)%n",
                qty, price, taker.id, maker.id);

        tradeCounter++;
        String tradeId = "T-" + tradeCounter;

        Trade trade = new Trade(tradeId, taker.id, maker.id, qty, price, taker.side);

        // 1. Armazena no histórico (Módulo de Ledger)
        tradeHistory.add(trade);

        // 2. Log para debug
        System.out.println(trade);

        // 3. (Futuro) Aqui você chamaria o Market Data Publisher para atualizar o gráfico
        //TODO Market Data Publisher

    }

    public List<Trade> getTradeHistory() {
        return Collections.unmodifiableList(tradeHistory);
    }

    /**
     *
     * Método Auxiliar, apenas para fins de desenvolvimento e debug
     *
     */
    public void printBookState() {
        System.out.println("\n==============================================");
        System.out.println("   SUPERDOM - MATCHING ENGINE (SNA-52)       ");
        System.out.println("==============================================");
        System.out.println(" Qtd Compra |    PREÇO    | Qtd Venda | Ordens");
        System.out.println("------------|-------------|-----------|-------");

        // 1. ASKS (Venda) - Mostrados de cima para baixo (preços maiores primeiro)
        askLevels.descendingMap().forEach((price, orders) -> {
            long totalVol = orders.stream().mapToLong(o -> o.remainingQuantity).sum();
            System.out.printf("            |  %10d | %9d | (%d)%n",
                    price, totalVol, orders.size());
        });

        // Linha do Spread (Onde o mercado se encontra)
//        System.out.println(" >> SPREAD  |             |           |");

        // 2. BIDS (Compra) - Mostrados de cima para baixo (melhor compra primeiro)
        bidLevels.forEach((price, orders) -> {
            long totalVol = orders.stream().mapToLong(o -> o.remainingQuantity).sum();
            System.out.printf(" %10d |  %10d |           | (%d)%n",
                    totalVol, price, orders.size());
        });


        if (!getTradeHistory().isEmpty()) {
            System.out.println("----------------------------------------------");
            Trade lastTrade = getTradeHistory().get(getTradeHistory().size() - 1);

            // Formatação visual: VERDE para compra, VERMELHO para venda (se o terminal suportar ANSI)
            String sideStr = (lastTrade.getSide() == Side.BUY) ? "COMPRA" : "VENDA";

            System.out.printf(" ÚLTIMO TRADE: %s de %d à %d  %n",
                    sideStr,
                    lastTrade.getQuantity(),
                    lastTrade.getPrice()
                    );
        }
        System.out.println("==============================================\n");

    }

}
