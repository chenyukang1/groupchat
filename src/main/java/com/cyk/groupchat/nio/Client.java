package com.cyk.groupchat.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {

    private static SocketChannel channel;

    private String name;

    public Client() {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress("127.0.0.1", 6777));
            while (!channel.finishConnect()) {
                // wait, or do something else...
            }
            this.name = channel.getLocalAddress().toString().substring(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        System.out.println("Client [" + name + "] start...");
        new Thread(new PrintRunnable()).start();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            channel.write(ByteBuffer.wrap(line.getBytes()));
        }
    }

    static class PrintRunnable implements Runnable {

        @Override
        public void run() {
            try {
                Selector selector = Selector.open();
                channel.register(selector, SelectionKey.OP_READ);
                while (true) {
                    selector.select();
                    for (SelectionKey key : selector.selectedKeys()) {
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            int read = channel.read(buffer);
                            if (read > 0) {
                                buffer.flip();
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                System.out.println(new String(bytes));
                            } else if (read == -1) {
                                System.out.println("Server closed the connection");
                                channel.close();
                                return;
                            }
                        }
                    }
                    selector.selectedKeys().clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Client().start();
    }
}
