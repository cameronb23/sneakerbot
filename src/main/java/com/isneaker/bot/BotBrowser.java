package com.isneaker.bot;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.ProxyConfig;
import com.machinepublishers.jbrowserdriver.Settings;
import org.openqa.selenium.Cookie;

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

        // go to cart page
        this.driver.get("https://www.adidas.com/on/demandware.store/Sites-adidas-US-Site/en_US/Cart-Show");

        // set cookies and refresh
        for(int i = 0; i < settings.getCookies().getCookies().size(); i++) {
            org.apache.http.cookie.Cookie c = settings.getCookies().getCookies().get(i);
            Cookie newCookie = createCookie(c);


            this.driver.manage().addCookie(newCookie);
        }

        this.driver.navigate().refresh();


        System.out.println("Browser opened with status " + driver.getStatusCode());
    }

    public Cookie createCookie(org.apache.http.cookie.Cookie cookie) {
        Cookie newCookie = new Cookie.Builder(cookie.getName(), cookie.getValue())
                .domain(cookie.getDomain())
                .path(cookie.getPath())
                .expiresOn(cookie.getExpiryDate())
                .isSecure(cookie.isSecure())
                .build();

        return newCookie;
    }

}
