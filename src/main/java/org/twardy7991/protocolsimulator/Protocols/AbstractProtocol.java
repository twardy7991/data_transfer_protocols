package org.twardy7991.protocolsimulator.Protocols;

import org.twardy7991.protocolsimulator.Protocols.Events.Event;
import org.twardy7991.protocolsimulator.Util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractProtocol implements Protocol {

    protected static System.Logger logger = System.getLogger("org.example");
    private volatile boolean running;

    protected Util util;
    protected BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();

    public AbstractProtocol(Util util){
        this.util = util;
    }

    @Override
    public void stop(){
        this.running = false;
    }

    @Override
    public void run(){
        this.running = true;

        while (running){
            try {
                this.handleEventQueue();
            } catch (InterruptedException _) {
                this.shutdown();
            }
        }
    }

    protected void shutdown(){};

    protected abstract void handleEventQueue() throws InterruptedException;
}
