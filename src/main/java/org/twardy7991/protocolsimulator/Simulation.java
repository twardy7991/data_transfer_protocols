package org.twardy7991.protocolsimulator;

import org.twardy7991.protocolsimulator.Protocols.Protocol;
import org.twardy7991.protocolsimulator.Protocols.SenderProtocol;

public class Simulation implements Runnable{

    private final SenderProtocol sender;
    private final Protocol receiver;
    private final Pipe pipe;

    public Simulation(SenderProtocol sender, Protocol receiver, Pipe pipe){
        this.sender = sender;
        this.receiver = receiver;
        this.pipe = pipe;
    }

    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("ctr c detected, cleaning up");
            sender.stop();
            receiver.stop();
            pipe.stop();
            System.out.println("finished simulation");
        }));

        int i = 1;

        Thread senderThread = new Thread(sender, "sender");
        Thread pipeThread =  new Thread(pipe, "pipe");
        Thread receiverThread = new Thread(receiver, "receiver");

        senderThread.start();
        pipeThread.start();
        receiverThread.start();

        while (i < 10){
            try  {
                Thread.sleep(500);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            int message = i;
//            int message = (int)(Math.random() * 1000) % 128;
            sender.sendMessage(new byte[]{(byte) message});
            i++;
        }

        try {
            receiverThread.join();

            sender.stop();
            senderThread.interrupt();
            senderThread.join();

            pipe.stop();
            pipeThread.interrupt();
            pipeThread.join();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
