package org.twardy7991.protocolsimulator.Protocols.RDT21;

import org.twardy7991.protocolsimulator.Protocols.AbstractReceiverProtocol;
import org.twardy7991.protocolsimulator.Protocols.Events.Event;
import org.twardy7991.protocolsimulator.Protocols.Events.NewPacketEvent;
import org.twardy7991.protocolsimulator.Util;
import static java.lang.System.Logger.Level.*;

public class RDT21Receiver extends AbstractReceiverProtocol {

    private int expectedSequence = 0;

    public RDT21Receiver(Util util) {
        super(util);
    }

    @Override
    public void rdt_receive(byte[] packet) throws InterruptedException {
        byte response;

        if (Util.isEqualChecksum(packet[0], packet[1])){

            if (packet[2] == expectedSequence){
                byte data = Util.extract(packet);
                logger.log(DEBUG, "RECEIVER: checksum is correct %s".formatted(Integer.toBinaryString((packet[1] ^ packet[0] & 0xFF) & 0xFF)));
                this.deliverData(data);
                response = 1;

                synchronized (this){
                    this.expectedSequence = (this.expectedSequence + 1) % 2;
                    logger.log(DEBUG, "RECEIVER: sequence %x".formatted(this.expectedSequence));
                }

            } else {
                logger.log(DEBUG, "RECEIVER: wrong packet sequence %s".formatted(Integer.toBinaryString(packet[2] & 0xFF)));
                response = 1;
            }
        } else {
            response = 0;
            logger.log(DEBUG, "RECEIVER: checksum is wrong %s".formatted(Integer.toBinaryString((packet[1] ^ packet[0] & 0xFF) & 0xFF)));
        }

        byte checksum = Util.calculateChecksum(response);
        byte[] sndpkt = Util.make_pkt(new byte[]{response}, checksum);
        util.udt_send(sndpkt, "sender", true, false);
    }

    @Override
    protected void handleEventQueue() throws InterruptedException {
        Event event = eventQueue.take();
        handlePacketEvent((NewPacketEvent) event);
    }
}
