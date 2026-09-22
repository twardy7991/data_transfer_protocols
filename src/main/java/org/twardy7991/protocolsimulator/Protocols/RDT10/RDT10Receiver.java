package org.twardy7991.protocolsimulator.Protocols.RDT10;


import org.twardy7991.protocolsimulator.Protocols.AbstractReceiverProtocol;
import org.twardy7991.protocolsimulator.Protocols.Events.NewPacketEvent;
import org.twardy7991.protocolsimulator.Util;

public class RDT10Receiver extends AbstractReceiverProtocol {

    private volatile boolean running;

    public RDT10Receiver(Util util) {
        super(util);
    }

    @Override
    public void rdt_receive(byte[] packet){
        byte data = Util.extract(packet);
        this.deliverData(data);
    }

    @Override
    protected void handleEventQueue() throws InterruptedException {
        NewPacketEvent event = (NewPacketEvent) this.eventQueue.take();
        this.handlePacketEvent(event);
    }
}
