package me.cameronb.bot.task.adidas;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.ProxyConfig;
import com.machinepublishers.jbrowserdriver.Settings;
import me.cameronb.bot.proxy.BotProxy;
import me.cameronb.bot.task.TaskInstance;
import me.cameronb.bot.util.Region;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * Created by Cameron on 5/21/2017.
 */
public class SplashChecker extends TaskInstance {

    private final Object lock = new Object();
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

    private AtomicBoolean running = new AtomicBoolean(true);
    private AtomicBoolean paused = new AtomicBoolean(false);
    private boolean wasJigged = false;

    public void jig(Region r) {
        setStatus("Attempting to bypass with " + r.getAbbrev() + " location.");
        paused.set(true);

        try {
            driver.navigate().to(r.getUrl());

            wasJigged = true;
            paused.set(false);
        } catch(Exception e) {
            setStatus("Errored: " + e.getMessage());
        }
    }

    @Override
    public void start() {
        setStatus("Starting...");
        System.out.println(String.format("(%d) Starting process...", this.getId()));
        driver.navigate().to(owner.getUrl());
        run();
    }

    @Override
    public void end() {
        running.set(false);
        driver.close();
        if(checkout != null) checkout.close();
    }

    public void run() {
        if(!running.get()) {
            return;
        }
        try {
            if(wasJigged) {
                wasJigged = false;
                driver.navigate().to(owner.getUrl());
                sleep(5000);
                run();
                return;
            }
            if(paused.get()) {
                sleep(owner.getDelay());
                run();
                return;
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
                    sleep(5000);
                } catch (InterruptedException e) {}

                Set<Cookie> cookieSet = driver.manage().getCookies();

                System.out.println("OLD:" + cookieSet);

                cookieSet.forEach(c -> checkout.manage().addCookie(c));

                System.out.println("NEW:" + checkout.manage().getCookies());

                checkout.get(driver.getCurrentUrl());

                if(owner.isOnePass()) {
                    owner.getIsDone().set(true);
                }
            } else {
                sleep(owner.getDelay());
                run();
            }
        } catch(Exception e) {
            setStatus("Errored: " + e.getMessage());
            return;
        }
    }

}
