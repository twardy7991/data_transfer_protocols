package org.twardy7991.protocolsimulator.Pipe.PipeEvents;

public record PacketInfo(byte[] packet, long delay, String address) {}
