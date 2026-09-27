package org.twardy7991.protocolsimulator.Protocols;

/*
interface for sender side of protocol that uses additional methods
 */
public interface SenderProtocol extends Protocol {

    /**
    method for sending logic
     **/
    void sendMessage(byte[] data);

    /**
    used by protocol to pass message to Pipe
     **/
    void rdt_send(byte[] data, String address) throws InterruptedException;
}
