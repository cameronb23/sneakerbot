package com.isneaker.bot;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by student on 2/9/17.
 */
public class SplashChecker extends Thread {


    String link = "https://www.google.com/recaptcha/";

    private static final String SELECTOR = "data-sitekey=";

    private final String id;
    private final String url;
    private final BotProxy proxy;
    private final HttpClient client;
    private final CookieStore cookieStore;
    private Timer timer;
    private BotBrowser browser;


    // SAVE COOKIES / SESSION DATA

    public SplashChecker(String id, String url, BotProxy proxyConfig) {
        this.id = id;
        this.url = url;
        this.proxy = proxyConfig;

        this.cookieStore = new BasicCookieStore();

        if(proxyConfig != null) {

            CredentialsProvider creds = new BasicCredentialsProvider();
            AuthScope scope = new AuthScope(proxyConfig.getAddress(), proxyConfig.getPort());

            Credentials cred = new UsernamePasswordCredentials(proxyConfig.getUsername(), proxyConfig.getPassword());
            creds.setCredentials(scope, cred);

            HttpHost proxy = new HttpHost(proxyConfig.getAddress(), proxyConfig.getPort());

            this.client = HttpClients.custom()
                    .setProxy(proxy)
                    .setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy())
                    .setDefaultCredentialsProvider(creds)
                    .setDefaultCookieStore(this.cookieStore)
                    .build();


        } else {
            this.client = HttpClients.custom()
                    .setDefaultCookieStore(this.cookieStore)
                    .build();


        }
    }


    public void run() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new SplashTask(this.client), 1000,  3000);
    }

    private class SplashTask extends TimerTask {

        private final HttpClient client;

        private SplashTask(HttpClient client) {
            this.client = client;
        }

        public void run() {
            try {
                HttpResponse res = this.client.execute(new HttpGet(BotMain.URL));
                InputStream data = res.getEntity().getContent();

                String result = IOUtils.toString(data, StandardCharsets.UTF_8);

                boolean contains  = result.contains(SELECTOR);

                if(contains) {
                    System.out.println("Browser(" + id + ") passed splash");
                    timer.cancel();
                    System.out.println("Wait for browser to reload cart page with cookies.");
                    browser = new BotBrowser(new BrowserData(proxy, cookieStore));
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}

