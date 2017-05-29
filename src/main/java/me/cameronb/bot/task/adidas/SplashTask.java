package me.cameronb.bot.task.adidas;

import lombok.Getter;
import lombok.Setter;
import me.cameronb.bot.BotApplication;
import me.cameronb.bot.proxy.BotProxy;
import me.cameronb.bot.task.Task;
import me.cameronb.bot.thread.ThreadPool;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Getter private AtomicBoolean isDone = new AtomicBoolean(false);

    //@Getter private ThreadPool executor;

    public SplashTask(String url, long requestDelay, int instances, String[] selectors, boolean onePass) {
        super("Adidas Splash", url);
        this.url = url;
        this.delay = requestDelay * 1000; // 1000 is one second
        this.instanceCount = instances;
        this.selectors = selectors;
        this.onePass = onePass;
    }

    @Override
    public void run() {
        setRunning(true);
        //executor = Executors.newWorkStealingPool(instanceCount * 2);
        executor = Executors.newFixedThreadPool(instanceCount * 2);
        /*executor = new ThreadPoolExecutor(
                1,
                instanceCount * 2,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100)
        );*/

        for(int i = 0; i < instanceCount; i++) {
            // create new instance
            BotProxy proxy = BotApplication.getInstance().getProxyLoader().getNext();

            SplashChecker instance;

            if(proxy != null) {
                instance = new SplashChecker(proxy, this, i);

                BotApplication.getInstance().getProxyLoader().markUsed(proxy);
            } else {
                instance = new SplashChecker(null, this, i);
            }

            instances.add(instance);
        }

        System.out.println(instances.size());

        Iterator<SplashChecker> iter = instances.iterator();

        while(iter.hasNext()) {
            SplashChecker checker = iter.next();

            // TODO: DOES NOT SEEM TO SUBMIT TASKS TO EXECUTOR
            executor.execute(checker);
        }
    }

    @Override
    public void end() {
        System.out.println("SHUTTING DOWN TASKS");

        for(SplashChecker c : instances) {
            c.stop();
        }

        instances.clear();

        executor.shutdownNow();
        executor = null;
        setRunning(false);
    }
}
