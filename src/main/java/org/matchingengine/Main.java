package org.matchingengine;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        OrderBook book = new OrderBook();

        // 1. Adicionar ordens de VENDA (Asks) que ficarão apenas apregoadas (no livro)
        System.out.println("\n--- Inserindo ordens de VENDA INICIAIS ---");
        book.processOrder(new Order("Venda_A", 500000, 10, Side.SELL)); // Venda a 50.00
        book.processOrder(new Order("Venda_B", 501000, 5, Side.SELL));  // Venda a 50.10

        // 2. Adicionar ordens de COMPRA (Bids) que ficarão apenas apregoadas
        System.out.println("\n--- Inserindo ordens de COMPRAS longe do preço de venda ---");
        book.processOrder(new Order("Compra_C", 490000, 10, Side.BUY)); // Compra a 49.00


        System.out.println("\n> > > > o < < < <");

        // 3. Simular um MATCH PARCIAL
        // Tenta comprar a 50.00, onde já existe a "Venda_A" com 10 unidades
        System.out.println("\n--- Tentando compra que gera Match Parcial ---");
        book.processOrder(new Order("Compra_D", 500000, 4, Side.BUY));
        // Resultado esperado: Match de 4 unidades com Venda_A a 50.00. Venda_A sobra 6.

        // 4. Simular um MATCH TOTAL e sobra no Book
        // Tenta comprar 10 unidades a 50.00.
        // Vai limpar as 6 que sobraram da Venda_A e os 4 restantes ficam apregoados na Compra_E
        System.out.println("\n--- Tentando compra que limpa o nível e sobra no book ---");
        book.processOrder(new Order("Compra_E", 500000, 10, Side.BUY));

        // 5. Simular uma venda que "atropela" o livro (Market Sweep)
        // Vende a 48.00 (muito barato), vai bater com a "Compra_C" que está a 49.00
        System.out.println("\n--- Tentando venda agressiva (Market Sweep) ---");
        book.processOrder(new Order("Venda_F", 480000, 2, Side.SELL));
    }
}
