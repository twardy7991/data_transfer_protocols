package org.twardy7991.protocolsimulator.Protocols;

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

    public AbstractSenderProtocol(Util util) {
        super(util);
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

        public void startTimer(int packet){
            final Runnable timer = new Runnable() {
                @Override
                public void run() {

                    logger.log(DEBUG, "TIMER: Timed out for packet %s".formatted(Integer.toBinaryString(packet)));
                    eventQueue.add(new TimeoutEvent(packet));
                }
            };

            final ScheduledFuture<?> beeperHandle = scheduler.schedule(timer, 7000, TimeUnit.MILLISECONDS);
            logger.log(DEBUG, "TIMER: Timer started for packet %s".formatted(Integer.toBinaryString(packet)));
            timers.put(packet, beeperHandle);
        }

        public void stopTimer(int packet){
            if (timers.get(packet) != null) {
                logger.log(DEBUG, "TIMER: Timer stopped for packet %s".formatted(Integer.toBinaryString(packet)));
                timers.get(packet).cancel(true);
                timers.remove(packet);
            }
        }

        public void shutdown(){
            for (int k : this.timers.keySet()){
                timers.get(k).cancel(true);
                logger.log(DEBUG, "TIMER: Cancelling timer");
            }

            scheduler.shutdown();
        }
    }
}


