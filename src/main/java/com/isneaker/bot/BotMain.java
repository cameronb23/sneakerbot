package com.isneaker.bot;

import com.machinepublishers.jbrowserdriver.UserAgent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by student on 2/9/17.
 */
public class BotMain {


    public static String USERAGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";

    // build useragent
    public static UserAgent USERAGENT_OBJ = null; // {UserAgent}

    //final static String URL = "http://www.adidas.com/yeezy";
    //final static String URL = "http://adidas.bot.nu/yeezy/";
    //final static String URL = "http://www.adidas.com/us/nmd_xr1-primeknit-shoes/BB2911.html";
    final static String URL = "http://www.adidas.com/on/demandware.store/Sites-adidas-US-Site/en_US/Cart-MiniAddProduct?layer=Add%20To%20Bag%20overlay&pid=BB2911_630&Quantity=1&masterPid=BB2911add-to-cart-button=";


    // use config
    final static ExecutorService executor = Executors.newFixedThreadPool(10);


    private static ProxyLoader proxyLoader;
    private static int threadsRunning = 0;
    private static int count = 1;

    public static void main(String[] args) {

        System.setProperty("webdriver.chrome.driver", "~/Downloads/chromedriver");


        try {
            proxyLoader = new ProxyLoader(new File(System.getProperty("user.dir") + "/proxies.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loading proxies and tasks");

        Iterator<BotProxy> iterator = proxyLoader.getProxiesLoaded().keySet().iterator();

        while(iterator.hasNext() && threadsRunning < count) {
            BotProxy proxy = iterator.next();

            SplashChecker thread = new SplashChecker(URL, proxy);
            executor.submit(thread);
            threadsRunning++;
        }

        while(threadsRunning < count) {
            SplashChecker thread = new SplashChecker(URL, null);
            executor.submit(thread);
            threadsRunning++;
        }
    }
}
