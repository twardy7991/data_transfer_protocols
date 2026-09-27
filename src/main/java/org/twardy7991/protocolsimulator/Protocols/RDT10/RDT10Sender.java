package org.twardy7991.protocolsimulator.Protocols.RDT10;

import org.twardy7991.protocolsimulator.Protocols.AbstractSenderProtocol;
import org.twardy7991.protocolsimulator.Protocols.Events.Event;
import org.twardy7991.protocolsimulator.Protocols.Events.NewMessageEvent;
import org.twardy7991.protocolsimulator.Protocols.Events.NewPacketEvent;
import org.twardy7991.protocolsimulator.Protocols.SenderProtocol;
import org.twardy7991.protocolsimulator.Pipe.Pipe;

import static java.lang.System.Logger.Level.DEBUG;

public class RDT10Sender extends AbstractSenderProtocol implements SenderProtocol {

    public RDT10Sender(Pipe pipe) {
        super(pipe);
    }

    @Override
    protected void handleEventQueue() throws InterruptedException {
        this.handleNewMessageEvent((NewMessageEvent) this.eventQueue.take());
    }

    private void handleNewMessageEvent(NewMessageEvent responseEvent) throws InterruptedException {
        this.rdt_send(responseEvent.data(), responseEvent.address());
    }

    @Override
    public void rdt_receive(byte[] packet) {
    }

    @Override
    public void rdt_send(byte[] packet, String address) throws InterruptedException {
        this.pipe.udt_send(packet, address, false, false, true);
        logger.log(DEBUG, "SENDER: data sent: %s".formatted(Integer.toBinaryString(packet[0] & 0xFF)));
    }
}
