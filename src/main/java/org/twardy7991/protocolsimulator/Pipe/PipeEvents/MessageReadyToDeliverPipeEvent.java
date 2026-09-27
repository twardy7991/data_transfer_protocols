package org.twardy7991.protocolsimulator.Pipe.PipeEvents;

public record MessageReadyToDeliverPipeEvent(byte[] packet, String address) implements PipeEvent {}
