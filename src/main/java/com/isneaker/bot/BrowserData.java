package com.isneaker.bot;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by student on 5/19/17.
 */
public class BrowserData {

    @Getter
    private final BotProxy proxy;

    @Getter
    private final List<Cookie> cookies;

    public BrowserData(BotProxy proxy, List<Cookie> cookies) {
        this.proxy = proxy;

        this.cookies = cookies;
    }


}
