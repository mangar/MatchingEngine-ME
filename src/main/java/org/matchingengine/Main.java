package org.matchingengine;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        OrderBook book = new OrderBook();

        // 1. Inserindo ordens de VENDA (Asks) - Corretoras diferentes
        System.out.println("\n--- Inserindo ordens de VENDA INICIAIS ---");
        // Ordem ORD-001 pela XP a 50.00
        book.processOrder(new Order("ORD-001", "XP", 500000, 10, Side.SELL));
        // Ordem ORD-002 pela UBS a 50.10
        book.processOrder(new Order("ORD-002", "UBS", 501000, 5, Side.SELL));
        book.processOrder(new Order("ORD-021", "BTG", 501000, 5, Side.SELL));
        book.processOrder(new Order("ORD-022", "AGORA", 501000, 5, Side.SELL));



        // 2. Inserindo ordens de COMPRA (Bids) longe do preço
        System.out.println("\n--- Inserindo ordens de COMPRAS (Passivas) ---");
        // Ordem ORD-003 pelo BTG a 49.00
        book.processOrder(new Order("ORD-003", "BTG", 490000, 10, Side.BUY));

        System.out.println("\n> > > > ESTADO INICIAL DO LIVRO < < < <");
        book.printBook();

        // 3. Simular um MATCH PARCIAL
        // A ITAU (Taker) ataca o vendedor da XP (Maker)
        System.out.println("\n--- ITAU agredindo a venda da XP (Match Parcial) ---");
        book.processOrder(new Order("ORD-004", "ITAU", 500000, 4, Side.BUY));
        // Resultado: Trade de 4 unidades. Maker ORD-001 (XP) sobra com 6 unidades.

        // 4. Simular um MATCH TOTAL e sobra no Book
        // A BRADESCO (Taker) tenta comprar 10 unidades a 50.00.
        // Limpa as 6 restantes da XP e deixa 4 pendentes no book.
        System.out.println("\n--- BRADESCO limpando a XP e ficando no book ---");
        book.processOrder(new Order("ORD-005", "BRAD", 500000, 10, Side.BUY));

        // 5. Simular uma venda agressiva (Market Sweep)
        // A MERRILL_LYNCH (Taker) vende a 48.00 e atinge a compra do BTG que estava a 49.00
        System.out.println("\n--- MERRILL LYNCH agredindo a compra do BTG (Market Sweep) ---");
        book.processOrder(new Order("ORD-006", "MERRILL", 480000, 2, Side.SELL));

        System.out.println("\n> > > > ESTADO FINAL DO LIVRO < < < <");
        book.printBook();


        System.out.println("\n> > > > NEGOCIAÇÕES FECHADAS < < < <");
        book.printTradeHistory();



    }
}
