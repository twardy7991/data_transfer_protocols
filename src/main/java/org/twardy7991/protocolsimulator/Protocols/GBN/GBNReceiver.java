package org.twardy7991.protocolsimulator.Protocols.GBN;

import org.twardy7991.protocolsimulator.Protocols.AbstractReceiverProtocol;
import org.twardy7991.protocolsimulator.Protocols.Events.Event;
import org.twardy7991.protocolsimulator.Protocols.Events.NewPacketEvent;
import org.twardy7991.protocolsimulator.Util;
import static java.lang.System.Logger.Level.*;

public class GBNReceiver extends AbstractReceiverProtocol {
    private int expectedSequence = 1;
    private volatile byte[] sndpkt = Util.make_pkt(new byte[]{0x0}, Util.calculateChecksum((byte)0));

    public GBNReceiver(Util util) {
        super(util);
    }

    @Override
    public void rdt_receive(byte[] packet) throws InterruptedException {
        byte response;

        if (Util.isEqualChecksum(packet[0], packet[1]) && packet[2] == expectedSequence) {
            byte data = Util.extract(packet);
            logger.log(DEBUG, "RECEIVER: checksum is correct %s, sequence %s".formatted(
                    Integer.toBinaryString((packet[1] ^ packet[0] & 0xFF) & 0xFF),
                    Integer.toBinaryString(packet[0] & 0xFF)
            ));

            this.deliverData(data);
            response = (byte) expectedSequence;

            byte checksum = Util.calculateChecksum(response);
            this.sndpkt = Util.make_pkt(new byte[]{response}, checksum);
            util.udt_send(this.sndpkt, "sender", true, true);

            expectedSequence++;
        } else {
            if ((Util.isEqualChecksum(packet[0], packet[1]) && !(packet[2] == expectedSequence))){
                logger.log(DEBUG, "RECEIVER: checksum is correct, sequence is wrong, expected %x, got %x".formatted(
                        expectedSequence, packet[2]
                ));
            }
            util.udt_send(this.sndpkt, "sender", true, true);
        }
    }

    @Override
    protected void handleEventQueue() throws InterruptedException {
        Event event = eventQueue.take();
        handlePacketEvent((NewPacketEvent) event);
    }
}

