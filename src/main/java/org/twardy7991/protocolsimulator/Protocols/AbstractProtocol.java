package org.twardy7991.protocolsimulator.Protocols;

import org.twardy7991.protocolsimulator.Pipe.Pipe;
import org.twardy7991.protocolsimulator.Protocols.Events.Event;
import org.twardy7991.protocolsimulator.Util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/*
abstract class for both senders and receivers
 */
public abstract class AbstractProtocol implements Protocol {

    protected static System.Logger logger = System.getLogger("org.twardy7991.protocolsimulator");
    private volatile boolean running;

    protected Pipe pipe;
    protected BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();

    public AbstractProtocol(Pipe pipe){
        this.pipe = pipe;
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
            } catch (InterruptedException e) {
                this.shutdown();
            }
        }
    }

    protected void shutdown(){};

    /**
     * Handles all incoming events that are placed in queue
     * @throws InterruptedException
     */
    protected abstract void handleEventQueue() throws InterruptedException;
}
