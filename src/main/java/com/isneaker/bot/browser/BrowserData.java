package com.isneaker.bot.browser;

import com.isneaker.bot.proxy.BotProxy;
import lombok.Getter;
import org.apache.http.client.CookieStore;

/**
 * Created by Cameron on 5/19/17.
 */
public class BrowserData {

    @Getter
    private final BotProxy proxy;

    @Getter
    private final CookieStore cookies;

    public BrowserData(BotProxy proxy, CookieStore cookies) {
        this.proxy = proxy;

        this.cookies = cookies;
    }


}
