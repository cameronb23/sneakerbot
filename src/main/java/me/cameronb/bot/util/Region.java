package me.cameronb.bot.util;

import lombok.Getter;

/**
 * Created by Cameron on 7/12/2017.
 */
public enum Region {

    UNITED_STATES("US"),
    CANADA("CA"),
    UNITED_KINGDOM("UK"),
    FRANCE("FR"),
    GERMANY("DE");

    public static final String URL_FORMAT = "http://www.adidas.com/%s/apps/yeezy";
    @Getter  private String abbrev;

    Region(String a) {
        this.abbrev = a;
    }

    public String getUrl() {
        return String.format(URL_FORMAT, this.abbrev.toLowerCase());
    }

    public static Region getRegion(String abbrev) {
        for(Region r : values()) {
            if(r.getAbbrev().equalsIgnoreCase(abbrev))
                return r;
        }
        return null;
    }

}
