package org.twardy7991.protocolsimulator.Protocols.GBN;

import org.twardy7991.protocolsimulator.Protocols.*;
import com.protocolsimulator.Protocols.Events.*;
import org.Protocols.Events.*;
import org.example.Protocols.Events.*;
import org.protocolsimulator.Protocols.Events.*;
import org.twardy7991.protocolsimulator.Message;
import org.twardy7991.protocolsimulator.Protocols.Events.*;
import org.twardy7991.protocolsimulator.Util;
//import org.jetbrains.annotations.NotNull;
import static java.lang.System.Logger.Level.*;

import java.util.concurrent.ConcurrentLinkedDeque;

public class GBNSender extends AbstractSenderProtocol {

    private final ConcurrentLinkedDeque<Message> sndQueue = new ConcurrentLinkedDeque<>();
    private final Sndpkt sndpkt;
    private final static int N = 3;
    private int base = 1;
    private int nextseqnum = 1;

    public GBNSender(Util util, Sndpkt sndpkt) {
        super(util);
        this.sndpkt = sndpkt;
    }

    public boolean isFull(){
        return nextseqnum >= base + N;
    }

    @Override
    public void rdt_send(byte[] data, String address) throws InterruptedException {
        if (nextseqnum < base + N){
            byte checksum = Util.calculateChecksum(data[0]);
            byte[] packet = Util.make_pkt(data, checksum, (byte) nextseqnum);
            sndpkt.add(packet);

            logger.log(DEBUG, "SENDER: data sent, packet data: %s".formatted(Integer.toBinaryString(packet[0] & 0xFF)));
            util.udt_send(packet, "receiver", true, true);

            if (base == nextseqnum){
                timer.startTimer(base);
            }
            nextseqnum++;
        }
    }

    @Override
    public void rdt_receive(byte[] packet) {

        if (!Util.isEqualChecksum(packet[0], packet[1])) {
            logger.log(DEBUG, "SENDER: Wrong checksum, packet data %s".formatted(Integer.toBinaryString(packet[0] & 0xFF)));
        } else {
            logger.log(DEBUG, "SENDER: ACK with correct checksum received, packet data %s".formatted(Integer.toBinaryString(packet[0] & 0xFF)));
            this.timer.stopTimer(base);

            this.sndpkt.delete(packet[0] + 1 - base);

            for (int i = 0;  i < packet[0] + 1 - base; i++){
                this.eventQueue.add(new SendNewPacketEvent());
            }

            this.base = packet[0] + 1;

            if (!(this.base == nextseqnum)){
                this.timer.startTimer(this.base);
            }
            //System.out.println("SENDER: sequence %x".formatted(this.currentSequence));
        }
    }

    @Override
    protected void handleEventQueue() throws InterruptedException {
        Event event = eventQueue.take();

        switch (event){
            case NewMessageEvent m -> this.handleMessageEvent(m);
            case NewPacketEvent r -> this.handlePacketEvent(r);
            case SendNewPacketEvent _ -> this.handleSendNewPacketEvent();
            case TimeoutEvent t -> this.handleTimeoutEvent(t);
            default -> System.out.println();
        }
    }

    private void handleTimeoutEvent(TimeoutEvent timeoutEvent) throws InterruptedException {

        if (timeoutEvent.packet() >= base) {
            this.timer.stopTimer(timeoutEvent.packet());

            logger.log(DEBUG, "SENDER: sndpkt size %s".formatted(sndpkt.size()));

            for (byte[] packet : sndpkt) {
                logger.log(DEBUG, "SENDER: Packet timeout, packet send again, packet data: %s".formatted(Integer.toBinaryString(packet[0] & 0xFF)));
                util.udt_send(packet, "receiver", true, true);
            }

            this.timer.startTimer(timeoutEvent.packet());
        } else {
            logger.log(DEBUG, "SENDER: Packet timeout for %x, base %s, no data send".formatted(timeoutEvent.packet(), base));
        }
    }

    private void handleSendNewPacketEvent() throws InterruptedException {
        Message message = this.sndQueue.poll();
        if (message != null) {
            this.rdt_send(message.packet(), message.address());
        }
    }

    private void handlePacketEvent(NewPacketEvent responseEvent){
        this.rdt_receive(responseEvent.packet());
    }

    private void handleMessageEvent(NewMessageEvent messageEvent) throws InterruptedException {
        if (!this.isFull()){
            this.rdt_send(messageEvent.data(), messageEvent.address());
        } else {
            sndQueue.add(new Message(messageEvent.data(), messageEvent.address()));
        }
    }

    @Override
    protected void shutdown(){
        this.timer.shutdown();
    }
}

