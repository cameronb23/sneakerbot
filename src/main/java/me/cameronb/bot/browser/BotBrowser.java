package me.cameronb.bot.browser;

import com.machinepublishers.jbrowserdriver.*;
import me.cameronb.bot.Config;
import lombok.Getter;
import org.apache.http.Header;
import org.apache.http.HeaderElement;

import java.util.LinkedHashMap;
import java.util.logging.Level;

/**
 * Created by Cameron on 5/19/2017.
 */
public abstract class BotBrowser implements Runnable {

    @Getter
    private JBrowserDriver driver;

    @Getter
    private final BrowserData settings;

    @Getter
    private final Header[] headers;

    private final ProxyConfig proxy;

    public BotBrowser() {
        this(null);
    }

    public BotBrowser(BrowserData settings, Header... headers) {
        this.settings = settings;
        this.headers = headers;

        // build the proxy object

        if(settings != null && settings.getProxy() != null) {
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
        } else {
            proxy = null;
        }
    }

    @Override
    public void run() {
        LinkedHashMap<String, String> httpHeaders = new LinkedHashMap<>();
        LinkedHashMap<String, String> httpsHeaders = new LinkedHashMap<>();

        for(Header h : headers) {
            System.out.println(h.getName() + ":" + h.getValue());
            for(HeaderElement e : h.getElements()) {
                System.out.println("--" + e.getName() + ":" + e.getValue() + " / " + e.getParameters().toString());
            }
        }

        // create settings object
        Settings.Builder settingBuilder = Settings.builder()
                .userAgent(new UserAgent(null, null, null, null, null, Config.INSTANCE.getUseragent()))
                .headless(false)
                .loggerLevel(Level.SEVERE)
                .logTrace(false)
                .logWarnings(false);
        //.userAgent(Bot.USERAGENT_OBJ)

        if(proxy != null) {
            settingBuilder = settingBuilder.proxy(proxy);
        }

        this.driver = new JBrowserDriver(settingBuilder.build());

        this.open();
    }

    abstract public void open();

}
