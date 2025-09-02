package com.cyk.groupchat.nio;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {

    private static final Set<SocketChannel> clients = ConcurrentHashMap.newKeySet();

    public static void addClient(SocketChannel client) {
        clients.add(client);
    }

    public static void removeClient(SocketChannel channel) {
        clients.remove(channel);
    }

    public static void broadcast(SocketChannel sender, String message) {
        for (SocketChannel client : clients) {
            if (client != sender && client.isOpen()) {
                try {
                    client.write(ByteBuffer.wrap(message.getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
