package org.twardy7991.protocolsimulator.Protocols;

import org.twardy7991.protocolsimulator.Pipe.Pipe;
import org.twardy7991.protocolsimulator.Protocols.Events.NewMessageEvent;
import org.twardy7991.protocolsimulator.Protocols.Events.NewPacketEvent;
import org.twardy7991.protocolsimulator.Protocols.Events.TimeoutEvent;
import org.twardy7991.protocolsimulator.Util;

import static java.lang.System.Logger.Level.*;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSenderProtocol extends AbstractProtocol implements SenderProtocol {

    protected Timer timer = new Timer();

    public AbstractSenderProtocol(Pipe pipe) {
        super(pipe);
    }

    @Override
    public void sendMessage(byte[] data) {
        this.eventQueue.add(new NewMessageEvent(data, "receiver"));
    }

    @Override
    public void deliverMessage(byte[] packet) {
        this.eventQueue.add(new NewPacketEvent(packet));
    }

    protected class Timer {

        private final HashMap<Integer, ScheduledFuture<?>> timers = new HashMap<>();
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        public void startTimer(int sequence){
            final Runnable timer = new Runnable() {
                @Override
                public void run() {

                    logger.log(DEBUG, "SENDER TIMER: Timed out for sequence num %s".formatted(Integer.toBinaryString(sequence)));
                    eventQueue.add(new TimeoutEvent(sequence));
                }
            };

            final ScheduledFuture<?> beeperHandle = scheduler.schedule(timer, 7000, TimeUnit.MILLISECONDS);
            logger.log(DEBUG, "SENDER TIMER: Timer started for sequence num %s".formatted(Integer.toBinaryString(sequence)));
            timers.put(sequence, beeperHandle);
        }

        public void stopTimer(int sequence){
            if (timers.get(sequence) != null) {
                logger.log(DEBUG, "SENDER TIMER: Timer stopped for sequence %s".formatted(Integer.toBinaryString(sequence)));
                timers.get(sequence).cancel(true);
                timers.remove(sequence);
            }
        }

        public void shutdown(){
            for (int k : this.timers.keySet()){
                timers.get(k).cancel(true);
                logger.log(DEBUG, "SENDER TIMER: Cancelling timer");
            }

            scheduler.shutdown();
        }
    }
}


