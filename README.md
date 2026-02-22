# Matching Engine (ME)

# Executando

## Primeira Execução - Core  ```org.matchingengine.core.Main```

__Book e Trade__: 

``` 
--- Inserindo ordens de VENDA INICIAIS ---

==============================================
   SUPERDOM - MATCHING ENGINE (SNA-52)       
==============================================
 Qtd Compra |    PREÇO    | Qtd Venda | Ordens
------------|-------------|-----------|-------
            |      500000 |        10 | (1)
==============================================


==============================================
   SUPERDOM - MATCHING ENGINE (SNA-52)       
==============================================
 Qtd Compra |    PREÇO    | Qtd Venda | Ordens
------------|-------------|-----------|-------
            |      501000 |         5 | (1)
            |      500000 |        10 | (1)
==============================================


--- Inserindo ordens de COMPRAS longe do preço de venda ---

==============================================
   SUPERDOM - MATCHING ENGINE (SNA-52)       
==============================================
 Qtd Compra |    PREÇO    | Qtd Venda | Ordens
------------|-------------|-----------|-------
            |      501000 |         5 | (1)
            |      500000 |        10 | (1)
         10 |      490000 |           | (1)
==============================================


> > > > o < < < <

--- Tentando compra que gera Match Parcial ---
TRADE: 4 unidades a 500000 (Taker: Compra_D, Maker: Venda_A)
[2026-02-22T02:54:15.423266300Z] TRADE ID: T-1 | Preço: 500000 | Qtd: 4 | Taker: Compra_D | Maker: Venda_A

==============================================
   SUPERDOM - MATCHING ENGINE (SNA-52)       
==============================================
 Qtd Compra |    PREÇO    | Qtd Venda | Ordens
------------|-------------|-----------|-------
            |      501000 |         5 | (1)
            |      500000 |         6 | (1)
         10 |      490000 |           | (1)
----------------------------------------------
 ÚLTIMO TRADE: COMPRA de 4 à 500000  
==============================================


--- Tentando compra que limpa o nível e sobra no book ---
TRADE: 6 unidades a 500000 (Taker: Compra_E, Maker: Venda_A)
[2026-02-22T02:54:15.426310200Z] TRADE ID: T-2 | Preço: 500000 | Qtd: 6 | Taker: Compra_E | Maker: Venda_A

==============================================
   SUPERDOM - MATCHING ENGINE (SNA-52)       
==============================================
 Qtd Compra |    PREÇO    | Qtd Venda | Ordens
------------|-------------|-----------|-------
            |      501000 |         5 | (1)
          4 |      500000 |           | (1)
         10 |      490000 |           | (1)
----------------------------------------------
 ÚLTIMO TRADE: COMPRA de 6 à 500000  
==============================================


--- Tentando venda agressiva (Market Sweep) ---
TRADE: 2 unidades a 500000 (Taker: Venda_F, Maker: Compra_E)
[2026-02-22T02:54:15.427831700Z] TRADE ID: T-3 | Preço: 500000 | Qtd: 2 | Taker: Venda_F | Maker: Compra_E

==============================================
   SUPERDOM - MATCHING ENGINE (SNA-52)       
==============================================
 Qtd Compra |    PREÇO    | Qtd Venda | Ordens
------------|-------------|-----------|-------
            |      501000 |         5 | (1)
          2 |      500000 |           | (1)
         10 |      490000 |           | (1)
----------------------------------------------
 ÚLTIMO TRADE: VENDA de 2 à 500000  
==============================================

```

## Ingress

1. Inicie o Servidor: ``org.matchingengine.ingress.MainFixServer``
2. Rode o cliente: ``org.matchengine.ingress.MainFixClientSimulator``

Após executar o cliente, o output deve ser algo parecido com isto:

    Logon: FIX.4.4:SNA52_EXCHANGE->XP_INVEST
    >>> Mensagem FIX recebida no Adapter: 8=FIX.4.49=14135=D34=249=XP_INVEST52=20260222-17:12:20.94256=SNA52_EXCHANGE11=CLIENT_ORD_00138=10040=244=5054=155=PETR460=20260222-17:12:20.94110=071
    [FIX INGRESS] Ordem recebida via FIX: CLIENT_ORD_001 de SNA52_EXCHANGE
    
    =================================================================================
    LIVRO DE OFERTAS DETALHADO (MARKET BY ORDER) - SNA-52            
    =================================================================================
    COMPRA (LOTES/CORR)     |    PREÇO    |     VENDA (LOTES/CORR)    | ID ORDEM
    ----------------------------|-------------|---------------------------|----------
    ----------------------------|  S P R E A D |---------------------------|----------
    100 (SNA52_EXCHANGE) |      500000 |                           | CLIENT_ORD_001
    =================================================================================
    
    Logout: FIX.4.4:SNA52_EXCHANGE->XP_INVEST



