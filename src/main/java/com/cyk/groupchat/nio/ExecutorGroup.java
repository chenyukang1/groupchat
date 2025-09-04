/*
 * Copyright (c) 2015-2025，千寻位置网络有限公司版权所有。
 *
 * 时空智能 共创数字中国（厘米级定位 | 毫米级感知 | 纳秒级授时）
 */
package com.cyk.groupchat.nio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yukang.chen
 */
public class ExecutorGroup {

    private static final ExecutorService EXEC = new ThreadPoolExecutor(200, 200,
            60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000),
            new NamedThreadFactory("business"));

    public static void processBusiness(Runnable task) {
        EXEC.submit(task);
    }

    static class NamedThreadFactory implements ThreadFactory {

        private final String baseName;

        private int counter = 0;

        public NamedThreadFactory(String baseName) {
            this.baseName = baseName;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, baseName + "-" + counter++);
        }
    }
}
