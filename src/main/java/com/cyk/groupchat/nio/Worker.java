package com.cyk.groupchat.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Worker implements Runnable{

    private final Selector selector;

    private final String name;

    private final Thread thread;

    private volatile boolean started = false;

    private final Queue<SocketChannel> queue = new ConcurrentLinkedQueue<>();

    public Worker(String name) throws IOException {
        this.selector = Selector.open();
        this.name = name;
        this.thread = new Thread(this, name);
    }

    public void register(SocketChannel channel) {
        queue.add(channel);
        if (!started) {
            thread.start();
            started = true;
        }
        selector.wakeup(); // 唤醒，让 run() 线程去完成注册
    }

    @Override
    public void run() {
        while (true) {
            try {
                selector.select(); // 阻塞等待就绪的事件
                SocketChannel channel;
                while ((channel = queue.poll()) != null) {
                    channel.register(selector, SelectionKey.OP_READ);
                    channel.configureBlocking(false);
                    ClientManager.addClient(channel);
                }
                // 处理就绪的事件（读事件）
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    iterator.remove(); // 移除已处理的事件
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRead(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            int read = channel.read(buffer);
            if (read > 0) {
                buffer.flip();
                final String message = new String(buffer.array(), 0, buffer.remaining());
                System.out.println(name + " received message: " + message);
                // 业务线程池隔离
                ExecutorGroup.processBusiness(() -> {
                    try {
                        if (message.startsWith("/join ")) {
                            String room = message.substring(6).trim();
                            ClientManager.joinRoom(room, channel);
                        } else if (message.startsWith("/quit ")) {
                            String room = message.substring(6).trim();
                            ClientManager.quitRoom(room, channel);
                        } else {
                            String broadcastMsg = "[" + channel.getRemoteAddress().toString().substring(1) + "] :" + message;
                            ClientManager.broadcast(channel, broadcastMsg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else if (read == -1) {
                // 客户端关闭连接
                ClientManager.removeClient(channel);
                key.cancel();
                channel.close();
                System.out.println("Client disconnected: " + channel.getRemoteAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
