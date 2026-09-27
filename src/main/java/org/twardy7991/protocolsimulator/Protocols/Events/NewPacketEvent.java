package org.twardy7991.protocolsimulator.Protocols.Events;

public record NewPacketEvent(byte[] packet) implements Event {}