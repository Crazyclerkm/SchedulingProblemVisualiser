package visualiser;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import parser.InputParser;
import parser.ParseException;
import scheduler.Schedule;
import scheduler.ScheduledTask;
import scheduler.Scheduler;
import scheduler.SchedulerListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {
    @FXML
    Button selectScheduleButton;

    @FXML
    Spinner<Integer> processorSpinner;

    @FXML
    ProgressIndicator progressIndicator;

    @FXML
    CheckBox multithreadCheckbox;

    @FXML
    Label scheduleName;

    @FXML
    Label processorsLabel;

    @FXML
    Label multithreadingLabel;

    @FXML
    ImageView taskGraphView;

    @FXML
    LineChart<String, Number> ramChart;

    @FXML
    ListView<Pair<Schedule, Integer>> scheduleListView;

    @FXML
    StackedBarChart<String, Integer> scheduleChart;

    @FXML
    Pane imageViewPane;

    @FXML
    Button saveScheduleButton;

    @FXML
    ProgressIndicator taskGraphIndicator;

    @FXML
    Label taskGraphLabel;

    @FXML
    Label timeTakenLabel;

    @FXML
    Button findScheduleButton;

    private File selectedFile;
    private InputParser inputParser = null;
    private Map<File, Integer> fileScheduleCount;


    @FXML
    void initialize() {
        fileScheduleCount = new HashMap<>();

        imageViewPane.setBorder(new Border(new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        scheduleListView.setPlaceholder(new Label("No schedules created"));

        // Set appropriate text when a schedule is added to the list view
        scheduleListView.setCellFactory(param -> new ListCell<Pair<Schedule, Integer>>() {
            @Override
            protected void updateItem(Pair<Schedule, Integer> item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(selectedFile.getName() + " Schedule " + item.getValue() + ", Length=" + item.getKey().getLength());
                }
            }
        });

        // Select new items added to the list view
        scheduleListView.getItems().addListener((ListChangeListener<Pair<Schedule, Integer>>) c -> scheduleListView.getSelectionModel().selectLast());

        // Update the scheduleChart when a new schedule is selected
        scheduleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateScheduleChart(newValue.getKey()));
    }

    private XYChart.Data<String, Integer> createTaskData(int processor, int length, boolean isGap) {
        XYChart.Data<String, Integer> data = new XYChart.Data<>("P" + processor, length);

        data.nodeProperty().addListener((observable, oldValue, newValue) -> {
            if (isGap) {
                newValue.setVisible(false);
            } else {
                newValue.setStyle("-fx-border-width: 2px");
                newValue.setStyle("-fx-border-style: solid");
            }
        });

        return data;
    }

    private void updateScheduleChart(Schedule schedule) {
        scheduleChart.getData().clear();

        int numProcessors = schedule.getNumProcessors();
        int[] startTimes = schedule.getProcStartTime();

        for (int i = 0; i < numProcessors; i++) {
            List<ScheduledTask> taskList = schedule.getProcessorTaskList(i);

            XYChart.Series<String, Integer> series = new XYChart.Series<>();

            // If processor does not have a task scheduled at time 0 add a gap
            if (startTimes[i] != 0) {
                series.getData().add(createTaskData(i, startTimes[i], true));
            }

            ScheduledTask task;
            if (taskList.size() > 0) {
                task = taskList.get(0);
            } else {
                break;
            }

            series.getData().add(createTaskData(i, task.getWeight(), false));

            for (int j = 1; j < taskList.size(); j++) {
                ScheduledTask s = taskList.get(j);
                ScheduledTask pre = taskList.get(j - 1);

                // If there's a gap between the last task and this task then add that gap
                if (s.getStartTime() != (pre.getStartTime() + pre.getWeight())) {
                    series.getData().add(createTaskData(i, s.getStartTime() - pre.getStartTime() + pre.getWeight(), true));
                }

                series.getData().add(createTaskData(i, s.getWeight(), false));

            }
            scheduleChart.getData().add(series);
        }

    }

    @FXML
    public void findSchedule() {
        if (selectedFile != null) {
            String inputTasks = selectedFile.getAbsolutePath();

            changeSettingsStatus(true);

            try {
                inputParser = new InputParser(inputTasks);
            } catch (IOException e) {
                showErrorMessage("Error reading file", "There was a problem with reading " + selectedFile.getName());
            } catch (ParseException e) {
                showErrorMessage("Error reading file", "The input file is in an invalid format");
            }

            Scheduler scheduler = new Scheduler(inputParser.getTasks(), processorSpinner.getValue());


            int numProcessors = 1;
            if (multithreadCheckbox.isSelected()) numProcessors = 2;

            int finalNumProcessors = numProcessors;

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            ramChart.getData().clear();
            ramChart.getData().add(series);

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

            Task<Schedule> schedulerTask = new Task<Schedule>() {
                @Override
                protected Schedule call() {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    executorService.scheduleAtFixedRate(() -> {

                        // Get ram usage and add it to chart, remove old data points
                        Runtime runtime = Runtime.getRuntime();
                        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                        long percentMemory = (long) ((float) usedMemory / (runtime.totalMemory()) * 100);

                        Platform.runLater(() -> {
                            Date time = new Date();
                            series.getData().add(new XYChart.Data<>(dateFormat.format(time), percentMemory));

                            if (series.getData().size() > 15) {
                                series.getData().remove(0);
                            }
                        });

                    }, 0, 1, TimeUnit.SECONDS);

                    SchedulerListener listener = new SchedulerListener() {
                        @Override
                        public void notifyInit(Schedule bound, int threadCount) {
                            addNewSchedule(bound);
                        }

                        @Override
                        public void notifyBound(int thread, Schedule bound) {
                            addNewSchedule(bound);
                        }

                        @Override
                        public void notifyConnections(int thread, Schedule parent, List<Schedule> children) {

                        }

                        @Override
                        public void notifyPop(int thread, Schedule schedule) {

                        }
                    };


                    // Time the execution of finding the optimal schedule
                    Timer timer = new Timer();
                    timeTakenLabel.setVisible(true);
                    timer.schedule(new TimerTask() {
                        final Instant start = Instant.now();

                        @Override
                        public void run() {
                            Instant now = Instant.now();
                            Duration d = Duration.between(start, now);
                            Platform.runLater(() -> {
                                long total = d.toMillis();
                                long millis = total % 60;
                                long seconds = (total / 1000) % 60;
                                long minutes = (total / (60 * 1000)) % 60;
                                long hours = (total / (3600 * 1000)) % 24;
                                timeTakenLabel.setText(String.format("%d:%02d:%02d:%02d", hours, minutes, seconds, millis));
                            });

                        }
                    }, 0, 50);
                    Schedule schedule = scheduler.findBestSchedule(finalNumProcessors, listener);
                    timer.cancel();
                    return schedule;
                }

            };

            schedulerTask.setOnSucceeded(e -> {
                changeSettingsStatus(false);
                executorService.shutdown();
            });

            new Thread(schedulerTask).start();

        } else {
            showErrorMessage("No Schedule Selected", "Please select a schedule");
        }
    }

    private void showErrorMessage(String title, String text) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setContentText(text);
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE));
        dialog.showAndWait();
    }

    private void addNewSchedule(Schedule bound) {
        if (!fileScheduleCount.containsKey(selectedFile)) {
            fileScheduleCount.put(selectedFile, 0);
        }

        int currentCount = fileScheduleCount.get(selectedFile);

        Pair<Schedule, Integer> pair = new Pair<>(bound, currentCount);
        Platform.runLater(() -> scheduleListView.getItems().add(pair));
        fileScheduleCount.put(selectedFile, currentCount + 1);
    }

    private void changeSettingsStatus(boolean status) {
        findScheduleButton.setDisable(status);
        selectScheduleButton.setDisable(status);
        processorSpinner.setDisable(status);
        multithreadCheckbox.setDisable(status);
        processorsLabel.setDisable(status);
        multithreadingLabel.setDisable(status);
        progressIndicator.setVisible(status);
    }

    private File chooseDotFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open dot File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Dot Files", "*.dot"));

        return fileChooser.showOpenDialog(selectScheduleButton.getScene().getWindow());
    }

    private File saveDotFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save dot File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Dot Files", "*.dot"));

        return fileChooser.showSaveDialog(saveScheduleButton.getScene().getWindow());
    }

    @FXML
    public void selectSchedule() {
        File newSelection = chooseDotFile();

        if (newSelection != null) {
            selectedFile = newSelection;
            scheduleName.setText(selectedFile.getName());
            displayTaskGraph();

        }
    }

    private void displayTaskGraph() {
        try {
            MutableGraph g = new Parser().read(selectedFile);

            File out = new File("taskGraph.png");
            Task<Image> graphvizTask = new Task<Image>() {
                @Override
                protected Image call() throws Exception {
                    Graphviz.fromGraph(g).width(400).height(308).render(Format.PNG).toFile(out);
                    return new Image(String.valueOf(out.toURI()));
                }
            };

            graphvizTask.setOnSucceeded(e -> {
                taskGraphIndicator.setVisible(false);
                taskGraphView.setImage(graphvizTask.getValue());
                taskGraphView.setPreserveRatio(true);
                taskGraphView.setFitWidth(400);
                taskGraphView.setFitHeight(400);
            });

            taskGraphView.setImage(null);
            taskGraphIndicator.setVisible(true);
            taskGraphLabel.setVisible(false);

            new Thread(graphvizTask).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSchedule() {
        Schedule selectedSchedule = scheduleListView.getSelectionModel().getSelectedItem().getKey();
        if (selectedSchedule != null) {
            File outputFile = saveDotFile();
            if (outputFile != null) {
                try {
                    OutputGenerator.scheduleToDot(selectedSchedule, outputFile, inputParser.getName());
                } catch (IOException e) {
                    showErrorMessage("Output Error", "There was a problem creating the output file");
                }
            }
        } else {
            showErrorMessage("No Schedule Selected", "Please select a schedule to save");
        }
    }
}