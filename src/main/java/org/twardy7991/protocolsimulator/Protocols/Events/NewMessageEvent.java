package org.twardy7991.protocolsimulator.Protocols.Events;

public record NewMessageEvent(byte[] data, String address) implements Event {};
