package me.cameronb.bot.task.adidas;

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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by Cameron on 2/9/17.
 */
public class RequestChecker implements Callable<Boolean> {

    private final int id;
    private final BotProxy proxy;
    private final CookieStore cookieStore;
    private HttpClient client;
    private AdidasBrowser browser;
    private final RequestTask owner;

    private volatile Future future;


    // SAVE COOKIES / SESSION DATA
    public RequestChecker(int id, BotProxy proxyConfig, RequestTask creator) {
        this.id = id;
        this.proxy = proxyConfig;
        this.owner = creator;

        this.cookieStore = new BasicCookieStore();

        if(this.proxy != null) {
            CredentialsProvider creds = new BasicCredentialsProvider();
            AuthScope scope = new AuthScope(this.proxy.getAddress(), this.proxy.getPort());

            Credentials cred = new UsernamePasswordCredentials(this.proxy.getUsername(), this.proxy.getPassword());
            creds.setCredentials(scope, cred);

            HttpHost proxy = new HttpHost(this.proxy.getAddress(), this.proxy.getPort());

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

    private boolean done = false;

    @Override
    public Boolean call() {


        try {
            System.out.println("HELLO FROM " + Thread.currentThread().getName());
            Thread.sleep(1000 * id);
            while(!done) {
                new RequestRunnable().run();
                Thread.sleep(owner.getDelay() * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Instance started.");
        return true;
    }


    public void stop() {
        done = true;
        System.out.println("Instance stopped");
    }

    private class RequestRunnable implements Runnable {

        @Override
        public void run() {
            synchronized (this) {
                try {
                    // submit request to splash page
                    HttpResponse res = client.execute(new HttpGet(owner.getUrl()));
                    InputStream data = res.getEntity().getContent();


                    String result = IOUtils.toString(data, "UTF-8");

                    Set<String> foundSelectors = new HashSet<>();
                    boolean contains = false;

                    for(String s : owner.getSelectors()) {
                        if(result.contains(s)) {
                            foundSelectors.add(s);
                            contains = true;
                        }
                    }

                    System.out.println("Got response");

                    if(contains) {
                        if(owner.isOnePass()) {
                            owner.end();
                        }

                        System.out.println("Browser(" + id + ") passed splash(" + foundSelectors.toString() + ")");

                        System.out.println("Wait for browser to reload cart page with cookies.");

                        owner.getExecutor().submit(
                                new AdidasBrowser(owner, new BrowserData(proxy, cookieStore))
                        );

                        done = true;
                    }
                } catch(Exception ex) {
                    System.out.println("err");
                }
            }
        }
    }
}

