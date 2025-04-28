package net.zousys.mathtrading.interfaces;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Connector {
    private ConcurrentLinkedQueue<Message> queue;
    public abstract void connect();
    public abstract void disconnect();
    public abstract void maintainSession();
}
