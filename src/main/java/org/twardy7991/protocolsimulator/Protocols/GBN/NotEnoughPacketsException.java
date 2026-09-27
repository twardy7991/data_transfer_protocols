package org.twardy7991.protocolsimulator.Protocols.GBN;

public class NotEnoughPacketsException extends RuntimeException {
    public NotEnoughPacketsException(String message) {
        super(message);
    }
}
