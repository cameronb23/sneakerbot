package me.cameronb.bot.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import me.cameronb.bot.task.Task;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Cameron on 5/21/2017.
 */
public class ThreadKiller extends Task {

    private Object s = new Object();
    private int count = 0;
    private AtomicBoolean bool = new AtomicBoolean(true);

    public ThreadKiller() {
        super("Thread Killer", "http://www.google.com/");
    }

    @Override
    public void run() {
        while(bool.get()){
            new Thread(() -> {
                synchronized(s){
                    count += 1;
                    System.err.println("New thread #"+count);
                }
                for(;;){
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e){
                        System.err.println(e);
                    }
                }
            }).start();
        }
    }

    @Override
    public void end() {
        bool.set(false);
    }

    @Override
    public ObservableList getInstances() {
        return FXCollections.observableArrayList();
    }

}
