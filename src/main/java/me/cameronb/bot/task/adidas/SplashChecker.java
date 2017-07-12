package me.cameronb.bot.task.adidas;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.*;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.ProxyConfig;
import com.machinepublishers.jbrowserdriver.Settings;
import lombok.Getter;
import lombok.Setter;
import me.cameronb.bot.proxy.BotProxy;
import me.cameronb.bot.task.TaskInstance;
import me.cameronb.bot.util.Region;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.openqa.selenium.*;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * Created by Cameron on 5/21/2017.
 */
public class SplashChecker extends TaskInstance {

    private final BotProxy proxy;
    private final SplashTask owner;
    private final HtmlUnitDriver driver;
    private JBrowserDriver checkout;

    private HtmlUnitDriver createDriver() {
        return new HtmlUnitDriver() {
            @Override
            protected WebClient modifyWebClient(WebClient client) {
                CredentialsProvider creds = new DefaultCredentialsProvider();
                Credentials credentials = new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword());
                creds.setCredentials(new AuthScope(proxy.getAddress(), proxy.getPort()), credentials);

                client.setCredentialsProvider(creds);
                return client;
            }
        };
    }

    /*private JBrowserDriver createDriver() {
        ProxyConfig proxyConfig = configureProxy();

        return new JBrowserDriver(build(proxyConfig));
    }*/

    public SplashChecker(int id, BotProxy proxy, SplashTask owner) {
        super(id, proxy);
        this.proxy = proxy;
        this.owner = owner;



        if(proxy != null) {
            driver = createDriver();
            Proxy p = new Proxy();
            p.setHttpProxy(proxy.getAddress() + ":" + proxy.getPort());
            driver.setProxySettings(p);
        } else {
            driver = new HtmlUnitDriver();
        }

        setStatus("Idle");
    }

    public void stop() {
        done = true;
        driver.close();
        if(checkout != null) checkout.close();
    }



    private ProxyConfig configureProxy() {
        ProxyConfig p;
        if(proxy == null) return null;
        if(proxy.getPassword() != null) {
            p = new ProxyConfig(
                    ProxyConfig.Type.HTTP,
                    proxy.getAddress(),
                    proxy.getPort(),
                    proxy.getUsername(),
                    proxy.getPassword());
        } else {
            p = new ProxyConfig(
                    ProxyConfig.Type.HTTP,
                    proxy.getAddress(),
                    proxy.getPort());
        }
        return p;
    }

    private Settings build(ProxyConfig p) {
        // create settings object
        Settings.Builder settingBuilder = Settings.builder()
                //.userAgent(new UserAgent(null, null, null, null, null, Config.INSTANCE.getUseragent()))
                .headless(false)
                .loggerLevel(Level.SEVERE)
                .logTrace(false)
                .logWarnings(false);
        //.userAgent(Bot.USERAGENT_OBJ)

        if(p != null) {
            settingBuilder = settingBuilder.proxy(p);
        }

        return settingBuilder.build();
    }

    @Getter @Setter
    private boolean done = false;

    private boolean firstRun = true;
    private AtomicBoolean paused = new AtomicBoolean(false);

    public void jig(Region r) {
        setStatus("Attempting to jig with " + r.getAbbrev() + " location.");
        paused.set(true);

        try {
            // todo
            driver.navigate().to(r.getUrl());

            Thread.sleep(10000);

            driver.navigate().to(owner.getUrl());

            paused.set(false);
        } catch(Exception e) {
            setStatus("Errored: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                if(paused.get()) {
                    Thread.sleep(owner.getDelay());
                    run();
                    return;
                }
                if(firstRun) {
                    setStatus("Starting...");
                    System.out.println(String.format("(%d) Starting process...", this.getId()));
                    driver.navigate().to(owner.getUrl());
                    firstRun = false;
                }
                setStatus("On splash page");

                String found = null;

                for (String s : owner.getSelectors()) {
                    try {
                        if(driver.findElementByCssSelector(s) != null) {
                            found = s;
                        }
                    } catch (NoSuchElementException e) {
                    } catch(NoSuchWindowException e) {
                        break;
                    }
                }

                if(found != null) {
                    System.out.println(String.format("(%d) PASSED SPLASH [%s]", this.getId(), found));

                    setStatus("Passed splash");
                    setSuccess(true);

                    System.out.println("LAST PAGE: " + driver.getCurrentUrl());
                    System.out.println("ORIGINAL: " + driver.manage().getCookies());

                    ProxyConfig proxyConfig = configureProxy();

                    checkout = new JBrowserDriver(build(proxyConfig));

                    checkout.navigate().to(driver.getCurrentUrl());

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {}

                    Set<Cookie> cookieSet = driver.manage().getCookies();

                    System.out.println("OLD:" + cookieSet);

                    for(Cookie c : cookieSet) {
                        checkout.manage().addCookie(c);
                    }

                    System.out.println("NEW:" + checkout.manage().getCookies());

                    checkout.get(driver.getCurrentUrl());

                    if(owner.isOnePass()) {
                        owner.getIsDone().set(true);
                    }
                } else {
                    Thread.sleep(owner.getDelay());
                    run();
                }
            } catch(Exception e) {
                setStatus("Errored: " + e.getMessage());
                return;
            }
        }
    }

}
