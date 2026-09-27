package org.twardy7991.protocolsimulator.Pipe;

import org.twardy7991.protocolsimulator.Pipe.PipeEvents.MessageReadyToDeliverPipeEvent;
import org.twardy7991.protocolsimulator.Protocols.Protocol;

import java.util.Optional;

import static java.lang.System.Logger.Level.DEBUG;

public class UnorderedPipe extends AbstractPipe {

    private final Timer timer;

    public UnorderedPipe(Timer timer){
        this.timer = timer;
        this.timer.setPipe(this);
    }

    @Override
    protected long generateDelay(String address){
        return this.generateRandomDelay();
    }

    @Override
    protected void handleDeliverMessageEvent(MessageReadyToDeliverPipeEvent event) {
        Optional<Protocol> socket;
        socket = Optional.ofNullable(sockets.get(event.address()));

        if (socket.isPresent()) {
            socket.get().deliverMessage(event.packet());
        } else {
            logger.log(DEBUG, "could not find specified address %s%n", event.address());
        }
    }

    @Override
    protected void passMessage(byte[] packet, String address, long delay) throws InterruptedException {
        this.timer.startTimer(packet,address, delay);
        logger.log(DEBUG, "PIPE: starting new timer with packet: %s, delay: %s, address: %s".formatted(
            Integer.toBinaryString(packet[0] & 0xFF),
            delay,
            address
        ));
    }

    @Override
    public void stop() {
        this.running = false;
        this.timer.shutdown();
    }

}
