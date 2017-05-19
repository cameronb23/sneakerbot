package com.isneaker.bot;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by student on 2/9/17.
 */
public class BotMain {

    //final static String URL = "http://www.adidas.com/yeezy";
    final static String URL = "http://www.adidas.com/us/nmd_xr1-primeknit-shoes/BB2911.html";


    // use config
    final static ExecutorService executor = Executors.newFixedThreadPool(10);


    private static ProxyLoader proxyLoader;
    private final Set<BotProxy> usedProxies = new HashSet<>();
    private static int threadsRunning = 0;

    public static void main(String[] args) {

        try {
            proxyLoader = new ProxyLoader(new File(System.getProperty("user.dir") + "/proxies.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterator<BotProxy> iterator = proxyLoader.getProxiesLoaded().keySet().iterator();

        while(iterator.hasNext()) {
            BotProxy proxy = iterator.next();

            SplashChecker thread = new SplashChecker(URL, proxy);
            executor.submit(thread);
        }

        while(threadsRunning < 10) {
            SplashChecker thread = new SplashChecker(URL, null);
            executor.submit(thread);
        }
    }

}
