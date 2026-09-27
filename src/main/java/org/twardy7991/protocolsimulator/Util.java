package org.twardy7991.protocolsimulator;

import org.twardy7991.protocolsimulator.Pipe.Pipe;

public class Util {
    private final System.Logger logger = System.getLogger("org.example");

    public static byte[] make_pkt(byte[] data){
        return new byte[]{data[0]};
    }

    public static byte[] make_pkt(byte[] data, byte... args){
        byte[] packet = new byte[args.length + 1];
        packet[0] = data[0];

        System.arraycopy(args, 0, packet, 1, args.length);

        return packet;
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
