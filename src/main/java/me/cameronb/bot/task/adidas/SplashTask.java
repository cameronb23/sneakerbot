package me.cameronb.bot.task.adidas;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import me.cameronb.bot.BotApplication;
import me.cameronb.bot.Config;
import me.cameronb.bot.proxy.BotProxy;
import me.cameronb.bot.task.Task;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Cameron on 5/20/2017.
 */
public class SplashTask extends Task<SplashChecker> {

    @Getter private long delay;
    @Getter private int instanceCount;
    @Getter private String[] selectors;
    @Getter private boolean onePass;

    private final ObservableList<SplashChecker> instances = FXCollections.observableArrayList();

    @Getter private AtomicBoolean isDone = new AtomicBoolean(false);

    //@Getter private ThreadPool executor;

    public SplashTask(int instanceCount) {
        super("Adidas Splash", Config.INSTANCE.getSplashUrl());
        this.delay = Config.INSTANCE.getRequestDelay() * 1000; // 1000 is one second
        this.instanceCount = instanceCount;
        this.selectors = Config.INSTANCE.getSelectors().toArray(new String[]{});
        this.onePass = Config.INSTANCE.isOnePass();

        for(int i = 0; i < instanceCount; i++) {
            // create new instance
            BotProxy proxy = BotApplication.getProxyLoader().getNext();

            SplashChecker instance;

            if(proxy != null) {
                instance = new SplashChecker(i, proxy, this);

                BotApplication.getProxyLoader().markUsed(proxy);
            } else {
                instance = new SplashChecker(i, null, this);
            }

            instances.add(instance);
        }
    }

    @Override
    public ObservableList<SplashChecker> getInstances() {
        return instances;
    }

    @Override
    public void run() {
        setRunning(true);

        for(SplashChecker instance : instances) {
            new Thread(instance).start();
        }

        for(;;) {
            if(isDone.get()) {
                return;
            }
        }
    }

    @Override
    public void end() {
        isDone.set(true);

        Iterator<SplashChecker> iter = instances.iterator();
        while(iter.hasNext()) {
            SplashChecker instance = iter.next();
            instance.end();
            iter.remove();
        }
    }
}
