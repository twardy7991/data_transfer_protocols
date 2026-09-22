package org.twardy7991.protocolsimulator;

import static java.lang.System.Logger.Level.*;

public class Util {

    private final Pipe pipe;
    private final System.Logger logger = System.getLogger("org.example");

    public Util(Pipe pipe){
        this.pipe = pipe;
    }

    public static byte[] make_pkt(byte[] data){
        return new byte[]{data[0]};
    }

    public static byte[] make_pkt(byte[] data, byte... args){
        byte[] packet = new byte[args.length + 1];
        packet[0] = data[0];

        System.arraycopy(args, 0, packet, 1, args.length);

        return packet;
    }

    public void udt_send(byte[] packet, String address, boolean canCorrupt, boolean canLose) throws InterruptedException {
        float min = 1;
        float max = 15;
        int random = (int)(max * Math.random() + min);

        if (random > 2 || !canCorrupt){
            logger.log(DEBUG, "PIPE: Clean pass of packet, packet data %x".formatted(packet[0]));
            pipe.cleanPassMessage(packet, address);
        } else if (random == 1){
            logger.log(DEBUG, "PIPE: Corrupted pass of packet, packet data %x".formatted(packet[0]));
            pipe.corruptPassMessage(packet, address);
        } else {
            logger.log(DEBUG, "PIPE: Loss of packet,  packet data %x".formatted(packet[0]));
        }
    }

    public static byte extract(byte[] packet){
        return packet[0];
    }

    public static byte calculateChecksum(byte data){
        return (byte) ~data;
//        return (byte) (data ^ 0xFF);
    }

    public static boolean isEqualChecksum(byte data, byte checksum){
        return ((checksum & 0xFF) ^ (data & 0xFF)) == 0xFF;
    }


}
