package me.cameronb.bot.task.adidas;

import me.cameronb.bot.Config;
import me.cameronb.bot.browser.BrowserData;
import me.cameronb.bot.proxy.BotProxy;
import me.cameronb.bot.task.TaskInstance;
import me.cameronb.bot.util.Region;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Cameron on 2/9/17.
 */
public class RequestChecker extends TaskInstance {
    private BasicCookieStore cookieStore;
    private CloseableHttpClient client;
    private AdidasBrowser browser;
    private final RequestTask owner;


    // SAVE COOKIES / SESSION DATA
    public RequestChecker(int id, BotProxy proxyConfig, RequestTask creator) {
        super(id, proxyConfig);
        this.owner = creator;

        this.cookieStore = new BasicCookieStore();

        HttpClientBuilder builder = HttpClientBuilder.create();

        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();

        if(getProxy() != null) {
            CredentialsProvider creds = new BasicCredentialsProvider();
            AuthScope scope = new AuthScope(getProxy().getAddress(), getProxy().getPort());

            Credentials cred = new UsernamePasswordCredentials(getProxy().getUsername(), getProxy().getPassword());
            creds.setCredentials(scope, cred);

            HttpHost proxy = new HttpHost(getProxy().getAddress(), getProxy().getPort());

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

        setStatus("Idle");
    }

    private AtomicBoolean firstRun = new AtomicBoolean(true);
    private AtomicBoolean paused = new AtomicBoolean(false);

    public void jig(Region r) {
        setStatus("Attempting to jig with " + r.getAbbrev() + " location.");
        paused.set(true);

        try {
            CloseableHttpResponse res = client.execute(new HttpGet(r.getUrl()));

            res.close();

            paused.set(false);
        } catch(IOException e) {
            setStatus("Errored: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        synchronized (this) {
            if(getStatus().toLowerCase().contains("error")) {
                return;
            }

            try {
                if(paused.get()) {
                    Thread.sleep(3000);
                    run();
                    return;
                }
                if(firstRun.get()) {
                    setStatus("Starting...");
                    firstRun.set(false);
                    Thread.sleep(1000 * getId());
                }

                setStatus("In splash");

                System.out.println(String.format("(%d) Sending request", getId()));
                // submit request to splash page
            /*HttpClientContext context = HttpClientContext.create();
            context.setCookieStore(cookieStore);*/
                if(firstRun.get()) {
                    CloseableHttpResponse initialRes = client.execute(new HttpGet(owner.getUrl()));
                    initialRes.close();
                    firstRun.set(false);
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

                for(Cookie c : cookieStore.getCookies()) {
                    if(c.getName().toLowerCase().contains("gceeqs") ||
                            c.getValue().toLowerCase().contains("hmac")) {
                        foundSelectors.add("dwsid");
                        contains = true;
                    }
                }

                if(contains) {
                    if(owner.isOnePass()) {
                        owner.getIsDone().set(true);
                    }

                    setStatus("Passed splash");
                    setSuccess(true);

                    System.out.println("Browser(" + getId() + ") passed splash(" + foundSelectors.toString() + ")");

                    System.out.println("Wait for browser to reload cart page with cookies.");

                /*for(Header h : res.getAllHeaders()) {
                    System.out.println(h.getName() + ":" + h.getValue());
                }*/

                    this.browser = new AdidasBrowser(owner, new BrowserData(getProxy(), cookieStore), res.getAllHeaders());
                    this.browser.run();
                    this.browser.open();

                    data.close();
                    res.close();
                    return;
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
                setStatus("Errored: " + ex.getMessage());
                return;
            }
        }
    }
}

