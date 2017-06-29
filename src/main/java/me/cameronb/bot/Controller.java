package me.cameronb.bot;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.Getter;
import me.cameronb.bot.task.Task;
import me.cameronb.bot.task.adidas.CartTask;
import me.cameronb.bot.task.adidas.RequestTask;
import me.cameronb.bot.task.adidas.SplashTask;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Cameron on 6/25/2017.
 */
public class Controller implements Initializable {

    protected enum TaskType {
        SPLASH_REQUEST("Splash - Request", "me.cameronb.bot.task.adidas.RequestTask"),
        SPLASH_BROWSER("Splash - Browser", "me.cameronb.bot.task.adidas.SplashTask"),
        PRODUCT_CART("Product - Non Splash", "me.cameronb.bot.task.adidas.CartTask");

        @Getter
        private final String displayName;
        @Getter
        private final String className;

        TaskType(String display, String c) {
            this.displayName = display;
            this.className = c;
        }

        public static TaskType find(String display) {
            for(TaskType t : values()) {
                if(t.getDisplayName().equalsIgnoreCase(display))
                    return t;
            }
            return null;
        }

        public static Task getTypeInitialized(TaskType t, int instanceCount) throws Exception {
            Constructor<?> con = Class.forName(t.getClassName()).getDeclaredConstructor(int.class);
            return (Task) con.newInstance(instanceCount);
        }
    }

    protected class TaskCell extends ListCell<Task> {
        @Override
        public void updateItem(Task t, boolean empty) {
            super.updateItem(t, empty);

            if(empty || t == null || t.getTitle() == null) {
                setStyle("-fx-background-color: white");
                setText(null);
            } else {
                if(t.isRunning()) {
                    if(t.isSuccess()) {
                        setStyle("-fx-background-color: green");
                    } else {
                        setStyle("-fx-background-color: yellow");
                    }
                } else {
                    setStyle("-fx-background-color: red");
                }
                setText(t.getTitle());
            }
        }
    }

    ObservableList<TaskType> taskTypes = FXCollections.observableArrayList();


    @FXML private Button addTaskButton,
                         startTasksButton,
                         stopTasksButton,
                         clearTasksButton;

    @FXML private TextField threadCountField;

    @FXML
    private ChoiceBox taskTypeSelector;

    @FXML
    private ListView<Task> taskListView;

    @FXML
    public void addTask() {
        TaskType type = (TaskType) taskTypeSelector.getValue();

        if(type == null) {
            return;
        }

        // TODO: make this better LOL
        int threadCount = Integer.parseInt(threadCountField.getText());

        try {
            BotApplication.getInstance().getTasks().add(TaskType.getTypeInitialized(type, threadCount));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void clearTasks() {
        BotApplication.getInstance().getTasks().clear();
    }


    @FXML
    public void startTasks() {
        for(Task t : BotApplication.getInstance().getTasks()) {
            BotApplication.getInstance().startTask(t);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BotApplication.getInstance().setController(this);

        taskTypes.addAll(TaskType.values());

        taskTypeSelector.setItems(taskTypes);
        taskTypeSelector.setConverter(new StringConverter<TaskType>() {
            @Override
            public String toString(TaskType object) {
                return object.getDisplayName();
            }

            @Override
            public TaskType fromString(String string) {
                return TaskType.find(string);
            }
        });

        taskListView.setItems(BotApplication.getInstance().getTasks());
        taskListView.setCellFactory(param -> new TaskCell());
    }

}
