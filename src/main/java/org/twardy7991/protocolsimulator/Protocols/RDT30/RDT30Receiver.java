package org.twardy7991.protocolsimulator.Protocols.RDT30;

import org.twardy7991.protocolsimulator.Pipe.Pipe;
import org.twardy7991.protocolsimulator.Protocols.AbstractReceiverProtocol;
import org.twardy7991.protocolsimulator.Protocols.Events.Event;
import org.twardy7991.protocolsimulator.Protocols.Events.NewPacketEvent;
import org.twardy7991.protocolsimulator.Util;
import static java.lang.System.Logger.Level.*;

public class RDT30Receiver extends AbstractReceiverProtocol {
    private int expectedSequence = 0;

    public RDT30Receiver(Pipe pipe) {
        super(pipe);
    }

    @Override
    public void rdt_receive(byte[] packet) throws InterruptedException {
        byte response;

        if (Util.isEqualChecksum(packet[0], packet[1])){

            if (packet[2] == expectedSequence){
                byte data = Util.extract(packet);
                logger.log(DEBUG, "RECEIVER: checksum is correct %s".formatted(Integer.toBinaryString((packet[1] ^ packet[0] & 0xFF) & 0xFF)));
                this.deliverData(data);
                response = (byte) expectedSequence;

                synchronized (this){
                    this.expectedSequence = (this.expectedSequence + 1) % 2;
                    logger.log(DEBUG, "RECEIVER: sequence %x".formatted(this.expectedSequence));
                }

            } else {
                logger.log(DEBUG, "RECEIVER: wrong packet sequence %s".formatted(Integer.toBinaryString(packet[2] & 0xFF)));
                response = (byte) ((this.expectedSequence + 1) % 2);
            }
        } else {
            response = (byte) ((this.expectedSequence + 1) % 2);
            logger.log(DEBUG, "RECEIVER: checksum is wrong %s".formatted(Integer.toBinaryString((packet[1] ^ packet[0] & 0xFF) & 0xFF)));
        }

        byte checksum = Util.calculateChecksum(response);
        byte[] sndpkt = Util.make_pkt(new byte[]{response}, checksum);
        pipe.udt_send(sndpkt, "sender", true, true, true);
    }

    @Override
    protected void handleEventQueue() throws InterruptedException {
        Event event = eventQueue.take();
        handlePacketEvent((NewPacketEvent) event);
    }
}
