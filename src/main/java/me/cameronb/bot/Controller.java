package me.cameronb.bot;

import com.sun.prism.paint.Paint;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BackgroundFill;
import lombok.Getter;
import me.cameronb.bot.task.Task;
import me.cameronb.bot.ui.controllers.AddTaskController;
import me.cameronb.bot.util.Console;

import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Cameron on 5/20/2017.
 */
public class Controller implements Initializable {

    @FXML @Getter
    private Label leftStatus;

    @FXML @Getter
    private Label rightStatus;

    @FXML @Getter
    private TextArea consoleOutput;

    ObservableList<Task> uiTasks = FXCollections.observableArrayList();

    @FXML
    private ListView<Task> taskListView;

    @FXML
    private Button startTasksButton;
    @FXML
    private Button stopTasksButton;

    @FXML
    public void openTaskCreation() {
        new AddTaskController();
    }

    public void addTask(Task t) {
        uiTasks.add(t);
        taskListView.setItems(uiTasks);
    }

    public void removeTask(Task t) {
        uiTasks.remove(t);
        taskListView.setItems(uiTasks);
    }

    public void updateTasks() {
        taskListView.setItems(uiTasks);
    }

    @FXML
    public void startTasks() {
        boolean success = BotApplication.getInstance().startTasks();
        if(success) {
            rightStatus.setText("Running");
            startTasksButton.setDisable(true);
            stopTasksButton.setDisable(false);
        }
    }

    @FXML
    public void stopTasks() {
        BotApplication.getInstance().stopTasks();
        rightStatus.setText("Idle");
        startTasksButton.setDisable(false);
        stopTasksButton.setDisable(true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        BotApplication.getInstance().setController(this);

        consoleOutput.setWrapText(true);

        /*Thread thread = new Thread(() -> {
            Console console = new Console(consoleOutput);
            PrintStream stream = new PrintStream(console, true);
            System.setOut(stream);
            System.setErr(stream);
        }, "CONSOLE-THREAD");
        thread.start();*/

        rightStatus.setText("Idle");

        taskListView.setEditable(true);
        taskListView.setCellFactory(param -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getTitle() == null) {
                    setStyle("-fx-background-color: white");
                    setText(null);
                } else {
                    if(item.isRunning()) {
                        setStyle("-fx-background-color: green");
                    } else {
                        setStyle("-fx-background-color: red");
                    }
                    setText(item.getTitle());
                }
            }
        });
        taskListView.setItems(uiTasks);
    }
}
