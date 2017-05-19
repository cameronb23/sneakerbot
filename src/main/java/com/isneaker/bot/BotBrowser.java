package com.isneaker.bot;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.ProxyConfig;
import com.machinepublishers.jbrowserdriver.RequestHeaders;
import com.machinepublishers.jbrowserdriver.Settings;
import org.apache.http.Header;
import org.openqa.selenium.Cookie;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.logging.Level;

/**
 * Created by student on 5/19/17.
 */
public class BotBrowser {

    private final JBrowserDriver driver;

    public BotBrowser(BrowserData settings) {

        // build the proxy object

        ProxyConfig proxy = null;

        if(settings.getProxy() != null) {
            if(settings.getProxy().getPassword() != null) {
                proxy = new ProxyConfig(
                        ProxyConfig.Type.HTTP,
                        settings.getProxy().getAddress(),
                        settings.getProxy().getPort(),
                        settings.getProxy().getUsername(),
                        settings.getProxy().getPassword());
            } else {
                proxy = new ProxyConfig(
                        ProxyConfig.Type.HTTP,
                        settings.getProxy().getAddress(),
                        settings.getProxy().getPort());
            }
        }


        Settings.Builder set = Settings.builder()
                .headless(false)
                .loggerLevel(Level.SEVERE)
                .logTrace(false)
                .logWarnings(false);
                //.userAgent(BotMain.USERAGENT_OBJ)

        if(proxy != null) {
            set = set.proxy(proxy);
        }



        this.driver = new JBrowserDriver(set.build());

        this.driver.get("https://www.adidas.com/on/demandware.store/Sites-adidas-US-Site/en_US/Cart-Show");

        for(io.netty.handler.codec.http.cookie.Cookie old : settings.getCookies()) {
            Cookie newCookie = new Cookie.Builder(old.name(), old.value()).build();



            this.driver.manage().addCookie(newCookie);
        }

        this.driver.navigate().refresh();


        System.out.println(driver.getStatusCode());
    }

}
