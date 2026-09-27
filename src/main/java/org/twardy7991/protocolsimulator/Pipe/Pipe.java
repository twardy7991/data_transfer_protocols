package org.twardy7991.protocolsimulator.Pipe;

import org.twardy7991.protocolsimulator.Protocols.Protocol;

public interface Pipe extends Runnable{
    void addReadyPacket(byte[] packet, String address);

    void stop();

    void run();

    void subscribe(String address, Protocol socket);

    void udt_send(byte[] packet, String address, boolean canCorrupt, boolean canLose, boolean isDelayed) throws InterruptedException ;
}
