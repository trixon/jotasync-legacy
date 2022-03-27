/*
 * Copyright 2022 Patrik Karlström.
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

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jota.client.Client;
import se.trixon.jota.client.Manager;
import se.trixon.jota.shared.ServerEvent;
import se.trixon.jota.shared.ServerEventAdapter;
import se.trixon.jota.shared.job.Job;

/**
 *
 * @author Patrik Karlström
 */
public class SpeedDialTab extends BaseTab {

    private final BorderPane mBorderPane = new BorderPane();
    private ComboBox<Job> mJobComboBox;
    private final Button mButton = new Button();
    private final Manager mManager = Manager.getInstance();
    protected Font mDefaultFont = Font.getDefault();
    protected final Client mClient;
    private boolean mShutdownInProgress;

    public SpeedDialTab() {
        mClient = mManager.getClient();
        createUI();
        initListeners();

        updateIconColor();
        loadConfiguration();

    }

    private void connectionConnect() {
        mClient.setShutdownRequested(false);
        mShutdownInProgress = false;

        Platform.runLater(() -> {
            loadConfiguration();
            enableGui(true);
//            updateStartServerState();
        });
    }

    private void connectionDisconnect() {
        mJobComboBox.getItems().clear();

        Platform.runLater(() -> {
            enableGui(false);
            if (mShutdownInProgress && !mClient.isShutdownRequested()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(Dict.CONNECTION_LOST.toString());
                alert.setHeaderText(null);
                alert.setContentText(Dict.CONNECTION_LOST_SERVER_SHUTDOWN.toString());

                alert.showAndWait();
//                getWorkbench().showErrorDialog(Dict.CONNECTION_LOST.toString(), Dict.CONNECTION_LOST_SERVER_SHUTDOWN.toString(), null);
            }
//            mServerStartAction.setDisabled(false);
        });
    }

    private void createUI() {
        mJobComboBox = new ComboBox<>();
        mJobComboBox.setPrefWidth(Double.MAX_VALUE);
        mJobComboBox.setCellFactory(listView -> new JobListCell());
        mJobComboBox.setVisibleRowCount(99);
        mJobComboBox.setButtonCell(new JobListCell());

        var borderPane = new BorderPane(mJobComboBox);
        borderPane.setRight(mButton);
        mBorderPane.setTop(borderPane);

        mJobComboBox.disableProperty().bind(mManager.connectedProperty().not());
        mButton.disableProperty().bind(mManager.connectedProperty().not());
//        mButton.prefHeightProperty().bind(mJobComboBox.heightProperty());
        mJobComboBox.prefHeightProperty().bind(mButton.heightProperty());

        var button = new Button("JOB");
        mBorderPane.setCenter(button);
        setClosable(false);
        setContent(mBorderPane);
    }

    private void enableGui(boolean state) {
        boolean cronActive = false;

        try {
            cronActive = state && mManager.getServerCommander().isCronActive();

        } catch (RemoteException ex) {
        }

        mPreferences.server().scheduledSyncProperty().set(cronActive);
        //mCategoryActionManager.setEnabled(KEY_ACTION_CATEGORY_CONNECTED, !state);
    }

    private void loadConfiguration() {
        if (!mManager.isConnected()) {
            mJobComboBox.getItems().clear();
            return;
        }

        boolean hasJob = mManager.isConnected() && mManager.hasJobs();

        try {
            mPreferences.server().scheduledSyncProperty().set(mManager.getServerCommander().isCronActive());
            var jobs = mManager.getServerCommander().getJobs();
            mJobComboBox.getItems().setAll(jobs);
            if (hasJob) {
                mJobComboBox.getSelectionModel().selectFirst();
            }
        } catch (RemoteException ex) {
            System.err.println("mManager: " + mManager);
        }

    }

    private Job getSelectedJob() {
        return mJobComboBox.getSelectionModel().getSelectedItem();
    }

    private void initListeners() {
//        mPreferences.general().iconColorProperty().addListener((observable, oldValue, newValue) -> {
//            updateIconColor();
//        });
        mManager.connectedProperty().addListener((observable, oldValue, connected) -> {
            if (connected) {
                connectionConnect();
            } else {
                connectionDisconnect();
            }
        });

        mClient.addServerEventListener(new ServerEventAdapter() {

            @Override
            public void onServerEvent(ServerEvent serverEvent) {
                Platform.runLater(() -> {
                    switch (serverEvent) {
                        case JOTA_CHANGED ->
                            loadConfiguration();

                        case SHUTDOWN -> {
                            mShutdownInProgress = true;
                            mManager.disconnect();
                        }
                    }
                });
            }
        });

    }

    @Override
    protected void updateIconColor() {
        setGraphic(MaterialIcon._Action.HOME.getImageView(App.ICON_SIZE_MODULE));
        mButton.setGraphic(MaterialIcon._Av.PLAY_ARROW.getImageView(App.ICON_SIZE_MODULE * 2));
        if (getTabPane() != null) {
            getTabPane().setTabMinHeight(App.ICON_SIZE_MODULE * 1.25);
        }
    }

    class JobListCell extends ListCell<Job> {

        private final Label mDescLabel = new Label();
        private final Label mLastLabel = new Label();
        private VBox mMainBox;
        private final Label mNameLabel = new Label();
        private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat();

        public JobListCell() {
            createUI();
        }

        @Override
        protected void updateItem(Job job, boolean empty) {
            super.updateItem(job, empty);

            if (job == null || empty) {
                clearContent();
            } else {
                addContent(job);
            }
        }

        private void addContent(Job job) {
            setText(null);

            mNameLabel.setText(job.getName());
            mDescLabel.setText(job.getDescription());
            String lastRun = "-";
            if (job.getLastRun() != 0) {
                lastRun = mSimpleDateFormat.format(new Date(job.getLastRun()));
            }
            mLastLabel.setText(lastRun);

            setGraphic(mMainBox);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            String fontFamily = mDefaultFont.getFamily();
            double fontSize = mDefaultFont.getSize();

            mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.4));
            mDescLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.1));
            mLastLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.1));

            mMainBox = new VBox(mNameLabel, mDescLabel, mLastLabel);
            mMainBox.setAlignment(Pos.CENTER_LEFT);
            mMainBox.setPadding(FxHelper.getUIScaledInsets(0, 8, 0, 8));
            mPreferences.general().iconColorProperty().addListener((observable, oldValue, newValue) -> {
                updateIconColor();
            });

            updateIconColor();
        }

        private void updateIconColor() {
            var color = MaterialIcon.getDefaultColor();
            mNameLabel.setTextFill(color);
            mDescLabel.setTextFill(color);
            mLastLabel.setTextFill(color);
        }
    }

}
