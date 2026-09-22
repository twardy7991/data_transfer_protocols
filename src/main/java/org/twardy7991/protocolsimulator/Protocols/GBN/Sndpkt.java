package org.twardy7991.protocolsimulator.Protocols.GBN;

import java.util.ArrayDeque;
import java.util.Iterator;

public class Sndpkt implements Iterable<byte[]> {

    private int N;

    public Sndpkt(int N){
        this.N = N;
    }

    private int counter = 0;

    private final ArrayDeque<byte[]> sndpkt = new ArrayDeque<>(N);

    //@NotNull
    @Override
    public Iterator<byte[]> iterator() {
        return sndpkt.iterator();
    }

    public boolean add(byte[] packet){
        if (counter < N){
            sndpkt.add(packet);
            counter++;
            return true;
        }
        return false;
    };

    public void delete(int i){
        if (i <= counter){
            for (int j = 0; j < i; j++){
                sndpkt.remove();
            }
            counter = counter - i;
            return;
        }
        throw new NotEnoughPacketsException("request for deletion of %x packets, but sndpkt contains only %x".formatted(i, sndpkt.size()));
    };

    public int size(){
        return this.sndpkt.size();
    }
}
