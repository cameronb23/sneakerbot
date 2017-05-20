package me.cameronb.bot.proxy;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cameron on 5/18/17.
 */
public class ProxyLoader {

    @Getter
    private final Map<BotProxy, Boolean> proxiesLoaded = new HashMap<>();

    public ProxyLoader(File f) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(f));

        String line;
        int lineIndex = 0;

        while((line = reader.readLine()) != null) {
            lineIndex++;
            String[] data = line.split(":");
            int len = data.length;
            if(len == 2) {
                proxiesLoaded.put(new BotProxy(data[0] + ":" + data[1]), false);
            } else if(len == 4) {
                proxiesLoaded.put(new BotProxy(data[0] + ":" + data[1], data[2], data[3]), false);
            } else {
                System.out.println("could not read line #" + lineIndex);
            }
        }

    }

    public void markUsed(BotProxy p) {
        this.proxiesLoaded.put(p, true);
    }

}
