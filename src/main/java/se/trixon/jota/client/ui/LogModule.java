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
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.almond.util.icons.material.MaterialIcon;
import static se.trixon.jota.client.ui.MainApp.ICON_SIZE_MODULE_TOOLBAR;

/**
 *
 * @author Patrik Karlström
 */
public class LogModule extends BaseModule {

    private BorderPane mBorderPane;
    private LogPanel mLogPanel;

    public LogModule() {
        super(Dict.HISTORY.toString(), MaterialIcon._Action.HISTORY.getImageView(MainApp.ICON_SIZE_MODULE).getImage());

        createUI();
    }

    @Override
    public Node activate() {
        return mBorderPane;
    }

    private void createUI() {
        ToolbarItem clearToolbarItem = new ToolbarItem(Dict.CLEAR.toString(), MaterialIcon._Content.CLEAR.getImageView(ICON_SIZE_MODULE_TOOLBAR), (event) -> {
        });
        getToolbarControlsLeft().addAll(clearToolbarItem);

        mLogPanel = new LogPanel();
        mBorderPane = new BorderPane(mLogPanel);

        Tab jobsTab = new Tab(Dict.JOBS.toString());
        ListView<String> jobsList = new ListView<>();
        jobsTab.setContent(jobsList);
        jobsTab.setClosable(false);
        jobsList.getItems().add("x");

        Tab tasksTab = new Tab(Dict.TASKS.toString());
        ListView<String> tasksList = new ListView<>();
        tasksTab.setContent(tasksList);
        tasksTab.setClosable(false);

        TabPane tabPane = new TabPane(jobsTab, tasksTab);
        tabPane.setPrefWidth(300);
        mBorderPane.setLeft(tabPane);
    }
}
