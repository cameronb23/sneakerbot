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
import me.cameronb.bot.task.adidas.SplashChecker;
import me.cameronb.bot.task.adidas.SplashTask;
import org.apache.log4j.PropertyConfigurator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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

    @Getter
    private static BotApplication instance;

    // create our executor
    @Getter
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    @Getter @Setter
    private Controller controller;

    @Getter
    private static ProxyLoader proxyLoader;

    @Getter
    private static Set<Task> tasks = new HashSet<>();

    public static void main(String[] args) {

        //PropertyConfigurator.configure(System.getProperty("user.dir") + "/src/main/resources/log4j.properties");

        if(args.length < 1) {
            System.err.println("You must provide a config file!");
            System.exit(0);
        }

        String configFile = args[0];

        try {
            Config.CONTEXT = JAXBContext.newInstance(Config.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException("JAXB context for " + Config.class + " unavailable.", ex);
        }
        File applicationConfigFile = new File(System.getProperty("user.dir") + "/config.xml");
        if (applicationConfigFile.exists()) {
            Config.INSTANCE = Config.loadConfig(applicationConfigFile);
        } else {
            Config.INSTANCE = new Config();
        }

        // chromedriver path
        System.setProperty("webdriver.chrome.driver", Config.INSTANCE.getChromeDriverPath());
        System.setProperty("webdriver.gecko.driver", Config.INSTANCE.getFirefoxDriverPath());

        System.out.println("Loading proxies");

        try {
            proxyLoader = new ProxyLoader(new File(System.getProperty("user.dir") + "/proxies.txt"));
            System.out.println("Loaded all proxies.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading proxies.");
        }

        launch(args);

        tasks.add(new SplashTask(
                Config.INSTANCE.getSplashUrl(),
                Config.INSTANCE.getRequestDelay(),
                Config.INSTANCE.getTaskCount(),
                Config.INSTANCE.getSelectors().toArray(new String[]{}),
                Config.INSTANCE.isOnePass()
        ));
        startTasks();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        Parent root = FXMLLoader.load(getClass().getResource("/application.fxml"));

        primaryStage.setTitle("NabeelForce v1");

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static void startTasks() {
        for(Task t : tasks) {
            new Thread(() -> t.run()).start();
        }
    }

    public static BotApplication getInstance() {
        return instance;
    }

}
