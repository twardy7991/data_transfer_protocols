package org.twardy7991.protocolsimulator.Pipe;

import org.twardy7991.protocolsimulator.Pipe.Timers.PipeEvents.MessageReadyToDeliverPipeEvent;
import org.twardy7991.protocolsimulator.Pipe.Timers.PipeEvents.NewMessagePipeEvent;
import org.twardy7991.protocolsimulator.Pipe.Timers.PipeEvents.PacketInfo;
import org.twardy7991.protocolsimulator.Pipe.Timers.PipeEvents.PipeEvent;
import org.twardy7991.protocolsimulator.Protocols.Protocol;
import org.twardy7991.protocolsimulator.Pipe.Timers.Timer;

import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.Logger.Level.*;

public abstract class AbstractPipe implements Pipe {

    protected final Map<String, Protocol> sockets = new HashMap<>();
    protected final BlockingQueue<PipeEvent> eventQueue = new LinkedBlockingQueue<>();

    protected final System.Logger logger = System.getLogger("org.twardy7991.protocolsimulator");
    protected boolean running = false;

    public void subscribe(String address, Protocol socket) {
        sockets.put(address, socket);
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            try {
                this.handleEventQueue();
            } catch (InterruptedException _) {
            }
        }
    }

    private void handleEventQueue() throws InterruptedException {
        PipeEvent event = eventQueue.take();

        switch (event){
            case MessageReadyToDeliverPipeEvent messageReadyEvent -> this.handleDeliverMessageEvent(messageReadyEvent);
            case NewMessagePipeEvent NewMessageEvent -> this.handleNewMessageEvent(
                    NewMessageEvent.packet(),
                    NewMessageEvent.address(),
                    NewMessageEvent.canCorrupt(),
                    NewMessageEvent.canLose(),
                    NewMessageEvent.isDelayed()
            );
            default -> throw new IllegalStateException("Unexpected value: " + event);
        };
    }

    protected long generateRandomDelay() {
        float min = 2;
        float max = 4;
        return (long) (max * Math.random() + min) * 1000;
    }

    protected void handleNewMessageEvent(byte[] packet, String address, boolean canCorrupt, boolean canLose, boolean isDelayed) throws InterruptedException {
        float min = 1;
        float max = 10;
        int random = (int) (max * Math.random() + min);

        long delay = isDelayed ? generateDelay(address) : 0;

        if (random < 3 && canCorrupt) {
            packet = this.corruptPacket(packet);
        } else if (random == 2 && canLose) {
            return;
        } else {
            logger.log(DEBUG, "PIPE: Clean pass of packet, packet data %s".formatted(Integer.toBinaryString(packet[0] & 0xFF)));
        }

        logger.log(DEBUG, "PIPE: Clean pass of packet, delay %s".formatted(delay));
        this.passMessage(packet, address, delay);
    }

    private byte[] corruptPacket(byte[] packet){
        float min = 0;
        float max = 7;
        byte[] corruptedPacket = packet.clone();
        byte mask = (byte) (0x01 << (int) (max * Math.random() + min));

        corruptedPacket[0] = (byte) (corruptedPacket[0] ^ mask);
        logger.log(DEBUG, "PIPE: corrupted packet, changed from %s to %s".formatted(
                Integer.toBinaryString(packet[0] & 0xFF),
                Integer.toBinaryString(corruptedPacket[0] & 0xFF)
        ));

        return corruptedPacket;
    }

    public void udt_send(byte[] packet, String address, boolean canCorrupt, boolean canLose, boolean isDelayed) throws InterruptedException {
        this.eventQueue.add(new NewMessagePipeEvent(packet, address, canCorrupt, canLose, isDelayed));
    }

    public void addReadyPacket(byte[] packet, String address){
        this.eventQueue.add(new MessageReadyToDeliverPipeEvent(packet, address));
    }

    protected abstract long generateDelay(String address);

    protected abstract void handleDeliverMessageEvent(MessageReadyToDeliverPipeEvent event);

    protected abstract void passMessage(byte[] packet, String address, long delay) throws InterruptedException;

    public abstract void stop();
}
