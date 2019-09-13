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
import javafx.scene.Scene;
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
public class JobModule extends BaseModule {

    private LogTab mDeletionLogTab;
    private LogTab mErrorLogTab;
    private LogTab mInfoLogTab;
    private ProgressBar mProgressBar = new ProgressBar(.8);
    private TabPane mTabPane;

    public JobModule(Scene scene) {
        super(scene, Dict.TASK.toString(), MaterialIcon._Maps.DIRECTIONS_RUN.getImageView(MainApp.ICON_SIZE_MODULE).getImage());
        createUI();
        postInit();
    }

    @Override
    public Node activate() {
        return mTabPane;
    }

    @Override
    public void setNightMode(boolean state) {
        mInfoLogTab.setGraphic(MaterialIcon._Action.INFO_OUTLINE.getImageView(ICON_SIZE_PROFILE, mPreferences.getThemedIconColor()));
        mErrorLogTab.setGraphic(MaterialIcon._Alert.WARNING.getImageView(ICON_SIZE_PROFILE, mPreferences.getThemedIconColor()));
        mDeletionLogTab.setGraphic(MaterialIcon._Action.DELETE.getImageView(ICON_SIZE_PROFILE, mPreferences.getThemedIconColor()));
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

        mInfoLogTab = new LogTab(Dict.LOG.toString());
        mErrorLogTab = new LogTab(Dict.Dialog.ERROR.toString());
        mDeletionLogTab = new LogTab(Dict.DELETIONS.toString());

        mTabPane = new TabPane(mInfoLogTab, mErrorLogTab, mDeletionLogTab);
        final double TAB_SIZE = ICON_SIZE_MODULE_TOOLBAR * 1.0;
        mTabPane.setTabMaxHeight(TAB_SIZE);
        mTabPane.setTabMinHeight(TAB_SIZE);
    }

    class LogTab extends Tab {

        private LogPanel mLogPanel;

        public LogTab(String string) {
            super(string);
            mLogPanel = new LogPanel();

            setContent(mLogPanel);
            setClosable(false);
        }
    }
}
