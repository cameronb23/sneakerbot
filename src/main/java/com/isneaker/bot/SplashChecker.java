package com.isneaker.bot;

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
    private final AsyncHttpClient client;
    private Timer timer;


    // SAVE COOKIES / SESSION DATA

    public SplashChecker(String url, BotProxy proxyConfig) {
        this.url = url;

        if(proxyConfig != null) {
            Realm r = new Realm.Builder(proxyConfig.getUsername(), proxyConfig.getPassword())
                    .setScheme(Realm.AuthScheme.BASIC)
                    .setUsePreemptiveAuth(true)
                    .build();

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
        timer.schedule(new SplashTask(this.client), 10000);
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

                boolean contains  = data.contains(SELECTOR);

                if(contains) {
                    timer.cancel();
                    System.out.println("FOUND CAPTCHA ELEMENT");
                    System.exit(0);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}
