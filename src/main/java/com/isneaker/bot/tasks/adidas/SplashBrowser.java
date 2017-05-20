package com.isneaker.bot.tasks.adidas;

import com.isneaker.bot.Config;
import com.isneaker.bot.browser.BotBrowser;
import com.isneaker.bot.browser.BrowserData;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.ProxyConfig;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.UserAgent;
import com.sun.javafx.applet.Splash;
import org.openqa.selenium.Cookie;

import java.util.logging.Level;

/**
 * Created by Cameron on 5/19/17.
 */
public class SplashBrowser extends BotBrowser {

    public SplashBrowser(BrowserData data) {
        super(data);
    }

    @Override
    public void open() {
        // go to splash page (passed)
        getDriver().get(Config.INSTANCE.getSplashUrl());

        // set cookies and refresh
        for(int i = 0; i < getSettings().getCookies().getCookies().size(); i++) {
            org.apache.http.cookie.Cookie c = getSettings().getCookies().getCookies().get(i);
            Cookie newCookie = createCookie(c);


            getDriver().manage().addCookie(newCookie);
        }

        // refresh page
        getDriver().navigate().refresh();
        System.out.println("Browser opened with status " + getDriver().getStatusCode());
    }

    /**
     * Creates a Cookie object that is usable with the browser
     * @param cookie the HTTP response cookie
     * @return a cookie to pass to chrome browsers
     */
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
