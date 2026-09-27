package org.twardy7991.protocolsimulator;
import org.twardy7991.protocolsimulator.Pipe.OrderedPipe;
import org.twardy7991.protocolsimulator.Pipe.Pipe;
import org.twardy7991.protocolsimulator.Pipe.UnorderedPipe;
import org.twardy7991.protocolsimulator.Protocols.GBN.GBNReceiver;
import org.twardy7991.protocolsimulator.Protocols.GBN.GBNSender;
import org.twardy7991.protocolsimulator.Protocols.GBN.Sndpkt;
import org.twardy7991.protocolsimulator.Pipe.Timer;
import org.twardy7991.protocolsimulator.Protocols.Protocol;
import org.twardy7991.protocolsimulator.Protocols.ProtocolType;
import org.twardy7991.protocolsimulator.Protocols.RDT10.RDT10Receiver;
import org.twardy7991.protocolsimulator.Protocols.RDT10.RDT10Sender;
import org.twardy7991.protocolsimulator.Protocols.RDT20.RDT20Receiver;
import org.twardy7991.protocolsimulator.Protocols.RDT20.RDT20Sender;
import org.twardy7991.protocolsimulator.Protocols.RDT21.RDT21Receiver;
import org.twardy7991.protocolsimulator.Protocols.RDT21.RDT21Sender;
import org.twardy7991.protocolsimulator.Protocols.RDT30.RDT30Receiver;
import org.twardy7991.protocolsimulator.Protocols.RDT30.RDT30Sender;
import org.twardy7991.protocolsimulator.Protocols.SenderProtocol;

import java.util.logging.*;

import static java.lang.System.Logger.Level.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.Logger logger = System.getLogger("org.twardy7991.protocolsimulator");

        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.FINEST);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.FINEST);

            handler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return "%s%n".formatted(
                            record.getMessage()
                    );
                }
            });
        }

        logger.log(INFO, "starting simulation");

        ProtocolType protocol = ProtocolType.RDT10;

        Timer senderToReceiverTimer = new Timer();
        Timer receiverToSenderTimer = new Timer();

        Pipe pipe;
        SenderProtocol sender;
        Protocol receiver;

        switch (protocol) {
            case RDT10 -> {
                pipe = new OrderedPipe(senderToReceiverTimer, receiverToSenderTimer);
                sender = new RDT10Sender(pipe);
                receiver = new RDT10Receiver(pipe);
            }
            case RDT20 -> {
                pipe = new OrderedPipe(senderToReceiverTimer, receiverToSenderTimer);
                sender = new RDT20Sender(pipe);
                receiver = new RDT20Receiver(pipe);
            }
            case RDT21 -> {
                pipe = new OrderedPipe(senderToReceiverTimer, receiverToSenderTimer);
                sender = new RDT21Sender(pipe);
                receiver = new RDT21Receiver(pipe);
            }
            case RDT30 -> {
                pipe = new OrderedPipe(senderToReceiverTimer, receiverToSenderTimer);
                sender = new RDT30Sender(pipe);
                receiver = new RDT30Receiver(pipe);
            }
            case GBN -> {
                Timer timer = new Timer();
                pipe = new UnorderedPipe(timer);
                sender = new GBNSender(pipe, new Sndpkt(3));
                receiver = new GBNReceiver(pipe);
            }
            default -> {
                logger.log(ERROR, "Unknown protocol type %s", protocol);
                pipe = new OrderedPipe(senderToReceiverTimer, receiverToSenderTimer);
                sender = new RDT10Sender(pipe);
                receiver = new RDT10Receiver(pipe);
            }
        }

        pipe.subscribe("receiver", receiver);
        pipe.subscribe("sender", sender);

        Simulation simulation = new Simulation(sender, receiver, pipe);
        Thread simulationThread = new Thread(simulation);

        simulationThread.start();
        simulationThread.join();
    }
}