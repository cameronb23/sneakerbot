package com.isneaker.bot;

import lombok.Getter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Cameron on 5/19/2017.
 *
 * from http://stackoverflow.com/a/27092826/7711910
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {

    private static final JAXBContext CONTEXT;
    public static final Config INSTANCE;

    // define configuration properties and default values
    @Getter
    private String mainUrl = "http://www.adidas.com/";

    @Getter
    private String splashUrl = "http://www.adidas.com/yeezy";

    @Getter
    private String cartUrl = "https://www.adidas.com/on/demandware.store/Sites-adidas-US-Site/en_US/Cart-Show";

    @Getter
    @XmlElement(type = Long.class)
    private long requestDelay = 10000;

    @Getter
    private int taskCount = 5;

    @Getter
    private String chromeDriverPath = "~/Downloads/chromedriver";

    @Getter
    private String useragent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";


    @Getter @XmlElementWrapper @XmlElement(name = "selector")
    private List<String> selectors = new ArrayList<>(Arrays.asList("data-sitekey="));

    // so it doesn't get initialized
    Config() {}

    static {
        try {
            CONTEXT = JAXBContext.newInstance(Config.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException("JAXB context for " + Config.class + " unavailable.", ex);
        }
        File applicationConfigFile = new File(System.getProperty("config", new File(System.getProperty("user.dir"), "config.xml").toString()));
        if (applicationConfigFile.exists()) {
            INSTANCE = loadConfig(applicationConfigFile);
        } else {
            INSTANCE = new Config();
        }
    }

    public static Config loadConfig(File file) {
        try {
            return (Config) CONTEXT.createUnmarshaller().unmarshal(file);
        } catch (JAXBException ex) {
            throw new IllegalArgumentException("Could not load configuration from " + file + ".", ex);
        }
    }

}
