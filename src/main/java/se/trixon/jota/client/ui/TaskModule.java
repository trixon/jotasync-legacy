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

import com.dlsc.workbenchfx.model.WorkbenchModule;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.almond.util.icons.material.MaterialIcon;
import static se.trixon.jota.client.ui.MainApp.*;

/**
 *
 * @author Patrik Karlström
 */
public class TaskModule extends WorkbenchModule {

    private ProgressBar mProgressBar = new ProgressBar(.8);
    private TabPane mTabPane;

    public TaskModule() {
        super(Dict.TASK.toString(), MaterialIcon._Maps.DIRECTIONS_RUN.getImageView(MainApp.ICON_SIZE_MODULE).getImage());
        createUI();
    }

    @Override
    public Node activate() {
        return mTabPane;
    }

    private void createUI() {
        ToolbarItem progressBarToolbarItem = new ToolbarItem(mProgressBar);
        ToolbarItem editToolbarItem = new ToolbarItem(MaterialIcon._Editor.MODE_EDIT.getImageView(ICON_SIZE_MODULE_TOOLBAR), (event) -> {
        });

        ToolbarItem startToolbarItem = new ToolbarItem(MaterialIcon._Av.PLAY_ARROW.getImageView(ICON_SIZE_MODULE_TOOLBAR), (event) -> {
        });

        mProgressBar.setPrefWidth(Integer.MAX_VALUE);
        getToolbarControlsLeft().addAll(progressBarToolbarItem);
        getToolbarControlsRight().addAll(editToolbarItem, startToolbarItem);

        LogTab infoLogTab = new LogTab(Dict.LOG.toString(), MaterialIcon._Action.INFO_OUTLINE.getImageView(ICON_SIZE_PROFILE));
        LogTab errorLogTab = new LogTab(Dict.Dialog.ERROR.toString(), MaterialIcon._Alert.WARNING.getImageView(ICON_SIZE_PROFILE));
        LogTab deletionLogTab = new LogTab(Dict.DELETIONS.toString(), MaterialIcon._Action.DELETE.getImageView(ICON_SIZE_PROFILE));

        mTabPane = new TabPane(infoLogTab, errorLogTab, deletionLogTab);
    }

    class LogTab extends Tab {

        private LogPanel mLogPanel;

        public LogTab(String string, Node node) {
            super(string, node);
            mLogPanel = new LogPanel();

            setContent(mLogPanel);
            setGraphic(node);
            setClosable(false);
        }
    }
}
