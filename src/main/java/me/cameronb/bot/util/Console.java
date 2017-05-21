package me.cameronb.bot.util;

import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Cameron on 5/20/2017.
 *
 * from http://stackoverflow.com/q/13841884/7711910
 */
public class Console extends OutputStream {

    private TextArea output;

    public Console(TextArea target) {
        this.output = target;
    }

    @Override
    public void write(int i) throws IOException {
        output.appendText(String.valueOf((char) i));
    }

}
