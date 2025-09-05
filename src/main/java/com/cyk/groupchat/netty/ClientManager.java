package com.cyk.groupchat.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientManager {

    // 房间->客户端列表
    private static final ConcurrentMap<String, Set<ChannelHandlerContext>> ROOMS = new ConcurrentHashMap<>();

    // 客户端->房间列表
    private static final ConcurrentMap<ChannelHandlerContext, Set<String>> CLIENTS = new ConcurrentHashMap<>();

    private static final String DEFAULT_ROOM = "default";

    static {
        ROOMS.put(DEFAULT_ROOM, ConcurrentHashMap.newKeySet());
    }

    public static void addClient(ChannelHandlerContext client) {
        joinRoom(DEFAULT_ROOM, client);
    }

    public static void removeClient(ChannelHandlerContext client) {
        quitRoom(DEFAULT_ROOM, client);
    }

    public static void joinRoom(String roomName, ChannelHandlerContext ctx) {
        System.out.println(ctx.channel().remoteAddress().toString().substring(1) + " join room " + roomName);
        CLIENTS.computeIfAbsent(ctx, k -> ConcurrentHashMap.newKeySet()).add(roomName);
        ROOMS.computeIfAbsent(roomName, k -> ConcurrentHashMap.newKeySet()).add(ctx);
    }

    public static void quitRoom(String roomName, ChannelHandlerContext ctx) {
        System.out.println(ctx.channel().remoteAddress().toString().substring(1) + " quit room " + roomName);
        Set<String> rooms = CLIENTS.get(ctx);
        if (rooms != null) {
            rooms.remove(roomName);
            if (rooms.isEmpty()) {
                CLIENTS.remove(ctx);
            }
        }
        Set<ChannelHandlerContext> roomClients = ROOMS.get(roomName);
        if (roomClients != null) {
            roomClients.remove(ctx);
            if (roomClients.isEmpty()) {
                ROOMS.remove(roomName);
            }
        }
    }

    public static void broadcast(ChannelHandlerContext sender, String message) {
        Set<String> rooms = CLIENTS.get(sender);
        if (rooms == null || rooms.isEmpty()) {
            return;
        }
        for (String room : rooms) {
            ROOMS.get(room).forEach(client -> {
                if (client != sender) {
                    try {
                        client.channel().writeAndFlush(client.alloc().buffer().writeBytes(message.getBytes(CharsetUtil.UTF_8)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
