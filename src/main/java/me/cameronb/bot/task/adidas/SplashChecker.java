package me.cameronb.bot.task.adidas;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.*;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.ProxyConfig;
import com.machinepublishers.jbrowserdriver.Settings;
import lombok.Getter;
import lombok.Setter;
import me.cameronb.bot.proxy.BotProxy;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.openqa.selenium.*;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created by Cameron on 5/21/2017.
 */
public class SplashChecker implements Runnable {

    private final BotProxy proxy;
    private final SplashTask owner;
    private final HtmlUnitDriver driver;
    private JBrowserDriver checkout;
    private final int id;

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

    public SplashChecker(BotProxy proxy, SplashTask owner, int id) {
        this.proxy = proxy;
        this.owner = owner;
        this.id = id + 1;



        if(proxy != null) {
            driver = createDriver();
            Proxy p = new Proxy();
            p.setHttpProxy(proxy.getAddress() + ":" + proxy.getPort());
            driver.setProxySettings(p);
        } else {
            driver = new HtmlUnitDriver();
        }
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


    @Override
    public void run() {
        while(!owner.getIsDone().get()) {
            try {
                Thread.sleep(1000 * id); // wait 1 second for each additional browser to prevent spam
            } catch (InterruptedException e) {}

            driver.navigate().to(owner.getUrl());

            System.out.println(String.format("(%d) WAITING ON SPLASH", id));

            String found = null;

            while(found == null) {
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
            }

            System.out.println(String.format("(%d) PASSED SPLASH [%s]", id, found));


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
        }
    }

}
