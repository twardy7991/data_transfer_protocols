package org.twardy7991.protocolsimulator.Protocols;

public interface Protocol extends Runnable {

    void rdt_receive(byte[] packet) throws InterruptedException;

    void deliverMessage(byte[] packet);

    void stop();
}
