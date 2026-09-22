package org.twardy7991.protocolsimulator;
import org.twardy7991.protocolsimulator.Protocols.GBN.GBNReceiver;
import org.twardy7991.protocolsimulator.Protocols.GBN.GBNSender;
import org.twardy7991.protocolsimulator.Protocols.GBN.Sndpkt;

import static java.lang.System.Logger.Level.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.Logger logger = System.getLogger("org.example");
        logger.log(INFO, "starting simulation");

        Pipe pipe = new Pipe();
        Util util = new Util(pipe);

//        RDT10 sender = new RDT10(util);
//        RDT10 receiver = new RDT10(util);

//        RDT20Sender sender = new RDT20Sender(util);
//        RDT20Receiver receiver = new RDT20Receiver(util);

//        RDT21Sender sender = new RDT21Sender(util);
//        RDT21Receiver receiver = new RDT21Receiver(util);
//
//        RDT30Sender sender = new RDT30Sender(util);
//        RDT30Receiver receiver = new RDT30Receiver(util);

        GBNSender sender = new GBNSender(util, new Sndpkt(3));
        GBNReceiver receiver = new GBNReceiver(util);

        pipe.subscribe("receiver", receiver);
        pipe.subscribe("sender", sender);
        Simulation simulation = new Simulation(sender, receiver, pipe);
        Thread simulationThread = new Thread(simulation);

        simulationThread.start();
        simulationThread.join();
    }
}