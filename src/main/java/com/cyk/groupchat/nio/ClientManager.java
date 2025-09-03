package com.cyk.groupchat.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientManager {

    // 房间->客户端列表
    private static final ConcurrentMap<String, Set<SocketChannel>> ROOMS = new ConcurrentHashMap<>();

    // 客户端->房间列表
    private static final ConcurrentMap<SocketChannel, Set<String>> CLIENTS = new ConcurrentHashMap<>();

    private static final String DEFAULT_ROOM = "default";

    static {
        ROOMS.put(DEFAULT_ROOM, ConcurrentHashMap.newKeySet());
    }

    public static void addClient(SocketChannel client) {
        joinRoom(DEFAULT_ROOM, client);
    }

    public static void removeClient(SocketChannel client) {
        quitRoom(DEFAULT_ROOM, client);
    }

    public static void joinRoom(String roomName, SocketChannel client) {
        try {
            System.out.println(client.getRemoteAddress().toString().substring(1) + " join room " + roomName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CLIENTS.computeIfAbsent(client, k -> ConcurrentHashMap.newKeySet()).add(roomName);
        ROOMS.computeIfAbsent(roomName, k -> ConcurrentHashMap.newKeySet()).add(client);
    }

    public static void quitRoom(String roomName, SocketChannel client) {
        try {
            System.out.println(client.getRemoteAddress().toString().substring(1) + " quit room " + roomName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<String> rooms = CLIENTS.get(client);
        if (rooms != null) {
            rooms.remove(roomName);
            if (rooms.isEmpty()) {
                CLIENTS.remove(client);
            }
        }
        Set<SocketChannel> roomClients = ROOMS.get(roomName);
        if (roomClients != null) {
            roomClients.remove(client);
            if (roomClients.isEmpty()) {
                ROOMS.remove(roomName);
            }
        }
    }

    public static void broadcast(SocketChannel sender, String message) {
        Set<String> rooms = CLIENTS.get(sender);
        if (rooms == null || rooms.isEmpty()) {
            return;
        }
        for (String room : rooms) {
            ROOMS.get(room).forEach(client -> {
                if (client != sender && client.isOpen()) {
                    try {
                        client.write(ByteBuffer.wrap(message.getBytes()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
