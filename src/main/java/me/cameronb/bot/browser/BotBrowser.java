package me.cameronb.bot.browser;

import me.cameronb.bot.Config;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.ProxyConfig;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.UserAgent;
import lombok.Getter;

import java.util.logging.Level;

/**
 * Created by Cameron on 5/19/2017.
 */
public abstract class BotBrowser implements Runnable {

    @Getter
    private JBrowserDriver driver;

    @Getter
    private final BrowserData settings;

    private final ProxyConfig proxy;

    public BotBrowser() {
        this(null);
    }

    public BotBrowser(BrowserData settings) {
        this.settings = settings;

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
