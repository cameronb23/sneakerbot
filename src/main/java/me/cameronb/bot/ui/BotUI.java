package me.cameronb.bot.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;

/**
 * Created by Cameron on 5/20/2017.
 */
public class BotUI {

    @Getter
    private final Parent parent;

    @Getter
    private final Stage mainStage;


    public BotUI(Stage mainStage, Parent root) {
        this.parent = root;
        this.mainStage = mainStage;

        mainStage.setTitle("NabeelForce v1");

        Scene scene = new Scene(root, 800, 600);
        mainStage.setScene(scene);
        mainStage.show();
    }

}
