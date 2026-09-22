package org.twardy7991.protocolsimulator.Protocols.RDT20;

import org.twardy7991.protocolsimulator.Protocols.AbstractSenderProtocol;
import org.twardy7991.protocolsimulator.Protocols.Events.Event;
import org.twardy7991.protocolsimulator.Protocols.Events.NewPacketEvent;
import org.twardy7991.protocolsimulator.Protocols.Events.NewMessageEvent;
import org.twardy7991.protocolsimulator.Protocols.Events.SendNewPacketEvent;
import org.twardy7991.protocolsimulator.Message;
import org.twardy7991.protocolsimulator.Protocols.SenderProtocol;
import org.twardy7991.protocolsimulator.Util;
import static java.lang.System.Logger.Level.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RDT20Sender extends AbstractSenderProtocol implements SenderProtocol {

    private volatile boolean waitACKNAK = false;

    private byte[] currPacket;
    private final BlockingQueue<Message> sndQueue = new LinkedBlockingQueue<>();

    public RDT20Sender(Util util) {
        super(util);
    }
    
    @Override
    public void rdt_send(byte[] data, String address) throws InterruptedException {
        byte checksum = Util.calculateChecksum(data[0]);
        this.currPacket = Util.make_pkt(data, checksum);

        logger.log(DEBUG, "data sent: %s".formatted(Integer.toBinaryString(this.currPacket[0] & 0xFF)));
        util.udt_send(currPacket, address, true, false);
        this.waitACKNAK = true;
    }

    @Override
    public void rdt_receive(byte[] packet) throws InterruptedException {
        if (packet[0] == 0){
        util.udt_send(this.currPacket, "receiver", false, false);
            logger.log(DEBUG, "data sent again: %s".formatted(Integer.toBinaryString(this.currPacket[0] & 0xFF)));
            return;
        }

        waitACKNAK = false;
        this.eventQueue.add(new SendNewPacketEvent());
    }

    @Override
    protected void handleEventQueue() throws InterruptedException {
        Event event = eventQueue.take();

        switch (event){
            case NewMessageEvent m -> this.handleMessageEvent(m);
            case NewPacketEvent r -> this.handlePacketEvent(r);
            case SendNewPacketEvent s -> this.handleSendNewPacketEvent();
            default -> System.out.println();
        }
    }

    private void handleSendNewPacketEvent() throws InterruptedException {
        Message message = this.sndQueue.take();
        this.rdt_send(message.packet(), message.address());
    }

    private void handlePacketEvent(NewPacketEvent responseEvent) throws InterruptedException {
        this.rdt_receive(responseEvent.packet());
    }

    private void handleMessageEvent(NewMessageEvent messageEvent) throws InterruptedException {
        if (!this.waitACKNAK){
            this.rdt_send(messageEvent.data(), messageEvent.address());
        } else {
            sndQueue.add(new Message(messageEvent.data(), messageEvent.address()));
        }
    }
}
