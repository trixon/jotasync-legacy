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
import java.util.logging.Level;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.almond.util.icons.material.MaterialIcon;
import static se.trixon.jota.client.ui.MainApp.*;
import se.trixon.jota.client.ui_swing.Progress;
import se.trixon.jota.shared.ProcessEvent;
import se.trixon.jota.shared.job.Job;

/**
 *
 * @author Patrik Karlström
 */
public class JobModule extends BaseModule {

    private StringBuilder mBuilder;
    private ToolbarItem mCancelToolbarItem;
    private boolean mClosable;
    private LogTab mDeletionLogTab;
    private ToolbarItem mEditToolbarItem;
    private LogTab mErrorLogTab;
    private LogTab mInfoLogTab;
    private final Job mJob;
    private boolean mLastLineWasBlank;
    private boolean mLastRowWasProgress;
    private Progress mProgress = new Progress();
    private ProgressBar mProgressBar = new ProgressBar();
    private ToolbarItem mStartToolbarItem;
    private TabPane mTabPane;
    private long mTimeFinished;

    public JobModule(Scene scene, Job job) {
        super(scene, job.getName(), MaterialIcon._Maps.DIRECTIONS_RUN.getImageView(MainApp.ICON_SIZE_MODULE).getImage());

        mJob = job;
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

    void enableSave() {
        mProgressBar.setProgress(1);
//        progressBar.setStringPainted(false);
        mEditToolbarItem.setDisable(false);
        getToolbarControlsRight().setAll(
                mEditToolbarItem,
                mStartToolbarItem
        );
        mTimeFinished = System.currentTimeMillis();
        mClosable = true;
//        mCloser.getButton().setEnabled(true);

    }

    void log(ProcessEvent processEvent, String string) {
//        String line = string + "\n";
        String line = string;

        LogTab lp = mInfoLogTab;

        if (mPreferences.general().isSplitDeletions() && StringUtils.startsWith(line, "deleting ")) {
            lp = mDeletionLogTab;

            if (!mTabPane.getTabs().contains(lp)) {
                mTabPane.getTabs().add(lp);
            }
        } else if (mPreferences.general().isSplitErrors() && (StringUtils.startsWith(line, "rsync: ") || StringUtils.startsWith(line, "rsync error: "))) {
            lp = mErrorLogTab;
            if (!mTabPane.getTabs().contains(lp)) {
                mTabPane.getTabs().add(lp);
            }
        }

//        if (mProgress.parse(line)) {
//            //mProgressBar.setStringPainted(true);
//
//            mProgressBar.setProgress(mProgress.getPercentage() / 100d);
//            //mProgressBar.setString(mProgress.toString());
//            mLastRowWasProgress = true;
//        } else {
//            if (mLastRowWasProgress && mLastLineWasBlank) {
//                try {
//                    int size = lp.getText().length();
////                    lp.getTextArea().replaceRange(null, size - 1, size);
//                    lp.replaceText(size - 1, size, null);
//                } catch (IllegalArgumentException e) {
//                }
//            }
//            lp.println(line);
//            mLastLineWasBlank = StringUtils.isBlank(line);
//            mLastRowWasProgress = false;
//        }
        lp.log(line);
//        System.out.println(line);
//        mBuilder.append(line).append("\n");
        lp.setText(mBuilder.toString());
    }

    void start() {
        mBuilder = new StringBuilder();
        initSubTabs();
        mProgressBar.setProgress(-1);
//        progressBar.setStringPainted(false);
        mEditToolbarItem.setDisable(true);
        getToolbarControlsRight().setAll(
                mEditToolbarItem,
                mCancelToolbarItem
        );
//        mClosable = false;
    }

    private void createUI() {
        ToolbarItem progressBarToolbarItem = new ToolbarItem(mProgressBar);
        mEditToolbarItem = new ToolbarItem(MaterialIcon._Editor.MODE_EDIT.getImageView(ICON_SIZE_MODULE_TOOLBAR), (event) -> {
        });

        mStartToolbarItem = new ToolbarItem(MaterialIcon._Av.PLAY_ARROW.getImageView(ICON_SIZE_MODULE_TOOLBAR), (event) -> {
            mJobController.run(mJob);
        });

        mCancelToolbarItem = new ToolbarItem(MaterialIcon._Navigation.CANCEL.getImageView(ICON_SIZE_MODULE_TOOLBAR), (event) -> {
            try {
                mManager.getServerCommander().stopJob(mJob);
            } catch (RemoteException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        });

        mProgressBar.setPrefWidth(Integer.MAX_VALUE);
        getToolbarControlsLeft().addAll(progressBarToolbarItem);
        getToolbarControlsRight().setAll(
                mEditToolbarItem,
                mCancelToolbarItem,
                mStartToolbarItem
        );

        mInfoLogTab = new LogTab(Dict.LOG.toString());
        mErrorLogTab = new LogTab(Dict.Dialog.ERROR.toString());
        mDeletionLogTab = new LogTab(Dict.DELETIONS.toString());

        mTabPane = new TabPane(mInfoLogTab);
        final double TAB_SIZE = ICON_SIZE_MODULE_TOOLBAR * 1.0;
        mTabPane.setTabMaxHeight(TAB_SIZE);
        mTabPane.setTabMinHeight(TAB_SIZE);
    }

    private void initSubTabs() {
        mInfoLogTab.clear();
        mErrorLogTab.clear();
        mDeletionLogTab.clear();

//        holderPanel.removeAll();
        mTabPane.getTabs().setAll(mInfoLogTab);
    }

    class LogTab extends Tab {

        private LogPanel mLogPanel;
        private ListView<String> mListView;

        public LogTab(String string) {
            super(string);
            mLogPanel = new LogPanel();
            mListView = new ListView<>();
            mLogPanel.setMonospaced();

//            setContent(mLogPanel);
            setContent(mListView);
            setClosable(false);
        }

        public LogPanel getLogPanel() {
            return mLogPanel;
        }

        void clear() {
            mLogPanel.clear();
            mListView.getItems().clear();
        }

        void log(String s) {
//            mLogPanel.println(s);
            mListView.getItems().add(s);
//            mListView.scrollTo(s);

        }
    }
}
