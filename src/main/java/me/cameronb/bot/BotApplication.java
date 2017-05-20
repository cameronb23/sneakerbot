package me.cameronb.bot;

import com.isneaker.bot.proxy.BotProxy;
import com.isneaker.bot.proxy.ProxyLoader;
import com.isneaker.bot.tasks.adidas.SplashChecker;
import com.isneaker.bot.ui.BotUI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Cameron on 2/9/17.
 */
public class BotApplication extends Application {


    // create our executor
    final static ExecutorService executor = Executors.newFixedThreadPool(Config.INSTANCE.getTaskCount());


    private static ProxyLoader proxyLoader;
    private static int threadsRunning = 0;

    private BotUI ui;

    public static void main(String[] args) {

        System.out.println("Loading proxies and tasks");

        // chromedriver path
        System.setProperty("webdriver.chrome.driver", Config.INSTANCE.getChromeDriverPath());


        try {
            proxyLoader = new ProxyLoader(new File(System.getProperty("user.dir") + "/proxies.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // finally, launch our UI.
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/bot.fxml"));
        this.ui = new BotUI(primaryStage, root);
    }


    public static void startTasks() {
        System.out.println("Starting " + Config.INSTANCE.getTaskCount() + " tasks.");

        Iterator<BotProxy> iterator = proxyLoader.getProxiesLoaded().keySet().iterator();

        while(iterator.hasNext() && threadsRunning < Config.INSTANCE.getTaskCount()) {
            BotProxy proxy = iterator.next();

            SplashChecker thread = new SplashChecker(UUID.randomUUID().toString(), proxy);
            executor.submit(thread);
            threadsRunning++;
        }

        while(threadsRunning < Config.INSTANCE.getTaskCount()) {
            SplashChecker thread = new SplashChecker(UUID.randomUUID().toString(), null);
            executor.submit(thread);
            threadsRunning++;
        }
    }
}
