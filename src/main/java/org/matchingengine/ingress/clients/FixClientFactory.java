package org.matchingengine.ingress.clients;

import quickfix.*;

import java.io.InputStream;

public class FixClientFactory {

    public static SocketInitiator createInitiator(Corretora corretora, Application adapter) throws Exception {
        String fileName = "fix-client-" + corretora.getCode() + ".cfg";
        InputStream configStream = FixClientFactory.class.getClassLoader().getResourceAsStream(fileName);

        if (configStream == null) {
            throw new RuntimeException("Configuração não encontrada para: " + fileName);
        }

        SessionSettings settings = new SessionSettings(configStream);
        FileStoreFactory storeFactory = new FileStoreFactory(settings);
        DefaultMessageFactory messageFactory = new DefaultMessageFactory();

        // Criamos o iniciador vinculado à corretora específica
        return new SocketInitiator(adapter, storeFactory, settings, messageFactory);
    }
}
