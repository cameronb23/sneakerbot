package me.cameronb.bot.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.cameronb.bot.BotApplication;
import me.cameronb.bot.task.adidas.RequestTask;
import me.cameronb.bot.task.adidas.SplashTask;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Cameron on 5/20/2017.
 */
public class AddTaskController implements Initializable {

    private Scene scene;

    @FXML private ChoiceBox<String> typeSelect;

    @FXML private TextField splashUrl;
    @FXML private TextField instanceCount;
    @FXML private TextField selectors;
    @FXML private TextField requestDelay;
    @FXML private CheckBox onePass;



    public AddTaskController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskSelection.fxml"));

        loader.setController(this);

        try {
            Parent parent = loader.load();
            this.scene = new Scene(parent);

            Stage stage = new Stage();
            stage.setTitle("Add Task");
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setScene(scene);
            stage.show();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        ((Stage) this.scene.getWindow()).close();
    }

    private boolean checkValues() {
        if(typeSelect.getValue() == null) return false;
        if(splashUrl.getText() == null) return false;
        if(instanceCount.getText() == null) return false;
        if(selectors.getText() == null) return false;
        if(requestDelay.getText() == null) return false;
        return true;
    }

    @FXML
    public void addTask() {
        if(!checkValues()) return;

        System.out.println("Added task type " + typeSelect.getValue());

        switch(typeSelect.getValue().toLowerCase()) {
            case "adidas splash": {
                BotApplication.getInstance().addTask(new SplashTask(
                        splashUrl.getText(),
                        Long.parseLong(requestDelay.getText()),
                        Integer.parseInt(instanceCount.getText()),
                        selectors.getText().split(","),
                        onePass.isSelected()
                ));
                close();
            }
            /*case "adidas request": {
                BotApplication.getInstance().addTask(new RequestTask(
                        splashUrl.getText(),
                        Long.parseLong(requestDelay.getText()),
                        Integer.parseInt(instanceCount.getText()),
                        selectors.getText().split(","),
                        onePass.isSelected()
                ));
                close();
            }*/
            default:
                return;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // initialize task types
        typeSelect.getItems().add("Adidas Splash");
        //typeSelect.getItems().add("Adidas Request");
    }
}
