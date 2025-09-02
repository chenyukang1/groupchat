package com.cyk.groupchat.nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class WorkerGroup {

    private final Worker[] workers;

    private int next = 0;

    public WorkerGroup(int size) throws IOException {
        workers = new Worker[size];
        for (int i = 0; i < size; i++) {
            workers[i] = new Worker("Worker-" + i);
        }
    }

    public void register(SocketChannel channel) {
        Worker worker = workers[next];
        worker.register(channel);
        next = (next + 1) % workers.length;
    }

}
