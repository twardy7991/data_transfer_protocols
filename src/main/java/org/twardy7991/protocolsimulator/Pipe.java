package org.twardy7991.protocolsimulator;

import org.twardy7991.protocolsimulator.Protocols.Protocol;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static java.lang.System.Logger.Level.*;

public class Pipe implements Runnable{

    private record Message(String address, byte[] packet){};
    private final System.Logger logger = System.getLogger("org.example");
    private boolean running = false;
    private final Map<String, Protocol> sockets = new HashMap<>();
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

    public void subscribe(String address, Protocol socket){
        sockets.put(address, socket);
    }

    @Override
    public void run(){
        running = true;

        while (running){
            try {
                this.handleMessageQueue();
            } catch (InterruptedException _) {}
        }
    }

    private void handleMessageQueue() throws InterruptedException {
        Message  message;
        message = messageQueue.take();

        Optional<Protocol> socket;
        socket = Optional.ofNullable(sockets.get(message.address));

        if (socket.isPresent()){
            socket.get().deliverMessage(message.packet);
        } else {
            logger.log(DEBUG, "could not find specified address %s%n", message.address);
        }
    }

    private void generateRandomDelay() throws InterruptedException {
        float min = 2;
        float max = 4;
        int random = (int)(max * Math.random() + min);
        Thread.sleep(random * 1000L);

    }

    public void addMessage(String address, byte[] packet) throws InterruptedException{
        this.generateRandomDelay();
        this.messageQueue.add(new Message(address, packet));
    }

    public void cleanPassMessage(byte[] packet, String address) throws InterruptedException{
        addMessage(address, packet);
    }

    public void stop(){
        this.running = false;
    }

    public void corruptPassMessage(byte[] packet, String address) throws InterruptedException {
        float min = 0;
        float max = 7;
        byte[] corrupted_packet = packet.clone();
        byte mask = (byte) (0x01 << (int)(max * Math.random() + min));

        corrupted_packet[0] = (byte) (corrupted_packet[0] ^ mask);

        addMessage(address, corrupted_packet);
    }
}
