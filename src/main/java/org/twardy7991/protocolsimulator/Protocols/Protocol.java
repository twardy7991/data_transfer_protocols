package org.twardy7991.protocolsimulator.Protocols;

/*
Interface for all protocols
 */
public interface Protocol extends Runnable {

    /**
    Used by protocol to implement logic and serve incoming packet
     **/
    void rdt_receive(byte[] packet) throws InterruptedException;

    /**
    Used by Pipe to deliver message to queue of protocol
    **/
    void deliverMessage(byte[] packet);

    /**
    Stops the protocol
     **/
    void stop();
}
