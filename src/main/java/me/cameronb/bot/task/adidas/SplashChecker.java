package me.cameronb.bot.task.adidas;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.ProxyConfig;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.UserAgent;
import me.cameronb.bot.Config;
import me.cameronb.bot.browser.BotBrowser;
import me.cameronb.bot.browser.BrowserData;
import me.cameronb.bot.proxy.BotProxy;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.util.logging.Level;

/**
 * Created by Cameron on 5/21/2017.
 */
public class SplashChecker implements Runnable {

    private final BotProxy proxy;
    private final SplashTask owner;
    private final HtmlUnitDriver driver;
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

    public SplashChecker(BotProxy proxy, SplashTask owner, int id) {
        this.proxy = proxy;
        this.owner = owner;
        this.id = id;

        if(proxy != null) {
            driver = createDriver();
            Proxy p = new Proxy();
            p.setHttpProxy(proxy.getAddress() + ":" + proxy.getPort());
            driver.setProxySettings(p);
        } else {
            driver = new HtmlUnitDriver();
        }
    }



    private ProxyConfig configureProxy() {
        ProxyConfig p;
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


    @Override
    public void run() {
        System.out.println(String.format("(%) WAITING ON SPLASH", id));
        driver.get(owner.getUrl());


        while(driver.findElement(By.className("sk-fading-circle")).isDisplayed()) {
            try {
                Thread.sleep(owner.getDelay());
            } catch(InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(String.format("(%d) PASSED SPLASH.", id));

        boolean found = false;

        while(!found) {
            for(String s : owner.getSelectors()) {
                if(driver.findElement(By.cssSelector(s)) != null) {
                    found = true;
                }
            }
        }

        ProxyConfig proxyConfig = configureProxy();

        WebDriver checkout = new JBrowserDriver(build(proxyConfig));

        for(Cookie c : driver.manage().getCookies()) {
            checkout.manage().addCookie(c);
        }

        checkout.get(owner.getUrl());
    }

}
