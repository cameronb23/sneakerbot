package me.cameronb.bot.task;

import lombok.Getter;
import me.cameronb.bot.proxy.BotProxy;

/**
 * Created by Cameron on 6/25/2017.
 */
public abstract class TaskInstance implements Runnable {

    @Getter
    private final int id;

    @Getter
    private BotProxy proxy;

    public TaskInstance(int id, BotProxy proxyConfig) {
        this.id = id;
        this.proxy = proxyConfig;
    }

    public abstract void run();
}
