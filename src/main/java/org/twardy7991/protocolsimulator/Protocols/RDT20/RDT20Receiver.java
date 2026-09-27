package org.twardy7991.protocolsimulator.Protocols.RDT20;

import org.twardy7991.protocolsimulator.Pipe.Pipe;
import org.twardy7991.protocolsimulator.Protocols.AbstractReceiverProtocol;
import org.twardy7991.protocolsimulator.Protocols.Events.Event;
import org.twardy7991.protocolsimulator.Protocols.Events.NewPacketEvent;
import org.twardy7991.protocolsimulator.Util;
import static java.lang.System.Logger.Level.*;

public class RDT20Receiver extends AbstractReceiverProtocol {

    public RDT20Receiver(Pipe pipe) {
        super(pipe);
    }

    @Override
    public void rdt_receive(byte[] packet) throws InterruptedException {
        byte[] sndpkt;

        if (Util.isEqualChecksum(packet[0], packet[1])){
            byte data = Util.extract(packet);
            sndpkt = Util.make_pkt(new byte[]{1});

            logger.log(DEBUG, "RECEIVER: checksum is correct %s".formatted(Integer.toBinaryString((packet[1] ^ packet[0] & 0xFF) & 0xFF)));
            this.deliverData(data);

        } else {
            sndpkt = Util.make_pkt(new byte[]{0});
            logger.log(DEBUG, "RECEIVER: checksum is wrong %s".formatted(Integer.toBinaryString((packet[1] ^ packet[0] & 0xFF) & 0xFF)));
        }

        pipe.udt_send(sndpkt, "sender", false, false, true);
    }

    @Override
    protected void handleEventQueue() throws InterruptedException {
        Event event = eventQueue.take();
        handlePacketEvent((NewPacketEvent) event);
    }
}