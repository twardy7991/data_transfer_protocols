package org.twardy7991.protocolsimulator.Pipe.PipeEvents;

public record NewMessagePipeEvent(byte[] packet, String address, boolean canCorrupt, boolean canLose, boolean isDelayed) implements PipeEvent{}
