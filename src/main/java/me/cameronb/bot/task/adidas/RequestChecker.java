package me.cameronb.bot.task.adidas;

import me.cameronb.bot.Config;
import me.cameronb.bot.browser.BrowserData;
import me.cameronb.bot.proxy.BotProxy;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.*;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

/**
 * Created by Cameron on 2/9/17.
 */
public class RequestChecker implements Runnable {

    private final int id;
    private final BotProxy proxy;
    private BasicCookieStore cookieStore;
    private CloseableHttpClient client;
    private AdidasBrowser browser;
    private final RequestTask owner;


    // SAVE COOKIES / SESSION DATA
    public RequestChecker(int id, BotProxy proxyConfig, RequestTask creator) {
        this.id = id;
        this.proxy = proxyConfig;
        this.owner = creator;

        this.cookieStore = new BasicCookieStore();

        HttpClientBuilder builder = HttpClientBuilder.create();

        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();

        if(this.proxy != null) {
            CredentialsProvider creds = new BasicCredentialsProvider();
            AuthScope scope = new AuthScope(this.proxy.getAddress(), this.proxy.getPort());

            Credentials cred = new UsernamePasswordCredentials(this.proxy.getUsername(), this.proxy.getPassword());
            creds.setCredentials(scope, cred);

            HttpHost proxy = new HttpHost(this.proxy.getAddress(), this.proxy.getPort());

            builder
                    .setProxy(proxy)
                    .setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy())
                    .setDefaultCredentialsProvider(creds);


        }

        // global configuration
        builder.setUserAgent(Config.INSTANCE.getUseragent())
                .setDefaultRequestConfig(globalConfig)
                .setDefaultCookieStore(this.cookieStore)
                .setRedirectStrategy(new LaxRedirectStrategy());

        this.client = builder.build();
    }

    private volatile boolean firstRun = true;

    @Override
    public void run() {
        synchronized (this) {
            try {
                System.out.println(String.format("(%d) Sending request", id));
                // submit request to splash page
                /*HttpClientContext context = HttpClientContext.create();
                context.setCookieStore(cookieStore);*/
                if(firstRun) {
                    CloseableHttpResponse initialRes = client.execute(new HttpGet(owner.getUrl()));
                    initialRes.close();
                    firstRun = false;
                }

                CloseableHttpResponse res = client.execute(new HttpGet(owner.getUrl()));
                InputStream data = res.getEntity().getContent();

                System.out.println("COOKIE LIST: " + cookieStore.toString());


                Set<String> foundSelectors = new HashSet<>();
                boolean contains = false;
                String result = IOUtils.toString(data, "UTF-8");

                for(String s : owner.getSelectors()) {
                    if(result.contains(s)) {
                        foundSelectors.add(s);
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

                    for(Header h : res.getAllHeaders()) {
                        System.out.println(h.getName() + ":" + h.getValue());
                    }

                    owner.getExecutor().submit(
                            new AdidasBrowser(owner, new BrowserData(proxy, cookieStore), res.getAllHeaders())
                    );

                    data.close();
                    res.close();
                } else {
                    data.close();
                    res.close();
                    Thread.sleep(owner.getDelay() * 1000);
                    if(owner.getIsDone().get()) {
                        client.close();
                        return;
                    } else {
                        run();
                    }
                }
            } catch(Exception ex) {
                return;
            }
        }
    }

    private void makeRequests() {
        while(!owner.getIsDone().get()) {
            System.out.println(String.format("(%d) sending another", id));
            new RequestRunnable().run();
            try {
                Thread.sleep(owner.getDelay() * 1000);
            } catch (InterruptedException e) {}
        }

    }

    private class RequestRunnable extends TimerTask implements Runnable {

        @Override
        public void run() {
            synchronized (this) {
                try {
                    System.out.println("sending");
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

                        for(Header h : res.getAllHeaders()) {
                            System.out.println(h.getName() + ":" + h.getValue());
                        }

                        owner.getExecutor().submit(
                                new AdidasBrowser(owner, new BrowserData(proxy, cookieStore), res.getAllHeaders())
                        );
                    }
                } catch(Exception ex) {
                    System.out.println("err");
                }
            }
        }
    }
}