---

# 1. Informações Úteis
O tema é específico, então as melhores fontes são documentos técnicos de bolsas reais e implementações de código aberto de alta performance:

- LMAX Disruptor: Pesquise sobre a arquitetura da LMAX Exchange. O "Disruptor" é um padrão de Ring Buffer em Java criado especificamente para resolver o problema de latência em bolsas.
- Fix Protocol (fixprotocol.org): Entenda como as mensagens de ordens (FIX) trafegam. Mesmo que você use JSON para simplificar, a lógica do protocolo FIX é o padrão da indústria.
- GitHub (Projetos Open Source):
    - CQEngine: Para consultas ultrarrápidas em memória.
    - Agrona: Biblioteca com estruturas de dados de baixa latência.
    - Aeron: Para transporte de mensagens ultra-rápido entre módulos.

# 2. Detalhes Técnicos Críticos

- Zero Allocation (ou Near-Zero): Evite criar objetos dentro do loop principal de matching. Use tipos primitivos sempre que possível. Cada objeto criado é um potencial "Stop the World" do GC.
- Determinação Sequencial: O motor deve ser determinístico. Dada uma sequência de entradas, a saída deve ser sempre a mesma. Por isso, a maioria dos MEs modernos é Single-Threaded no núcleo de cruzamento para evitar travas (locks) e disputa de cache.
- Data Structures: * O Order Book geralmente é implementado com um TreeMap ou SortedMap (para os níveis de preço) e uma LinkedList ou ArrayDeque (para a fila FIFO dentro de cada preço).
- CPU Cache Friendly: Tente manter os dados próximos na memória (L1/L2 cache). O uso de Long2ObjectHashMap (de bibliotecas como fastutil ou koloboke) é melhor que o HashMap padrão do Java.


---




# 3. Divisão em Módulos e Motivação
A distribuição deve focar na separação de responsabilidades para que o "núcleo" (Matching) nunca seja interrompido por I/O (entrada/saída).

## 🔨 Módulo A: Gateway (Ingress)
O que faz: Recebe as ordens (via REST, WebSocket ou FIX), valida a sintaxe e a autenticação.
__Motivador__: Isolar a rede. O núcleo não pode esperar um pacote TCP chegar. O Gateway limpa a "sujeira" e entrega uma mensagem pronta.

- QuickFix/J


## Módulo B: Sequencer (Opcional, mas recomendado)
O que faz: Atribui um número de sequência único e global para cada mensagem recebida.

Motivador: Garante a ordem de chegada (First-Come, First-Served). Se você tiver vários Gateways, o Sequencer dita a ordem justa.

## ✅ Módulo C: Matching Engine (O Core)
O que faz: Mantém o Order Book em memória e executa o algoritmo de cruzamento (Price-Time Priority).
__Motivador__: Performance Pura. Este módulo deve rodar isolado, idealmente com afinidade de CPU (CPU pinning), sem fazer acesso a disco ou banco de dados diretamente.

## Módulo D: Ledger / Journaler (Persistência)
O que faz: Grava cada evento (ordem aceita, execução, cancelamento) em um log sequencial (Append-only).

Motivador: Recuperação de desastres. Se o sistema cair, você reconstrói o estado da memória relendo o Journal.

## Módulo E: Market Data Publisher (Egress)
O que faz: Notifica o mundo exterior sobre as execuções (Trades) e mudanças no livro de ofertas (L1/L2 data).

Motivador: Desacoplamento. O Matching Engine "cospe" o resultado e segue para a próxima ordem; este módulo cuida da entrega lenta para os clientes.

LMAX Disruptor para comunicar o Gateway com o Matching Engine. É a forma mais performática de passar dados entre threads em Java.






---

## TODO

- Ingress
- Core
  - LMAX com Ingress
  - WebSocket com Cotação/DOM e Trades
- Egress
- Ledger