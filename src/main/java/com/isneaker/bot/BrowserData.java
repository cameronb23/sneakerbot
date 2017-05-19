package com.isneaker.bot;

import lombok.Getter;
import org.apache.http.client.CookieStore;

/**
 * Created by student on 5/19/17.
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
