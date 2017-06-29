package me.cameronb.bot.task.adidas;

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
public class SplashTask extends Task {

    @Getter private long delay;
    @Getter private int instanceCount;
    @Getter private String[] selectors;
    @Getter private boolean onePass;

    private final Set<SplashChecker> instances = new HashSet<>();

    @Getter private ExecutorService executor;

    @Getter private AtomicBoolean isDone = new AtomicBoolean(false);

    //@Getter private ThreadPool executor;

    public SplashTask(int instances) {
        super("Adidas Splash", Config.INSTANCE.getSplashUrl());
        this.delay = Config.INSTANCE.getRequestDelay() * 1000; // 1000 is one second
        this.instanceCount = instances;
        this.selectors = Config.INSTANCE.getSelectors().toArray(new String[]{});
        this.onePass = Config.INSTANCE.isOnePass();
    }

    @Override
    public void run() {
        setRunning(true);
        //executor = Executors.newWorkStealingPool(instanceCount * 2);
        executor = Executors.newFixedThreadPool(instanceCount);
        /*executor = new ThreadPoolExecutor(
                1,
                instanceCount * 2,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100)
        );*/

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

        for(SplashChecker instance : instances) {
            new Thread(instance).start();
            //executor.submit(instance);
        }

        for(;;) {
            if(isDone.get()) {
                return;
            }
        }
    }

    @Override
    public void end() {
        this.getIsDone().set(true);
        Iterator<SplashChecker> iter = instances.iterator();

        this.isDone.set(true);

        while(iter.hasNext()) {
            SplashChecker instance = iter.next();
            iter.remove();
        }

        if(executor != null) {
            try {
                System.out.println("attempt to shutdown executor");
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("tasks interrupted");
            } finally {
                if (!executor.isTerminated()) {
                    System.err.println("cancel non-finished tasks");
                }
                executor.shutdownNow();
                System.out.println("shutdown finished");
            }
        }
    }
}
