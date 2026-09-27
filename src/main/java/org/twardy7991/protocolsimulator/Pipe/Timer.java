package org.twardy7991.protocolsimulator.Pipe;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.System.Logger.Level.DEBUG;

public class Timer {
    protected final System.Logger logger = System.getLogger("org.twardy7991.protocolsimulator");
    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    protected Pipe pipe;

    public void shutdown() {scheduler.shutdown();}

    public void setPipe(Pipe pipe){
        this.pipe = pipe;
    }

    public ScheduledFuture<?> startTimer(byte[] packet, String address, long delay) throws InterruptedException {

        final Runnable timer = new Runnable() {
            @Override
            public void run() {
            logger.log(DEBUG, "PIPE TIMER: passing packet %s to %s".formatted(Integer.toBinaryString(packet[0] & 0xFF), address));
            pipe.addReadyPacket(packet, address);
            }
        };

        ScheduledFuture<?> firstTimer = scheduler.schedule(timer, delay, TimeUnit.MILLISECONDS);
        logger.log(DEBUG, "PIPE TIMER: Timer started for packet %s, address: %s, with delay %s s".formatted(Integer.toBinaryString(packet[0] & 0xFF), address, delay / 1000));

        return firstTimer;
    }
}