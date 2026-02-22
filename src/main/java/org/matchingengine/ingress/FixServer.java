package org.matchingengine.ingress;

import org.matchingengine.core.OrderBook;
import quickfix.*;

import java.io.InputStream;


/**
 * Servidor FIX (Acceptor) que inicializa o Gateway de Entrada.
 */
public class FixServer {
    private ThreadedSocketAcceptor acceptor;

    public void start(OrderBook orderBook) throws Exception {
        // Configurações da sessão (porta, host, data dictionaries)
        // Normalmente carregado de um arquivo .cfg
        // Busca o arquivo na raiz do classpath (pasta resources)
        InputStream configStream = getClass().getClassLoader().getResourceAsStream("fix-server.cfg");
        if (configStream == null) {
            throw new RuntimeException("ERRO: O arquivo fix-server.cfg não foi encontrado em src/main/resources!");
        }
        SessionSettings settings = new SessionSettings(configStream);

//        SessionSettings settings = new SessionSettings("fix-server.cfg");

        FixIngressAdapter adapter = new FixIngressAdapter(orderBook);
        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        acceptor = new ThreadedSocketAcceptor(
                adapter, storeFactory, settings, logFactory, messageFactory
        );

        acceptor.start();
        System.out.println("FIX Ingress Gateway rodando na porta 9876...");
    }

    public void stop() {

        if (acceptor != null) acceptor.stop();
        System.out.println("FIX Ingress Gateway parado");
    }
}