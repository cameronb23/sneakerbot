package me.cameronb.bot.task.adidas;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import me.cameronb.bot.BotApplication;
import me.cameronb.bot.Config;
import me.cameronb.bot.proxy.BotProxy;
import me.cameronb.bot.task.Task;
import me.cameronb.bot.task.TaskInstance;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Cameron on 5/20/2017.
 */
public class RequestTask extends Task<RequestChecker> {

    @Getter private String url;
    @Getter private long delay;
    @Getter private int instanceCount;
    @Getter private String[] selectors;
    @Getter private boolean onePass;
    @Getter private AtomicBoolean isDone = new AtomicBoolean(false);

    private final ObservableList<RequestChecker> instances = FXCollections.observableArrayList();

    @Getter private ExecutorService executor;

    public RequestTask(int instanceCount) {
        super("Adidas Request", Config.INSTANCE.getSplashUrl());
        this.delay = Config.INSTANCE.getRequestDelay() * 1000;
        this.instanceCount = instanceCount;
        this.selectors = Config.INSTANCE.getSelectors().toArray(new String[]{});
        this.onePass = Config.INSTANCE.isOnePass();

        for(int i = 0; i < instanceCount; i++) {
            // create new instance
            BotProxy proxy = BotApplication.getProxyLoader().getNext();

            RequestChecker instance;

            if(proxy != null) {
                instance = new RequestChecker(
                        i + 1,
                        proxy,
                        this
                );

                BotApplication.getProxyLoader().markUsed(proxy);
            } else {
                instance = new RequestChecker(
                        i + 1,
                        null,
                        this
                );
            }

            instances.add(instance);
        }
    }

    @Override
    public ObservableList<RequestChecker> getInstances() {
        return instances;
    }

    @Override
    public void run() {
        //executor = Executors.newFixedThreadPool(instanceCount);
        for(RequestChecker instance : instances) {
            instance.start();
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

        Iterator<RequestChecker> iter = instances.iterator();
        while(iter.hasNext()) {
            RequestChecker instance = iter.next();
            instance.end();
            iter.remove();
        }

        /*if(executor != null) {
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
        }*/
    }

    @Override
    public boolean isRunning() {
        return executor != null;
    }
}
