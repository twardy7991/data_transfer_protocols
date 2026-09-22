package org.twardy7991.protocolsimulator.Protocols.RDT30;

import org.twardy7991.protocolsimulator.Protocols.*;
import com.protocolsimulator.Protocols.Events.*;
import org.Protocols.Events.*;
import org.example.Protocols.Events.*;
import org.protocolsimulator.Protocols.Events.*;
import org.twardy7991.protocolsimulator.Message;
import org.twardy7991.protocolsimulator.Protocols.Events.*;
import org.twardy7991.protocolsimulator.Util;
import static java.lang.System.Logger.Level.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RDT30Sender extends AbstractSenderProtocol {

    private int currentSequence = 0;
    private volatile boolean waitACKNAK = false;

    private byte[] currPacket;
    private final BlockingQueue<Message> sndQueue = new LinkedBlockingQueue<>();

    public RDT30Sender(Util util) {
        super(util);
    }

    @Override
    public void rdt_send(byte[] data, String address) throws InterruptedException {
        byte checksum = Util.calculateChecksum(data[0]);
        this.currPacket = Util.make_pkt(data, checksum, (byte) this.currentSequence);

        logger.log(DEBUG, "SENDER: data sent: %s".formatted(Integer.toBinaryString(this.currPacket[0] & 0xFF)));
        util.udt_send(currPacket, address, true, true);
        this.timer.startTimer(this.currentSequence);

        this.waitACKNAK = true;
    }

    @Override
    public void rdt_receive(byte[] packet) throws InterruptedException {
        if (!Util.isEqualChecksum(packet[0], packet[1])) {
            logger.log(DEBUG, "SENDER: Wrong checksum, data sent again: %s".formatted(Integer.toBinaryString(this.currPacket[0] & 0xFF)));
            util.udt_send(this.currPacket, "receiver", true, true);

        } else if (packet[0] != this.currentSequence){
            logger.log(DEBUG, "SENDER: wrong ACK received, data sent again: %s".formatted(Integer.toBinaryString(this.currPacket[0] & 0xFF)));
            util.udt_send(this.currPacket, "receiver", true, true);

        } else {
            logger.log(DEBUG, "SENDER: ACK with correct checksum received");
            this.timer.stopTimer(this.currentSequence);

            synchronized (this) {
                this.currentSequence = (this.currentSequence + 1) % 2;
                logger.log(DEBUG, "SENDER: sequence %x".formatted(this.currentSequence));
            }
            this.waitACKNAK = false;
            this.eventQueue.add(new SendNewPacketEvent());
        }
    }

    @Override
    protected void handleEventQueue() throws InterruptedException {
        Event event = eventQueue.take();

        switch (event){
            case NewMessageEvent m -> this.handleMessageEvent(m);
            case NewPacketEvent r -> this.handlePacketEvent(r);
            case SendNewPacketEvent s -> this.handleSendNewPacketEvent();
            case TimeoutEvent t -> this.handleTimeoutEvent(t);
            default -> System.out.println();
        }
    }

    private void handleTimeoutEvent(TimeoutEvent timeoutEvent) throws InterruptedException {
        this.timer.stopTimer(timeoutEvent.packet());
        logger.log(DEBUG, "SENDER: Packet timeout, packet send again: %s".formatted(Integer.toBinaryString(this.currPacket[0] & 0xFF)));
        util.udt_send(this.currPacket, "receiver", true, true);
        this.timer.startTimer(timeoutEvent.packet());
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
