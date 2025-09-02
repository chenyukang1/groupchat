package com.cyk.groupchat.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ReactorServer {

    private ServerSocketChannel server;

    private Selector bossSelector;

    private WorkerGroup workerGroup;

    public ReactorServer() {
        try {
            server = ServerSocketChannel.open();
            server.socket().bind(new InetSocketAddress(6777));
            server.configureBlocking(false);
            bossSelector = Selector.open();
            server.register(bossSelector, SelectionKey.OP_ACCEPT);

            workerGroup = new WorkerGroup(Runtime.getRuntime().availableProcessors());
        } catch (IOException e) {
            e.printStackTrace();
            try {
                server.close();
            } catch (IOException ex) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        System.out.println("Server start...");
        try {
            while (true) {
                int count = bossSelector.select();
                if (count == 0) {
                    continue;
                }
                // 有事件处理
                Iterator<SelectionKey> iterator = bossSelector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        System.out.println("Client connected: " + client.getRemoteAddress().toString().substring(1));
                        workerGroup.register(client);
                    }
                    // 删除当前key，防止重复处理
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ReactorServer().start();
    }
}
