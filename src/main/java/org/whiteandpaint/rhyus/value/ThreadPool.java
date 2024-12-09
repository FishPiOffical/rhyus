package org.whiteandpaint.rhyus.value;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {

    public static ExecutorService messageThreadPool = Executors.newFixedThreadPool(2);
    public static ExecutorService quickMessageThreadPool = Executors.newFixedThreadPool(1);
    public static ExecutorService slowMessageThreadPool = Executors.newFixedThreadPool(1);
    public static ExecutorService adminMessageThreadPool = Executors.newFixedThreadPool(1);

}
