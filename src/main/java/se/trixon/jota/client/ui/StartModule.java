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

import com.dlsc.workbenchfx.model.WorkbenchDialog;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javax.swing.JOptionPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.jota.client.Client;
import static se.trixon.jota.client.ui.MainApp.*;
import se.trixon.jota.client.ui_swing.editor.EditorPanel;
import se.trixon.jota.server.JobValidator;
import se.trixon.jota.shared.ServerEvent;
import se.trixon.jota.shared.ServerEventAdapter;
import se.trixon.jota.shared.job.Job;

/**
 *
 * @author Patrik Karlström
 */
public class StartModule extends BaseModule {

    private static final Logger LOGGER = Logger.getLogger(StartModule.class.getName());
    private BorderPane mBorderPane;
    private final ResourceBundle mBundle = SystemHelper.getBundle(MainApp.class, "Bundle");
    private Action mClientConnectAction;
    private Action mClientDisconnectAction;
    private Action mEditorAction;
    private ListView<Job> mListView;
    private Action mServerShutdownAction;
    private Action mServerShutdownQuitAction;
    private boolean mServerShutdownRequested;
    private Action mServerStartAction;
    private boolean mShutdownInProgress;
    private WebView mWebView;

    public StartModule(Scene scene) {
        super(scene, Dict.HOME.toString(), MaterialIcon._Action.HOME.getImageView(MainApp.ICON_SIZE_MODULE).getImage());
        createUI();

        updateWindowTitle();
        loadConfiguration();

        if (mManager.isConnected()) {
            enableGui(true);
            //mTabHolder.getSpeedDialPanel().onConnectionConnect();
            updateStartServerState();
        } else {
            enableGui(false);
        }

        if (mManager.isConnected()) {
            //displayEditor(-1);
        }
    }

    @Override
    public Node activate() {
        return mBorderPane;
    }

    @Override
    public void connectionConnect() {
        mClientDisconnectAction.setDisabled(false);
        mServerShutdownRequested = false;
        mShutdownInProgress = false;

        Platform.runLater(() -> {
            loadConfiguration();
            enableGui(true);
            updateStartServerState();
        });
    }

    @Override
    public void connectionDisconnect() {
        mListView.getItems().clear();
        mClientDisconnectAction.setDisabled(true);

        Platform.runLater(() -> {
            enableGui(false);
            if (mShutdownInProgress && !mServerShutdownRequested) {
                getWorkbench().showErrorDialog(Dict.CONNECTION_LOST.toString(), Dict.CONNECTION_LOST_SERVER_SHUTDOWN.toString(), null);
            }
            mServerStartAction.setDisabled(false);
        });
    }

    @Override
    public void setNightMode(boolean state) {
        if (state) {
            mWebView.getEngine().setUserStyleSheetLocation(getClass().getResource("darkWeb.css").toExternalForm());
        } else {
            mWebView.getEngine().setUserStyleSheetLocation(getClass().getResource("lightWeb.css").toExternalForm());
        }

        mClientConnectAction.setGraphic(MaterialIcon._Communication.CALL_MADE.getImageView(ICON_SIZE_DRAWER, mPreferences.getThemedIconColor()));
        mClientDisconnectAction.setGraphic(MaterialIcon._Communication.CALL_RECEIVED.getImageView(ICON_SIZE_DRAWER, mPreferences.getThemedIconColor()));
    }

    private void createUI() {
        mListView = new ListView<>();
        mListView.setPrefWidth(400);
        mListView.setCellFactory((ListView<Job> param) -> new JobListCell());

        mWebView = new WebView();
        mBorderPane = new BorderPane(mWebView);
        mBorderPane.setLeft(mListView);

        initActions();
        initToolbar();
        initListeners();
        initAccelerators();

        postInit();
    }

