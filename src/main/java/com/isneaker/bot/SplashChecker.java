package com.isneaker.bot;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.asynchttpclient.*;
import org.asynchttpclient.proxy.ProxyServer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by student on 2/9/17.
 */
public class SplashChecker extends Thread {


    String link = "https://www.google.com/recaptcha/";

    private static final String SELECTOR = "data-sitekey=";

    private final String url;
    private final BotProxy proxy;
    private final AsyncHttpClient client;
    private Timer timer;
    private BotBrowser browser;


    // SAVE COOKIES / SESSION DATA

    public SplashChecker(String url, BotProxy proxyConfig) {
        this.url = url;
        this.proxy = proxyConfig;

        if(proxyConfig != null) {

            // configure proxy auth
            Realm r = new Realm.Builder(proxyConfig.getUsername(), proxyConfig.getPassword())
                    .setScheme(Realm.AuthScheme.BASIC)
                    .setUsePreemptiveAuth(true)
                    .build();

            // configure client for proxy support
            AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder()
                    .setProxyServer(new ProxyServer.Builder(proxyConfig.getAddress(), proxyConfig.getPort()).setRealm(r))
                    .build();

            this.client = new DefaultAsyncHttpClient(config);
        } else {
            this.client = new DefaultAsyncHttpClient();
        }
    }

    public Response test() {
        Future<Response> res = this.client.prepareGet(this.url).execute();

        try {
            return res.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }


    public void run() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new SplashTask(this.client), 1000,  3000);
    }

    private class SplashTask extends TimerTask {

        private final AsyncHttpClient client;

        private SplashTask(AsyncHttpClient client) {
            this.client = client;
        }

        public void run() {
            Future<Response> res = this.client.prepareGet(url).execute();
            try {
                String data = res.get().getResponseBody();

                System.out.println("Got response");

                boolean contains  = data.contains(SELECTOR);

                timer.cancel();
                System.out.println("FOUND CAPTCHA ELEMENT");
                System.out.println("COOKIES: \n" + res.get().getCookies().toString());

                browser = new BotBrowser(new BrowserData(proxy, res.get().getCookies()));

            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}

