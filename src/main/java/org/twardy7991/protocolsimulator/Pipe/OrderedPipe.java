package org.twardy7991.protocolsimulator.Pipe;

import org.twardy7991.protocolsimulator.Pipe.PipeEvents.MessageReadyToDeliverPipeEvent;
import org.twardy7991.protocolsimulator.Pipe.PipeEvents.PacketInfo;
import org.twardy7991.protocolsimulator.Protocols.Protocol;

import java.util.*;
import java.util.concurrent.*;

import static java.lang.System.Logger.Level.*;

public class OrderedPipe extends AbstractPipe {

    protected final Map<String, Timer> timers;
    protected final Map<String, LinkedBlockingQueue<PacketInfo>> packetQueues = new HashMap<>();
    private final Map<String, ScheduledFuture<?>> firstTimers = new HashMap<>();

    public OrderedPipe(Timer senderToReceiverTimer, Timer receiverToSenderTimer){
        super();
        this.timers = new HashMap<>();
        senderToReceiverTimer.setPipe(this);
        receiverToSenderTimer.setPipe(this);

        timers.put("receiver", senderToReceiverTimer);
        timers.put("sender", receiverToSenderTimer);

        packetQueues.put("receiver", new LinkedBlockingQueue<>());
        packetQueues.put("sender", new LinkedBlockingQueue<>());

        firstTimers.put("receiver", null);
        firstTimers.put("sender", null);
    }

    public boolean hasPacket(String address){
        return !packetQueues.get(address).isEmpty();
    }

    public PacketInfo getFirstPacket(String address){
        return packetQueues.get(address).remove();
    }

    protected long generateDelay(String address){
        logger.log(DEBUG, "PIPE: GENERATE DELAY, %s, %s, %s ".formatted(generateRandomDelay() , getCurrentDelay(address), (getCurrentDelay(address) / 100) * 100));
        return Math.max(1000, (generateRandomDelay() - (getCurrentDelay(address) / 100) * 100));
    }

    protected void handleDeliverMessageEvent(MessageReadyToDeliverPipeEvent event) {
        Optional<Protocol> socket;
        socket = Optional.ofNullable(sockets.get(event.address()));

        if (socket.isPresent()) {
            socket.get().deliverMessage(event.packet());
        } else {
            logger.log(DEBUG, "could not find specified address %s%n", event.address());
        }

        if (this.hasPacket(event.address())) {
            PacketInfo newTimer;
            newTimer = this.getFirstPacket(event.address());
            try {
                firstTimers.put(event.address(), timers.get(event.address()).startTimer(newTimer.packet(), event.address(), newTimer.delay()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            firstTimers.put(event.address(), null);
        }
    }

    public void stop() {
        this.running = false;
        for (Timer timer : timers.values()){
            timer.shutdown();
        }
    }

    protected void passMessage(byte[] packet, String address, long delay) throws InterruptedException {
        if (firstTimers.get(address) != null){
            this.packetQueues.get(address).add(new PacketInfo(packet, delay, address));
            logger.log(DEBUG, "PIPE: adding packet to queue: %s, delay: %s, address: %s".formatted(
                    Integer.toBinaryString(packet[0] & 0xFF),
                    delay,
                    address
            ));
        } else {
            this.firstTimers.put(address, this.timers.get(address).startTimer(packet,address, delay));
            logger.log(DEBUG, "PIPE: starting new timer with packet: %s, delay: %s, address: %s".formatted(
                    Integer.toBinaryString(packet[0] & 0xFF),
                    delay,
                    address
            ));
        }
    }

    private long getCurrentDelay(String address){
        if (firstTimers.get(address) != null){
            return this.firstTimers.get(address).getDelay(TimeUnit.MILLISECONDS);
        }
        return 0;
    }
}
