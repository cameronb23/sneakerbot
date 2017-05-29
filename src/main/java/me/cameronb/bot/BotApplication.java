package me.cameronb.bot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import me.cameronb.bot.proxy.ProxyLoader;
import me.cameronb.bot.task.Task;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Cameron on 2/9/17.
 */
public class BotApplication extends Application {

    private static BotApplication instance;

    // create our executor
    @Getter
    private final ExecutorService executor = Executors.newFixedThreadPool(1);


    @Getter
    private ProxyLoader proxyLoader;

    @Getter @Setter
    private Controller controller;

    @Getter
    private Set<Task> tasks = new HashSet<>();

    public static void main(String[] args) {

        // chromedriver path
        System.setProperty("webdriver.chrome.driver", Config.INSTANCE.getChromeDriverPath());
        System.setProperty("webdriver.gecko.driver", Config.INSTANCE.getFirefoxDriverPath());

        // finally, launch our UI.
        launch(args);

    }

    public static BotApplication getInstance() {
        return instance;
    }

    public boolean startTasks() {
        if(tasks.size() < 1) {
            System.out.println("No tasks added!");
            return false;
        }

        System.out.println("Starting " + tasks.size() + " tasks.");

        for(Task task : tasks) {
            new Thread(() -> {
                task.run();
            }).start();
            controller.updateTasks();
        }

        System.out.println("Tasks started.");
        return true;
    }

    public void stopTasks() {
        for(Task t : tasks) {
            t.end();
            controller.updateTasks();
        }

        System.out.println("All tasks stopped");
    }

    public void addTask(Task t) {
        this.tasks.add(t);
        controller.addTask(t);

        System.out.println("New size: " + this.tasks.size());
    }

    public void removeTask(Task t) {
        this.tasks.remove(t);
        controller.removeTask(t);

        System.out.println("New size: " + this.tasks.size());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        Parent root = FXMLLoader.load(getClass().getResource("/bot.fxml"));

        primaryStage.setTitle("NabeelForce v1");

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("Loading proxies");

        try {
            proxyLoader = new ProxyLoader(new File(System.getProperty("user.dir") + "/proxies.txt"));
            System.out.println("Loaded all proxies.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading proxies.");
        }
    }

}
