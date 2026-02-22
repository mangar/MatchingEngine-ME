package org.matchingengine.core;

import org.matchingengine.Trade;

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


        this.printBook();

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

        Trade trade = new Trade(
                tradeId,
                taker.id,
                maker.id,
                taker.broker,
                maker.broker,
                qty,
                price,
                taker.side
        );

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



    public void printBook(){
        this.printDetailedOrderBook();
    }


    /**
     *
     * Método Auxiliar, apenas para fins de desenvolvimento e debug
     *
     */
    public void printSuperDOM() {
        System.out.println("\n==============================================================");
        System.out.println("            SUPERDOM - MATCHING ENGINE (SNA-52)               ");
        System.out.println("==============================================================");
        System.out.println("   COMPRA (LOTES)   |    PREÇO    |   VENDA (LOTES)   | ORDENS");
        System.out.println("--------------------|-------------|-------------------|-------");

        // 1. ASKS (Venda) - Preços maiores primeiro
        askLevels.descendingMap().forEach((price, orders) -> {
            long totalVol = orders.stream().mapToLong(o -> o.remainingQuantity).sum();
            System.out.printf("                    |  %10d | %17d | (%d)%n",
                    price, totalVol, orders.size());
        });

        System.out.println(">>----------------------------------------------------------<<");

        // 2. BIDS (Compra) - Melhor compra primeiro
        bidLevels.forEach((price, orders) -> {
            long totalVol = orders.stream().mapToLong(o -> o.remainingQuantity).sum();
            System.out.printf(" %18d |  %10d |                   | (%d)%n",
                    totalVol, price, orders.size());
        });

        System.out.println("==============================================================");

        // Rodapé com detalhes da última execução
        if (!tradeHistory.isEmpty()) {
            Trade lastTrade = tradeHistory.get(tradeHistory.size() - 1);
            String lado = (lastTrade.getSide() == Side.BUY) ? "COMPRA" : "VENDA";

            System.out.printf(" ÚLTIMO TRADE: %d | QTD: %d | AGRESSÃO: %s%n",
                    lastTrade.getPrice(), lastTrade.getQuantity(), lado);
            System.out.printf(" EXECUÇÃO: Taker %s vs Maker %s (%s)%n",
                    lastTrade.getTakerBroker(), lastTrade.getMakerBroker(), lastTrade.getMakerOrderId());
        }
        System.out.println("==============================================================\n");
    }

    /**
     * Imprime o histórico completo de transações (Time & Sales).
     * Identifica Comprador e Vendedor com base no lado do agressor (Taker).
     */
    public void printTradeHistory() {
        System.out.println("\n================================================================================================================");
        System.out.println("                                  HISTÓRICO DE TRANSAÇÕES (TIME & SALES)                                     ");
        System.out.println("================================================================================================================");
        System.out.println(" DATA/HORA                      | ID TRADE | COMPRADOR      | PREÇO      | QTD | VENDEDOR       | LADO AGRESSOR ");
        System.out.println("--------------------------------|----------|----------------|------------|-----|----------------|---------------");

        for (Trade t : tradeHistory) {
            String comprador;
            String vendedor;

            // Lógica para definir Comprador/Vendedor baseado no agressor (Taker)
            if (t.getSide() == Side.BUY) {
                // Se o agressor comprou, o Taker é o comprador e o Maker é o vendedor
                comprador = t.getTakerBroker();
                vendedor = t.getMakerBroker();
            } else {
                // Se o agressor vendeu, o Taker é o vendedor e o Maker é o comprador
                comprador = t.getMakerBroker();
                vendedor = t.getTakerBroker();
            }

            System.out.printf(" %-30s | %-8s | %-14s | %10d | %3d | %-14s | %-13s %n",
                    t.getTimestamp(),
                    t.getTradeId(),
                    comprador,
                    t.getPrice(),
                    t.getQuantity(),
                    vendedor,
                    t.getSide());
        }
        System.out.println("================================================================================================================\n");
    }


    /**
     * Imprime o livro de ofertas detalhado (sem agrupamento por preço).
     * Exibe cada ordem individualmente com sua respectiva corretora.
     */
    public void printDetailedOrderBook() {
        System.out.println("\n=================================================================================");
        System.out.println("                LIVRO DE OFERTAS DETALHADO (MARKET BY ORDER) - SNA-52            ");
        System.out.println("=================================================================================");
        System.out.println("    COMPRA (LOTES/CORR)     |    PREÇO    |     VENDA (LOTES/CORR)    | ID ORDEM ");
        System.out.println("----------------------------|-------------|---------------------------|----------");

        // 1. ASKS (Venda) - Mostra cada ordem individualmente
        // Usamos descendingMap para que o melhor preço (menor) fique perto do spread
        askLevels.descendingMap().forEach((price, orders) -> {
            for (Order o : orders) {
                String detail = String.format("%d (%s)", o.remainingQuantity, o.broker);
                System.out.printf("                            |  %10d | %25s | %s %n",
                        price, detail, o.id);
            }
        });

        System.out.println("----------------------------|  S P R E A D |---------------------------|----------");

        // 2. BIDS (Compra) - Mostra cada ordem individualmente
        bidLevels.forEach((price, orders) -> {
            for (Order o : orders) {
                String detail = String.format("%d (%s)", o.remainingQuantity, o.broker);
                System.out.printf(" %26s |  %10d |                           | %s %n",
                        detail, price, o.id);
            }
        });

        System.out.println("=================================================================================\n");
    }


}
