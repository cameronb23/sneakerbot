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
import me.cameronb.bot.task.TaskInstance;
import me.cameronb.bot.task.adidas.CartTask;
import me.cameronb.bot.task.adidas.RequestTask;
import me.cameronb.bot.task.adidas.SplashTask;
import sun.reflect.generics.tree.Tree;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

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

    protected class TaskCell extends TreeCell<Object> {
        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            if(empty || item == null) {
                setStyle("-fx-background-color: white");
                setText(null);
            } else {
                setStyle(getStyle(item));
                setText(getText(item));
            }
        }

        private String getText(Object item) {
            if(item instanceof Task) {
                return (((Task) item).getTitle());
            } else {
                TaskInstance i = (TaskInstance) item;
                return (i.getId() + 1) + " - " + i.getStatus();
            }
        }

        private String getStyle(Object item) {
            if(item instanceof Task) {
                if(((Task) item).isRunning())
                    return "-fx-background-color: green";
                else
                    return "-fx-background-color: red";
            } else {
                if(((TaskInstance) item).isSuccess())
                    return "-fx-background-color: green";
                else
                    return "-fx-background-color: yellow";
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

    @FXML private TreeView<Object> taskListView;

    @FXML
    public void addTask() {
        TaskType type = (TaskType) taskTypeSelector.getValue();

        if(type == null) {
            return;
        }

        // TODO: make this better LOL
        int threadCount = Integer.parseInt(threadCountField.getText());

        try {
            Task t = TaskType.getTypeInitialized(type, threadCount);

            BotApplication.getInstance().getTasks().add(t);

            TreeItem<Object> taskItem = new TreeItem<>(t);

            for(TaskInstance i : ((ObservableList<TaskInstance>) t.getInstances())) {
                taskItem.getChildren().add(new TreeItem<>(i));
            }

            taskListView.getRoot().getChildren().add(taskItem);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void clearTasks() {
        BotApplication.getInstance().getTasks().clear();

        taskListView.getRoot().getChildren().clear();
    }


    @FXML
    public void startTasks() {
        for(Task t : BotApplication.getInstance().getTasks()) {
            BotApplication.getInstance().startTask(t);
        }
    }

    @FXML
    public void stopTasks() {
        for(Task t : BotApplication.getInstance().getTasks()) {
            t.end();
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

        taskListView.setCellFactory(param -> new TaskCell());
        TreeItem<Object> root = new TreeItem<>("is not used");
        taskListView.setRoot(root);
        //taskListView.setEditable(false);
        taskListView.setShowRoot(false);

        new Thread(new Runnable() {
            @Override
            public void run() {

                for(TreeItem task : taskListView.getRoot().getChildren()) {
                    Task t = (Task) task.getValue();

                    task.setValue(null);
                    task.setValue(t);

                    for(Object j : task.getChildren()) {
                        TreeItem i = (TreeItem) j;
                        TaskInstance instance = (TaskInstance) i.getValue();

                        i.setValue(null);
                        i.setValue(instance);
                    }
                }
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    run();
                    return;
                }
                run();
            }
        }).start();


//        taskListView.setItems(BotApplication.getInstance().getTasks());
//        taskListView.setCellFactory(param -> new TaskCell());
    }

}
