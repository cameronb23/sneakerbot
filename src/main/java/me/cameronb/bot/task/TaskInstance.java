package me.cameronb.bot.task;

import lombok.Getter;
import lombok.Setter;
import me.cameronb.bot.proxy.BotProxy;

/**
 * Created by Cameron on 6/25/2017.
 */
public abstract class TaskInstance implements Runnable {

    @Getter
    private final int id;

    @Getter
    private BotProxy proxy;

    @Getter @Setter
    private boolean success = false;

    @Getter @Setter
    private String status;

    @Getter @Setter
    private Thread thread;

    public TaskInstance(int id, BotProxy proxyConfig) {
        this.id = id;
        this.proxy = proxyConfig;
        this.thread = Thread.currentThread();
    }

    public abstract void run();
}
