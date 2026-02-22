package org.matchingengine.ingress.clients;

public enum Corretora {
    BTG("BTG"),
    UBS("UBS"),
    NECTON("NECTON"),
    XP_INVEST("XP_INVEST"),
    ITAU("ITAU");


    private final String code;
    Corretora(String code) { this.code = code; }
    public String getCode() { return code; }
}
