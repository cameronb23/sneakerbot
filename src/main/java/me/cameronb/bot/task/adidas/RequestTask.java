package me.cameronb.bot.task.adidas;

import lombok.Getter;
import me.cameronb.bot.BotApplication;
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
public class RequestTask extends Task {

    @Getter private String url;
    @Getter private long delay;
    @Getter private int instanceCount;
    @Getter private String[] selectors;
    @Getter private boolean onePass;
    @Getter private AtomicBoolean isDone = new AtomicBoolean(false);

    private final Set<RequestChecker> instances = new HashSet<>();

    @Getter private ExecutorService executor;

    //@Getter private ThreadPool executor;

    public RequestTask(String url, long requestDelay, int instances, String[] selectors, boolean onePass) {
        super("Adidas Request", url);
        this.url = url;
        this.delay = requestDelay;
        this.instanceCount = instances;
        this.selectors = selectors;
        this.onePass = onePass;
    }

    @Override
    public void start() {
        //executor = Executors.newWorkStealingPool(128);
        //executor = new ThreadPool(instanceCount * 3);
        executor = Executors.newFixedThreadPool(128);
        //executor = new ThreadPoolExecutor(1, 128, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));

        for(int i = 0; i < instanceCount; i++) {
            // create new instance
            BotProxy proxy = BotApplication.getInstance().getProxyLoader().getNext();

            RequestChecker instance;

            if(proxy != null) {
                instance =new RequestChecker(
                        i + 1,
                        proxy,
                        this
                );

                BotApplication.getInstance().getProxyLoader().markUsed(proxy);
            } else {
                instance = new RequestChecker(
                        i + 1,
                        null,
                        this
                );
            }

            instances.add(instance);
        }

        for(RequestChecker instance : instances) {
            executor.submit(instance);
        }
    }

    @Override
    public void end() {
        this.getIsDone().set(true);
        Iterator<RequestChecker> iter = instances.iterator();

        this.isDone.set(true);

        while(iter.hasNext()) {
            RequestChecker instance = iter.next();
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

    @Override
    public boolean isRunning() {
        return executor != null;
    }
}
