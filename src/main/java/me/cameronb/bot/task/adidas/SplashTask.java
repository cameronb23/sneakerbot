package me.cameronb.bot.task.adidas;

import lombok.Getter;
import me.cameronb.bot.BotApplication;
import me.cameronb.bot.proxy.BotProxy;
import me.cameronb.bot.task.Task;
import me.cameronb.bot.thread.ThreadPool;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by Cameron on 5/20/2017.
 */
public class SplashTask extends Task {

    @Getter private String url;
    @Getter private long delay;
    @Getter private int instanceCount;
    @Getter private String[] selectors;
    @Getter private boolean onePass;

    private final Set<SplashChecker> instances = new HashSet<>();

    @Getter private ExecutorService executor;

    //@Getter private ThreadPool executor;

    public SplashTask(String url, long requestDelay, int instances, String[] selectors, boolean onePass) {
        super("Adidas Splash", url);
        this.url = url;
        this.delay = requestDelay;
        this.instanceCount = instances;
        this.selectors = selectors;
        this.onePass = onePass;
    }

    @Override
    public void start() {
        setRunning(true);
        //executor = Executors.newWorkStealingPool(128);
        //executor = new ThreadPool(instanceCount);
        executor = Executors.newFixedThreadPool(instanceCount * 2);
        //executor = new ThreadPoolExecutor(1, 128, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));

        for(int i = 0; i < instanceCount; i++) {
            // create new instance
            BotProxy proxy = BotApplication.getInstance().getProxyLoader().getNext();

            SplashChecker instance;

            if(proxy != null) {
                instance = new SplashChecker(proxy, this, i + 1);

                BotApplication.getInstance().getProxyLoader().markUsed(proxy);
            } else {
                instance = new SplashChecker(null, this, i + 1);
            }

            instances.add(instance);
        }

        for(SplashChecker instance : instances) {
            executor.submit(instance);
        }
    }

    @Override
    public void end() {
        executor.shutdownNow();
        executor = null;
        setRunning(false);
    }
}