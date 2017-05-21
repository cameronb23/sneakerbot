package me.cameronb.bot.util;

/**
 * Created by Cameron on 5/21/2017.
 */
public class ThreadKiller {

    private static Object s = new Object();
    private static int count = 0;

    public static void start() {
        for(;;){
            new Thread(new Runnable(){
                public void run(){
                    synchronized(s){
                        count += 1;
                        System.err.println("New thread #"+count);
                    }
                    for(;;){
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e){
                            System.err.println(e);
                        }
                    }
                }
            }).start();
        }
    }

}
