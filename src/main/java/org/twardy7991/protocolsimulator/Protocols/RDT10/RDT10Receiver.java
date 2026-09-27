package org.twardy7991.protocolsimulator.Protocols.RDT10;

import org.twardy7991.protocolsimulator.Pipe.Pipe;
import org.twardy7991.protocolsimulator.Protocols.AbstractReceiverProtocol;
import org.twardy7991.protocolsimulator.Protocols.Events.NewPacketEvent;
import org.twardy7991.protocolsimulator.Util;

import static java.lang.System.Logger.Level.DEBUG;

public class RDT10Receiver extends AbstractReceiverProtocol {

    private volatile boolean running;

    public RDT10Receiver(Pipe pipe) {
        super(pipe);
    }

    @Override
    public void rdt_receive(byte[] packet){
        byte data = Util.extract(packet);
        logger.log(DEBUG, "RECEIVER: received packet: %s".formatted(Integer.toBinaryString(packet[0] & 0xFF)));
        this.deliverData(data);
    }

    @Override
    protected void handleEventQueue() throws InterruptedException {
        NewPacketEvent event = (NewPacketEvent) this.eventQueue.take();
        this.handlePacketEvent(event);
    }
}
