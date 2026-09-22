package org.twardy7991.protocolsimulator.Protocols;

public interface SenderProtocol extends Protocol {
    void sendMessage(byte[] data);

    void rdt_send(byte[] data, String address) throws InterruptedException;
}
