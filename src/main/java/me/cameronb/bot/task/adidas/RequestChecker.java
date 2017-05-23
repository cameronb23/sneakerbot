package me.cameronb.bot.task.adidas;

import me.cameronb.bot.Config;
import me.cameronb.bot.browser.BrowserData;
import me.cameronb.bot.proxy.BotProxy;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.*;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by Cameron on 2/9/17.
 */
public class RequestChecker implements Runnable {

    private final int id;
    private final BotProxy proxy;
    private final CookieStore cookieStore;
    private HttpClient client;
    private AdidasBrowser browser;
    private final RequestTask owner;


    // SAVE COOKIES / SESSION DATA
    public RequestChecker(int id, BotProxy proxyConfig, RequestTask creator) {
        this.id = id;
        this.proxy = proxyConfig;
        this.owner = creator;

        this.cookieStore = new BasicCookieStore();

        HttpClientBuilder builder;

        if(this.proxy != null) {
            CredentialsProvider creds = new BasicCredentialsProvider();
            AuthScope scope = new AuthScope(this.proxy.getAddress(), this.proxy.getPort());

            Credentials cred = new UsernamePasswordCredentials(this.proxy.getUsername(), this.proxy.getPassword());
            creds.setCredentials(scope, cred);

            HttpHost proxy = new HttpHost(this.proxy.getAddress(), this.proxy.getPort());

            builder = HttpClients.custom()
                    .setUserAgent(Config.INSTANCE.getUseragent())
                    .setProxy(proxy)
                    .setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy())
                    .setDefaultCredentialsProvider(creds)
                    .setDefaultCookieStore(this.cookieStore);


        } else {
             builder = HttpClients.custom()
                    .setUserAgent(Config.INSTANCE.getUseragent())
                    .setDefaultCookieStore(this.cookieStore);


        }

        builder.setRedirectStrategy(new LaxRedirectStrategy());
        /*builder.setRedirectStrategy(new DefaultRedirectStrategy() {
            public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)  {
                System.out.println(String.format("(%d) WAS REDIRECTED", id));
                try {
                    InputStream data = response.getEntity().getContent();


                    Set<String> foundSelectors = new HashSet<>();
                    boolean contains = false;

                    while(!contains && !owner.getIsDone().get()) {
                        String result = IOUtils.toString(data, "UTF-8");

                        for (String s : owner.getSelectors()) {
                            if (result.contains(s)) {
                                foundSelectors.add(s);
                                contains = true;
                            }
                        }
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });*/

        this.client = builder.build();
    }

    @Override
    public void run() {
        try {
            System.out.println("HELLO FROM " + Thread.currentThread().getName());
            Thread.sleep(1000 * id);
            while(!owner.getIsDone().get()) {
                System.out.println(String.format("(%d) sending another", id));
                new RequestRunnable().run();
                Thread.sleep(owner.getDelay() * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class RequestRunnable implements Runnable {

        @Override
        public void run() {
            synchronized (this) {
                try {
                    // submit request to splash page
                    HttpResponse res = client.execute(new HttpGet(owner.getUrl()));
                    InputStream data = res.getEntity().getContent();

                    System.out.println(cookieStore.getCookies());


                    Set<String> foundSelectors = new HashSet<>();
                    boolean contains = false;
                     /*String result = IOUtils.toString(data, "UTF-8");

                        for(String s : owner.getSelectors()) {
                            if(result.contains(s)) {
                                foundSelectors.add(s);
                                contains = true;
                            }
                        }*/

                    for(Cookie c : cookieStore.getCookies()) {
                        if(c.getName().toLowerCase().contains("dwsid") ||
                                c.getValue().toLowerCase().contains("dwsid")) {
                            foundSelectors.add("dwsid");
                            contains = true;
                        }
                    }

                    if(contains) {
                        System.out.println("passed splash");

                        if(owner.isOnePass()) {
                            owner.getIsDone().set(true);
                        }

                        System.out.println("Browser(" + id + ") passed splash(" + foundSelectors.toString() + ")");

                        System.out.println("Wait for browser to reload cart page with cookies.");

                        owner.getExecutor().submit(
                                new AdidasBrowser(owner, new BrowserData(proxy, cookieStore))
                        );
                    }
                } catch(Exception ex) {
                    System.out.println("err");
                }
            }
        }
    }
}

