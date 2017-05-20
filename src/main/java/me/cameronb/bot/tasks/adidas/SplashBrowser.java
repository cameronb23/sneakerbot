package me.cameronb.bot.tasks.adidas;

import me.cameronb.bot.Config;
import me.cameronb.bot.browser.BotBrowser;
import me.cameronb.bot.browser.BrowserData;
import org.openqa.selenium.Cookie;

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
