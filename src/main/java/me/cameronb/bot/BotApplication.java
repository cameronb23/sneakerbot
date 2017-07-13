package me.cameronb.bot;

import com.license4j.DefaultOnlineLicenseKeyCheckTimerHandlerImpl;
import com.license4j.License;
import com.license4j.LicenseValidator;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
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

    @Getter
    private static License currentLicense;

    @Getter @Setter
    private Controller controller;

    @Getter
    private static ProxyLoader proxyLoader;

    @Getter
    private ObservableList<Task> tasks = FXCollections.observableArrayList();

    public static void main(String[] args) {

        //PropertyConfigurator.configure(System.getProperty("user.dir") + "/src/main/resources/log4j.properties");

        try {
            Config.CONTEXT = JAXBContext.newInstance(Config.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException("JAXB context for " + Config.class + " unavailable.", ex);
        }
        File applicationConfigFile = new File("./config.xml");
        if (applicationConfigFile.exists()) {
            Config.INSTANCE = Config.loadConfig(applicationConfigFile);
        } else {
            Config.INSTANCE = new Config();
        }

        // chromedriver path
        System.setProperty("webdriver.chrome.driver", Config.INSTANCE.getChromeDriverPath());
        System.setProperty("webdriver.gecko.driver", Config.INSTANCE.getFirefoxDriverPath());


        // TODO: verify license

        String key = Config.INSTANCE.getLicense();

        System.out.println("Validating license " + key);

        License license = LicenseValidator.validate(
                key,
                "30819f300d06092a864886f70d010101050003818d003081893032301006072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0004622934f25ab711f87fce78016da81cf979ed05326d37f2fd0bb695b9G02818100960db2d1eb0ed2c3d71d7c422abd9dd62fc9f1a6583612470c03676af0774c7b999b865957380c2efc5cda889cfe874fde0f4799850332ef53f22cc7aaf88b19a2fc190bf93d3fcbdf6f630ec330233cc301001ea9b9f180d5186f804973f91403RSA4102413SHA512withRSA58473a2f1de26f1fa325837a193707f58042df884b8e3852737d184a6f7e063f0203010001",
                "1", // product id
                "Tester Edition",
                "0.2-BETA",
                new Date(),
                null,
                "http://license.cameronb.me/algas/validateobk",
                new DefaultOnlineLicenseKeyCheckTimerHandlerImpl("License key could not be validated via validation server.", true)
        );

        switch(license.getValidationStatus()) {
            case LICENSE_VALID:
                break;
            case MISMATCH_HARDWARE_ID:
                System.err.println("Hardware ID could not be validated.");
                System.exit(0);
                break;
            case LICENSE_EXPIRED:
                System.err.println("Your license has expired!");
                System.exit(0);
                break;
            case INCORRECT_SYSTEM_TIME:
                System.err.println("Your system time is incorrect");
                System.exit(0);
                break;
            case MISMATCH_PRODUCT_ID:
                System.err.println("bad id");
                System.exit(0);
                break;
            case MISMATCH_PRODUCT_EDITION:
                System.err.println("bad edition");
                System.exit(0);
                break;
            case MISMATCH_PRODUCT_VERSION:
                System.err.println("bad version");
                System.exit(0);
                break;
            case FLOATING_LICENSE_NOT_FOUND:
                System.err.println("Invalid license key provided.");
                System.exit(0);
                break;
            default:
                System.err.println("Could not get license key validation.");
                System.err.println("stat: " + license.getValidationStatus().toString());
                System.exit(0);
                break;
        }

        currentLicense = license;



        System.out.println("Loading proxies");

        try {
            proxyLoader = new ProxyLoader(new File(System.getProperty("user.dir") + "/proxies.txt"));
            System.out.println("Loaded all proxies.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading proxies.");
        }

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        Parent root = FXMLLoader.load(getClass().getResource("/application.fxml"));

        primaryStage.setTitle("Adislayer v0.2");

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void startTask(Task t) {
        new Thread(() -> t.run()).start();
    }

}
