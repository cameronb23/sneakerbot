package me.cameronb.bot.task.adidas;

import me.cameronb.bot.browser.BotBrowser;
import me.cameronb.bot.browser.BrowserData;
import me.cameronb.bot.task.Task;
import org.apache.http.Header;
import org.openqa.selenium.Cookie;

/**
 * Created by Cameron on 5/19/17.
 */
public class AdidasBrowser extends BotBrowser {

    private Task owner;

    public AdidasBrowser(Task owner, BrowserData data, Header... headers) {
        super(data, headers);
        this.owner = owner;
    }

    @Override
    public void open() {
        // go to splash page (passed)
        getDriver().get(owner.getUrl());

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
