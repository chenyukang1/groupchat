/*
 * Copyright (c) 2015-2025，千寻位置网络有限公司版权所有。
 *
 * 时空智能 共创数字中国（厘米级定位 | 毫米级感知 | 纳秒级授时）
 */
package com.cyk.groupchat.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * @author yukang.chen
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ClientManager.addClient(ctx);
        System.out.println("Client connected: " + ctx.channel().remoteAddress().toString().substring(1));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ClientManager.removeClient(ctx);
        System.out.println("Client disconnected: " + ctx.channel().remoteAddress().toString().substring(1));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        String message = buf.toString(CharsetUtil.UTF_8);
        System.out.println("Received: " + message);
        if (message.startsWith("/join ")) {
            String room = message.substring(6).trim();
            ClientManager.joinRoom(room, ctx);
        } else if (message.startsWith("/quit ")) {
            String room = message.substring(6).trim();
            ClientManager.quitRoom(room, ctx);
        } else {
            String broadcastMsg = "[" + ctx.channel().remoteAddress().toString().substring(1) + "] :" + message;
            ClientManager.broadcast(ctx, broadcastMsg);
        }
    }
}
