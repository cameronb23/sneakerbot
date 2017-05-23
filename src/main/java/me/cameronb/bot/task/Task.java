package me.cameronb.bot.task;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Cameron on 5/20/2017.
 */
public abstract class Task {

    @Getter
    private final String title;

    @Getter
    private final String url;

    @Getter @Setter
    private boolean running;

    @Override
    public String toString() {
        return title;
    }

    public Task(String name, String url) {
        this.title = name;
        this.url = url;
    }

    /**
     * Starts the task.
     */
    public abstract void start();

    /**
     * Ends this task(stopping it)
     */
    public abstract void end();

}
