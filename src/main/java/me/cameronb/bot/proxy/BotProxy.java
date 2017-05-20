package me.cameronb.bot.proxy;

import lombok.Getter;

/**
 * Created by Cameron on 5/18/2017.
 */
public class BotProxy {

    @Getter
    private String address;

    @Getter
    private int port;

    @Getter
    private String username;

    @Getter
    private String password;

    public BotProxy(String address) {
        this(address, null, null);
    }

    public BotProxy(String address, String username, String password) {
        this.address = address.split(":")[0];
        this.port = Integer.parseInt(address.split(":")[1]);
        this.username = username;
        this.password = password;
    }

}
