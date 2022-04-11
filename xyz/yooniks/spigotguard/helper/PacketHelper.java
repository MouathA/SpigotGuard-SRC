package xyz.yooniks.spigotguard.helper;

import net.minecraft.server.v1_8_R3.*;
import java.io.*;

public class PacketHelper
{
    public static void checkLogin(final PacketDataSerializer packetDataSerializer) throws IOException {
        if (packetDataSerializer.readableBytes() > 40 || packetDataSerializer.readableBytes() < 5) {
            throw new IOException("Too long LoginStart");
        }
        final int e = packetDataSerializer.e();
        if (!packetDataSerializer.isReadable() || e != 0) {
            throw new IOException("Invalid LoginStart packet id");
        }
        final byte[] array = new byte[packetDataSerializer.e()];
        packetDataSerializer.readBytes(array);
        final String s = new String(array);
        if (packetDataSerializer.isReadable() || s.length() > 16 || s.length() <= 2) {
            throw new IOException("Invalid LoginStart packet");
        }
    }
    
    public static int checkHandshake(final PacketDataSerializer packetDataSerializer) throws IOException {
        if (packetDataSerializer.readableBytes() > 300 || packetDataSerializer.readableBytes() < 5) {
            throw new IOException("Invalid Handshake packet 1");
        }
        packetDataSerializer.e();
        if (!packetDataSerializer.isReadable()) {
            throw new IOException("Invalid Handshake packet 2");
        }
        final int e = packetDataSerializer.e();
        if (!packetDataSerializer.isReadable() || e <= 0) {
            throw new IOException("Invalid Handshake packet 3");
        }
        packetDataSerializer.readBytes(new byte[packetDataSerializer.e()]);
        if (packetDataSerializer.readableBytes() <= 2) {
            throw new IOException("Invalid Handshake packet 4");
        }
        final int unsignedShort = packetDataSerializer.readUnsignedShort();
        if (!packetDataSerializer.isReadable() || unsignedShort <= 0 || packetDataSerializer.readableBytes() > 1) {
            throw new IOException("Invalid Handshake packet 5");
        }
        final int e2 = packetDataSerializer.e();
        if (packetDataSerializer.isReadable() || (e2 != 1 && e2 != 2)) {
            throw new IOException("Invalid Handshake packet 6");
        }
        return e2;
    }
}
