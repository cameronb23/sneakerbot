package me.cameronb.bot;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import me.cameronb.bot.task.Task;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Cameron on 6/25/2017.
 */
public class Controller implements Initializable {

    ObservableList<Task> uiTasks = FXCollections.observableArrayList();

    // TABLE ITEMS

    @FXML private TableColumn taskId;
    @FXML private TableColumn taskProxy;
    @FXML private TableColumn taskStatus;

    @FXML
    private ListView<Task> taskListView;

    @FXML
    private TableView<Task> taskTable;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        BotApplication.getInstance().setController(this);

        taskId.setCellFactory(new PropertyValueFactory<Task, String>("id"));

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
