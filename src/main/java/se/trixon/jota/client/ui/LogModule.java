/*
 * Copyright 2019 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.jota.client.ui;

import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.almond.util.icons.material.MaterialIcon;
import static se.trixon.jota.client.ui.MainApp.*;
import se.trixon.jota.client.ui_swing.HistoryItem;
import se.trixon.jota.shared.job.Job;
import se.trixon.jota.shared.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class LogModule extends BaseModule {

    private BorderPane mBorderPane;
    private final HashMap<Long, ArrayList<String>> mHistoryMap = new HashMap<>();
    private ListView<HistoryItem> mJobList;
    private LogPanel mLogPanel;
    private ListView<HistoryItem> mTaskList;

    public LogModule(Scene scene) {
        super(scene, Dict.HISTORY.toString(), MaterialIcon._Action.HISTORY.getImageView(MainApp.ICON_SIZE_MODULE).getImage());

        createUI();
        initListeners();
        refresh();
    }

    @Override
    public Node activate() {
        return mBorderPane;
    }

    @Override
    public void connectionConnect() {
        refresh();
    }

    @Override
    public void connectionDisconnect() {
        refresh();
    }

    private void createUI() {
        ToolbarItem clearToolbarItem = new ToolbarItem(Dict.CLEAR.toString(), MaterialIcon._Content.CLEAR.getImageView(ICON_SIZE_MODULE_TOOLBAR), (event) -> {
            System.out.println("clear");
        });
        getToolbarControlsLeft().addAll(clearToolbarItem);

        mLogPanel = new LogPanel();
        mBorderPane = new BorderPane(mLogPanel);

        Tab jobsTab = new Tab(Dict.JOBS.toString());
        mJobList = new ListView<>();
        jobsTab.setContent(mJobList);
        jobsTab.setClosable(false);

        Tab tasksTab = new Tab(Dict.TASKS.toString());
        mTaskList = new ListView<>();
        tasksTab.setContent(mTaskList);
        tasksTab.setClosable(false);

        TabPane tabPane = new TabPane(jobsTab, tasksTab);
        tabPane.setPrefWidth(300);
        final double TAB_SIZE = ICON_SIZE_MODULE_TOOLBAR * 1.0;
        tabPane.setTabMaxHeight(TAB_SIZE);
        tabPane.setTabMinHeight(TAB_SIZE);
        mBorderPane.setLeft(tabPane);

        mCategoryActionManager.addAll(KEY_ACTION_CATEGORY_CONNECTED,
                clearToolbarItem.disableProperty()
        );
    }

    private ArrayList<String> getList(Long key) {
        if (!mHistoryMap.containsKey(key)) {
            ArrayList<String> list = new ArrayList<>();
            mHistoryMap.put(key, list);
        }

        return mHistoryMap.get(key);
    }

    private void initListeners() {
        ChangeListener<HistoryItem> changeListener = (ObservableValue<? extends HistoryItem> ov, HistoryItem t, HistoryItem t1) -> {
            mLogPanel.clear();
            if (t1 != null) {
                mLogPanel.println(t1.getHistory());
            }
        };

        mJobList.getSelectionModel().selectedItemProperty().addListener(changeListener);
        mTaskList.getSelectionModel().selectedItemProperty().addListener(changeListener);
    }

    private void refresh() {
        String history = "";
        mLogPanel.clear();
        mHistoryMap.clear();
        mJobList.getItems().clear();
        mTaskList.getItems().clear();

        if (!mManager.isConnected()) {
            return;
        }

        try {
            history = mManager.getServerCommander().getHistory();
            String lines[] = history.split("\\r?\\n");

            for (String line : lines) {
                String[] lineItems = StringUtils.split(line, null, 2);
                Long key = Long.parseLong(lineItems[0]);
                if (mOptions.isDisplayDryRun() || (!mOptions.isDisplayDryRun() && !lineItems[1].endsWith(")"))) {
                    getList(key).add(lineItems[1]);
                }
            }

            LinkedList<Job> jobs = mManager.getServerCommander().getJobs();
            LinkedList<Task> tasks = mManager.getServerCommander().getTasks();

            ArrayList<HistoryItem> jobHistoryItems = new ArrayList<>();
            ArrayList<HistoryItem> taskHistoryItems = new ArrayList<>();

            for (Long key : mHistoryMap.keySet()) {
                boolean isJob = false;
                boolean isTask = false;

                for (Job job : jobs) {
                    if (job.getId() == key) {
                        HistoryItem historyItem = new HistoryItem(key, job.getName(), String.join("\n", mHistoryMap.get(key)));
                        jobHistoryItems.add(historyItem);
                        isJob = true;
                        break;
                    }
                }

                if (!isJob) {
                    for (Task task : tasks) {
                        if (task.getId() == key) {
                            HistoryItem historyItem = new HistoryItem(key, task.getName(), String.join("\n", mHistoryMap.get(key)));
                            taskHistoryItems.add(historyItem);
                            isTask = true;
                            break;
                        }
                    }
                }
            }

            jobHistoryItems.sort((HistoryItem o1, HistoryItem o2) -> o1.getName().compareTo(o2.getName()));
            taskHistoryItems.sort((HistoryItem o1, HistoryItem o2) -> o1.getName().compareTo(o2.getName()));

            mJobList.getItems().setAll(jobHistoryItems);
            mTaskList.getItems().setAll(taskHistoryItems);
        } catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