    private void displayEditor(long jobId) {
        boolean openJob = jobId != -1;
        EditorPanel editorPanel = new EditorPanel(jobId, openJob);
        SwingHelper.makeWindowResizable(editorPanel);

        int retval = JOptionPane.showOptionDialog(null,
                editorPanel,
                mBundle.getString("jobEditor"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if (retval == JOptionPane.OK_OPTION) {
            editorPanel.save();
            try {
                mManager.getServerCommander().saveJota();
            } catch (RemoteException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void enableGui(boolean state) {
        boolean cronActive = false;

        try {
            cronActive = state && mManager.getServerCommander().isCronActive();

        } catch (RemoteException ex) {
        }

        mPreferences.server().scheduledSyncProperty().set(cronActive);
        mCategoryActionManager.setEnabled(KEY_ACTION_CATEGORY_CONNECTED, !state);

        if (state) {
            updateWindowTitle();
        } else {
            mStage.setTitle("JotaSync");
            mWebView.getEngine().loadContent("");
        }
    }

    private void initAccelerators() {
        final ObservableMap<KeyCombination, Runnable> accelerators = getScene().getAccelerators();

        accelerators.put(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN), () -> {
            mClientConnectAction.handle(new ActionEvent());
        });

        accelerators.put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN), () -> {
            mClientDisconnectAction.handle(new ActionEvent());
        });

        accelerators.put(new KeyCodeCombination(KeyCode.J, KeyCombination.SHORTCUT_DOWN), () -> {
            if (!mEditorAction.isDisabled()) {
                displayEditor(-1);
            }
        });

        mClientConnectAction.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        mClientDisconnectAction.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN));

        mServerStartAction.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        mServerShutdownAction.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        mServerShutdownQuitAction.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));

        mEditorAction.setAccelerator(new KeyCodeCombination(KeyCode.J, KeyCombination.SHORTCUT_DOWN));
    }

    private void initActions() {
        // CONNECTION
        //Connect
        mClientConnectAction = new Action(Dict.CONNECT_TO_SERVER.toString(), (ActionEvent event) -> {
            requestConnect();
        });

        //Disconnect
        mClientDisconnectAction = new Action(Dict.DISCONNECT.toString(), (ActionEvent event) -> {
            mManager.disconnect();
        });
//        mClientDisconnectAction.setDisabled(mManager.isConnected());

        //Server Start
        mServerStartAction = new Action(Dict.START.toString(), (ActionEvent event) -> {
            try {
                if (mClient.serverStart()) {
                    mManager.connect(SystemHelper.getHostname(), mOptions.getAutostartServerPort());
                }
            } catch (URISyntaxException | IOException | NotBoundException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        });

        //Server Stop
        mServerShutdownAction = new Action(Dict.SHUTDOWN.toString(), (ActionEvent event) -> {
            serverShutdown();
        });

        //Server Stop and Quit
        mServerShutdownQuitAction = new Action(Dict.SHUTDOWN_AND_QUIT.toString(), (ActionEvent event) -> {
            serverShutdown();
            quit();
        });

        //editor
        mEditorAction = new Action(mBundle.getString("jobEditor"), (ActionEvent event) -> {
            displayEditor(-1);
        });
        mEditorAction.setGraphic(MaterialIcon._Editor.MODE_EDIT.getImageView(ICON_SIZE_DRAWER));

        mCategoryActionManager.addAll(KEY_ACTION_CATEGORY_CONNECTED,
                mEditorAction.disabledProperty(),
                mClientDisconnectAction.disabledProperty(),
                mServerShutdownAction.disabledProperty(),
                mServerShutdownQuitAction.disabledProperty()
        );
    }

    private void initListeners() {
        mListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Job> c) -> {
            Job job = mListView.getSelectionModel().getSelectedItem();
            if (job != null) {
                mWebView.getEngine().loadContent(job.getSummaryAsHtml());
            } else {
                mWebView.getEngine().loadContent("");
            }
        });

        mClient.addServerEventListener(new ServerEventAdapter() {

            @Override
            public void onServerEvent(ServerEvent serverEvent) {
                Platform.runLater(() -> {
                    switch (serverEvent) {
                        case JOTA_CHANGED:
                            loadConfiguration();
                            break;

                        case SHUTDOWN:
                            mShutdownInProgress = true;
                            mManager.disconnect();
                            break;
                    }
                });
            }
        });
    }

    private void initToolbar() {
        Menu serverMenuItem = new Menu(Dict.SERVER.toString());
        serverMenuItem.getItems().setAll(
                ActionUtils.createMenuItem(mServerStartAction),
                ActionUtils.createMenuItem(mServerShutdownAction),
                ActionUtils.createMenuItem(mServerShutdownQuitAction)
        );

        getToolbarControlsLeft().addAll(
                new ToolbarItem(Dict.CONNECTION.toString(),
                        ActionUtils.createMenuItem(mClientConnectAction),
                        ActionUtils.createMenuItem(mClientDisconnectAction),
                        new SeparatorMenuItem(),
                        serverMenuItem
                )
        );

        ToolbarItem dummyRunToolbarItem = new ToolbarItem(Dict.RUN.toString(), MaterialIcon._Maps.DIRECTIONS_RUN.getImageView(ICON_SIZE_TOOLBAR, Color.LIGHTGRAY),
                event -> {
                    TaskModule taskModule = new TaskModule(getScene());
                    getWorkbench().getModules().add(taskModule);
                    getWorkbench().openModule(taskModule);
                }
        );

        ToolbarItem editorToolbarItem = new ToolbarItem(mEditorAction.getText(), mEditorAction.getGraphic(),
                event -> {
                    displayEditor(-1);
                }
        );

        getToolbarControlsRight().addAll(
                dummyRunToolbarItem,
                editorToolbarItem
        );

        mCategoryActionManager.addAll(KEY_ACTION_CATEGORY_CONNECTED,
                dummyRunToolbarItem.disableProperty(),
                editorToolbarItem.disableProperty()
        );
    }

    private void jobRun(Job job) {
        try {
            JobValidator validator = mManager.getServerCommander().validate(job);

            if (validator.isValid()) {
                HtmlPane previewPanel = new HtmlPane(job.getSummaryAsHtml());

                ButtonType runButtonType = new ButtonType(Dict.RUN.toString());
                ButtonType dryRunButtonType = new ButtonType(Dict.DRY_RUN.toString(), ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelButtonType = new ButtonType(Dict.CANCEL.toString(), ButtonBar.ButtonData.CANCEL_CLOSE);

                String title = String.format(Dict.Dialog.TITLE_PROFILE_RUN.toString(), job.getName());

                WorkbenchDialog dialog = WorkbenchDialog.builder(title, previewPanel, runButtonType, dryRunButtonType, cancelButtonType).onResult(buttonType -> {
                    try {
                        if (buttonType != cancelButtonType) {
                            boolean dryRun = buttonType == dryRunButtonType;
                            mManager.getServerCommander().startJob(job, dryRun);
                        }
                    } catch (RemoteException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }).build();
                getWorkbench().showDialog(dialog);
            } else {
                WorkbenchDialog dialog = WorkbenchDialog.builder(
                        Dict.Dialog.ERROR_VALIDATION.toString(),
                        new HtmlPane(validator.getSummaryAsHtml()),
                        WorkbenchDialog.Type.ERROR).build();

                getWorkbench().showDialog(dialog);
            }
        } catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void loadConfiguration() {
        if (!mManager.isConnected()) {
            mListView.getItems().clear();
            return;
        }

        boolean hasJob = mManager.isConnected() && mManager.hasJobs();

        try {
            mPreferences.server().scheduledSyncProperty().set(mManager.getServerCommander().isCronActive());
            LinkedList<Job> jobs = mManager.getServerCommander().getJobs();
            mListView.getItems().setAll(jobs);
        } catch (RemoteException ex) {
            System.err.println("mManager: " + mManager);
        }

    }

    private void quit() {
        mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private void requestConnect() {
        String[] hosts = mOptions.getHosts().split(";");
        Arrays.sort(hosts);
        Label hostLabel = new Label(Dict.HOST.toString());
        ComboBox<String> hostComboBox = new ComboBox<>(FXCollections.observableArrayList(hosts));
        hostComboBox.setEditable(true);
        hostComboBox.setPrefWidth(Integer.MAX_VALUE);
        Label portLabel = new Label(Dict.PORT.toString());
        Spinner<Integer> portSpinner = new Spinner(1024, 65535, 1099, 1);
        portSpinner.setEditable(true);
        portSpinner.setPrefWidth(Integer.MAX_VALUE);
        FxHelper.autoCommitSpinner(portSpinner);

        hostComboBox.getSelectionModel().select(mClient.getHost());
        portSpinner.getValueFactory().setValue(mClient.getPortHost());

        VBox box = new VBox(
                hostLabel,
                hostComboBox,
                portLabel,
                portSpinner
        );

        box.setPrefWidth(300);

        ButtonType connectButtonType = new ButtonType(Dict.CONNECT.toString(), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(Dict.CANCEL.toString(), ButtonBar.ButtonData.CANCEL_CLOSE);

        WorkbenchDialog dialog = WorkbenchDialog.builder(Dict.CONNECT_TO_HOST.toString(), box, connectButtonType, cancelButtonType).onResult(buttonType -> {
            if (buttonType == connectButtonType) {
                String currentHost = mClient.getHost();
                int currentPort = mClient.getPortHost();
                String host = (String) hostComboBox.getSelectionModel().getSelectedItem();
                int port = portSpinner.getValue();
                try {
                    mManager.disconnect();
                    mManager.connect(host, port);

                    if (hostComboBox.getItems().indexOf(host) == -1) {
                        hostComboBox.getItems().add(host);
                    }
                    mOptions.setHosts(hostComboBox.getItems().stream().collect(Collectors.joining(";")));
                } catch (NumberFormatException ex) {
                    getWorkbench().showErrorDialog(Dict.Dialog.ERROR.toString(), ex.getMessage(), ex, (bt) -> {
                    });
                } catch (NotBoundException | MalformedURLException | RemoteException | SocketException ex) {
                    getWorkbench().showErrorDialog(Dict.Dialog.ERROR.toString(), ex.getMessage(), ex, (bt) -> {
                    });
                    mClient.setHost(currentHost);
                    mClient.setPortHost(currentPort);
                }
            }
        }).build();
        getWorkbench().showDialog(dialog);
    }

    private void serverShutdown() {
        mServerShutdownRequested = true;
        mClient.execute(Client.Command.SHUTDOWN);
    }

    private void updateStartServerState() {
        boolean connectedToAutstartServer = mManager.isConnected()
                && mClient.getHost().equalsIgnoreCase(SystemHelper.getHostname())
                && mClient.getPortHost() == mOptions.getAutostartServerPort();

        mServerStartAction.setDisabled(connectedToAutstartServer);
    }

    private void updateWindowTitle() {
        mStage.setTitle(String.format(mBundle.getString("windowTitle"), mManager.getClient().getHost(), mManager.getClient().getPortHost()));
    }

    class JobListCell extends ListCell<Job> {

        private final BorderPane mBorderPane = new BorderPane();
        private final Label mDescLabel = new Label();
        private final Duration mDuration = Duration.millis(200);
        private Action mEditAction;
        private final FadeTransition mFadeInTransition = new FadeTransition();
        private final FadeTransition mFadeOutTransition = new FadeTransition();
        private final Label mLastLabel = new Label();
        private final Label mNameLabel = new Label();
        private Action mRunAction;
        private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat();

        public JobListCell() {
            mFadeInTransition.setDuration(mDuration);
            mFadeInTransition.setFromValue(0);
            mFadeInTransition.setToValue(1);

            mFadeOutTransition.setDuration(mDuration);
            mFadeOutTransition.setFromValue(1);
            mFadeOutTransition.setToValue(0);

            createUI();
            setNightMode(mPreferences.general().isNightMode());
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

            setGraphic(mBorderPane);
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

            mRunAction = new Action(Dict.RUN.toString(), (ActionEvent event) -> {
                jobRun(getSelectedJob());
                mListView.requestFocus();
            });

            mEditAction = new Action(Dict.EDIT.toString(), (ActionEvent event) -> {
                displayEditor(getSelectedJob().getId());
                mListView.requestFocus();
            });

            VBox mainBox = new VBox(mNameLabel, mDescLabel, mLastLabel);
            mainBox.setAlignment(Pos.CENTER_LEFT);

            Collection<? extends Action> actions = Arrays.asList(
                    mEditAction,
                    mRunAction
            );

            mPreferences.general().nightModeProperty().addListener((observable, oldValue, newValue) -> {
                setNightMode(newValue);
            });
            ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
            toolBar.setBackground(Background.EMPTY);
            toolBar.setVisible(false);
            toolBar.setStyle("-fx-spacing: 0px;");
            FxHelper.adjustButtonWidth(toolBar.getItems().stream(), ICON_SIZE_PROFILE * 1.8);

            toolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                    .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
                FxHelper.undecorateButton(buttonBase);
            });

            BorderPane.setAlignment(toolBar, Pos.CENTER);

            mBorderPane.setCenter(mainBox);
            BorderPane.setMargin(mainBox, new Insets(8));
            mBorderPane.setRight(toolBar);
            mFadeInTransition.setNode(toolBar);
            mFadeOutTransition.setNode(toolBar);

            mBorderPane.setOnMouseEntered((MouseEvent event) -> {
                if (!toolBar.isVisible()) {
                    toolBar.setVisible(true);
                }

                selectListItem();
                mFadeInTransition.playFromStart();
            });

            mBorderPane.setOnMouseExited((MouseEvent event) -> {
                mFadeOutTransition.playFromStart();
            });
        }

        private Job getSelectedJob() {
            return mListView.getSelectionModel().getSelectedItem();
        }

        private void selectListItem() {
            mListView.getSelectionModel().select(this.getIndex());
            mListView.requestFocus();
        }

        private void setNightMode(boolean state) {
            mRunAction.setGraphic(MaterialIcon._Av.PLAY_ARROW.getImageView(ICON_SIZE_PROFILE, mPreferences.getThemedIconColor()));
            mEditAction.setGraphic(MaterialIcon._Image.EDIT.getImageView(ICON_SIZE_PROFILE, mPreferences.getThemedIconColor()));
        }
    }
}
