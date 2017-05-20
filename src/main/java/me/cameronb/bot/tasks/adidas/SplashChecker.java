package me.cameronb.bot.tasks.adidas;

import me.cameronb.bot.Config;
import me.cameronb.bot.browser.BrowserData;
import me.cameronb.bot.proxy.BotProxy;
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
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Cameron on 2/9/17.
 */
public class SplashChecker extends Thread {

    private final String id;
    private final BotProxy proxy;
    private final HttpClient client;
    private final CookieStore cookieStore;
    private Timer timer;
    private SplashBrowser browser;


    // SAVE COOKIES / SESSION DATA

    public SplashChecker(String id, BotProxy proxyConfig) {
        this.id = id;
        this.proxy = proxyConfig;

        this.cookieStore = new BasicCookieStore();

        if(proxyConfig != null) {

            CredentialsProvider creds = new BasicCredentialsProvider();
            AuthScope scope = new AuthScope(proxyConfig.getAddress(), proxyConfig.getPort());

            Credentials cred = new UsernamePasswordCredentials(proxyConfig.getUsername(), proxyConfig.getPassword());
            creds.setCredentials(scope, cred);

            HttpHost proxy = new HttpHost(proxyConfig.getAddress(), proxyConfig.getPort());

            this.client = HttpClients.custom()
                    .setUserAgent(Config.INSTANCE.getUseragent())
                    .setProxy(proxy)
                    .setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy())
                    .setDefaultCredentialsProvider(creds)
                    .setDefaultCookieStore(this.cookieStore)
                    .build();


        } else {
            this.client = HttpClients.custom()
                    .setUserAgent(Config.INSTANCE.getUseragent())
                    .setDefaultCookieStore(this.cookieStore)
                    .build();


        }
    }


    public void run() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new SplashTask(this.client), 1000,  Config.INSTANCE.getRequestDelay());
    }

    private class SplashTask extends TimerTask {

        private final HttpClient client;

        private SplashTask(HttpClient client) {
            this.client = client;
        }

        public void run() {
            try {
                // submit request to splash page
                HttpResponse res = this.client.execute(new HttpGet(Config.INSTANCE.getSplashUrl()));
                InputStream data = res.getEntity().getContent();

                String result = IOUtils.toString(data, StandardCharsets.UTF_8);

                Set<String> foundSelectors = new HashSet<>();
                boolean contains = false;

                for(String s : Config.INSTANCE.getSelectors()) {
                    if(result.contains(s)) {
                        foundSelectors.add(s);
                        contains = true;
                    }
                }

                if(contains) {
                    System.out.println("Browser(" + id + ") passed splash(" + foundSelectors.toString() + ")");
                    timer.cancel();

                    System.out.println("Wait for browser to reload cart page with cookies.");
                    browser = new SplashBrowser(new BrowserData(proxy, cookieStore));
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}

