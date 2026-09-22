package org.twardy7991.protocolsimulator.Protocols.RDT10;

import org.twardy7991.protocolsimulator.Protocols.AbstractSenderProtocol;
import org.twardy7991.protocolsimulator.Protocols.Events.NewPacketEvent;
import org.twardy7991.protocolsimulator.Protocols.SenderProtocol;
import org.twardy7991.protocolsimulator.Util;

public class RDT10Sender extends AbstractSenderProtocol implements SenderProtocol {

    public RDT10Sender(Util util) {
        super(util);
    }

    @Override
    protected void handleEventQueue() throws InterruptedException {
        NewPacketEvent event = (NewPacketEvent) this.eventQueue.take();
        this.handlePacketEvent(event);
    }

    private void handlePacketEvent(NewPacketEvent responseEvent) throws InterruptedException {
        this.rdt_send(responseEvent.packet(), "receiver");
    }

    @Override
    public void rdt_receive(byte[] packet) {
    }

    @Override
    public void rdt_send(byte[] packet, String address) throws InterruptedException {
        this.util.udt_send(packet, address, false, false);
    }
}
