package org.twardy7991.protocolsimulator.Protocols;

import org.twardy7991.protocolsimulator.Protocols.Events.NewPacketEvent;
import org.twardy7991.protocolsimulator.Util;

import static java.lang.System.Logger.Level.*;

public abstract class AbstractReceiverProtocol extends AbstractProtocol {

    private int expectedPacket = 1;

    public AbstractReceiverProtocol(Util util) {
        super(util);
    }

    @Override
    public void deliverMessage(byte[] packet) {
        this.eventQueue.add(new NewPacketEvent(packet));
    }

    protected void deliverData(byte data) {

        if (expectedPacket != data){
            throw new RuntimeException("expected packet %x, got packet %s".formatted(expectedPacket, Integer.toBinaryString(data & 0xFF)));
        } else {
            logger.log(DEBUG, "RECEIVER: data delivered: %s".formatted(Integer.toBinaryString(data & 0xFF)));
            expectedPacket++;
        }

        if (data == 4){
            logger.log(DEBUG, "RECEIVER: Stopping");
            this.stop();
        }
    }

    protected void handlePacketEvent(NewPacketEvent packetEvent) throws InterruptedException {
        this.rdt_receive(packetEvent.packet());
    }
}
