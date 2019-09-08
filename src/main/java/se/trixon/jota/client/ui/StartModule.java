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
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import static se.trixon.jota.client.ui.MainApp.*;
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
    private Stage mStage;
    private WebView mWebView;

    public StartModule(Scene scene) {
        super(scene, Dict.HOME.toString(), MaterialIcon._Action.HOME.getImageView(MainApp.ICON_SIZE_MODULE).getImage());
        mStage = (Stage) scene.getWindow();
        createUI();

        updateWindowTitle();
    }

    @Override
    public Node activate() {
        return mBorderPane;
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

    private void connectionConnect() {
        System.out.println("connected");
        mClientDisconnectAction.setDisabled(false);
        mServerShutdownRequested = false;
        mShutdownInProgress = false;

        Platform.runLater(() -> {
            loadConfiguration();
            enableGui(true);
            updateStartServerState();
        });
    }

    private void connectionDisconnect() {
        System.out.println("disconnected");
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

    private void createUI() {
        mListView = new ListView<>();
        mListView.setPrefWidth(300);

        mWebView = new WebView();
        mBorderPane = new BorderPane(mWebView);
        mBorderPane.setLeft(mListView);

        initActions();
        initToolbar();
        initListeners();
        initAccelerators();

        postInit();
    }

    private void displayEditor() {
        System.out.println("display Editor");
    }

    private void enableGui(boolean state) {
        boolean cronActive = false;

        try {
            cronActive = state && mManager.getServerCommander().isCronActive();

        } catch (RemoteException ex) {
        }

        mPreferences.server().scheduledSyncProperty().set(cronActive);

        //TODO
//        mActionManager.getConditionallyEnabledActions().stream().forEach((action) -> {
//            action.setEnabled(state);
//        });
//
//        mActionManager.getAction(ActionManager.CLOSE_TAB).setEnabled(false);
//        mActionManager.getAction(ActionManager.SAVE_TAB).setEnabled(false);
        if (state) {
            updateWindowTitle();
        } else {
            mStage.setTitle("JotaSync");
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
        mClientDisconnectAction.setDisabled(true);

        //Server Start
        mServerStartAction = new Action(Dict.START.toString(), (ActionEvent event) -> {
        });

        //Server Stop
        mServerShutdownAction = new Action(Dict.SHUTDOWN.toString(), (ActionEvent event) -> {
        });

        //Server Stop and Quit
        mServerShutdownQuitAction = new Action(Dict.SHUTDOWN_AND_QUIT.toString(), (ActionEvent event) -> {
        });

        //editor
        mEditorAction = new Action(mBundle.getString("jobEditor"), (ActionEvent event) -> {
            displayEditor();
        });
        mEditorAction.setGraphic(MaterialIcon._Editor.MODE_EDIT.getImageView(ICON_SIZE_DRAWER));
    }

    private void initListeners() {
        mManager.connectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean connected) -> {
            if (connected) {
                connectionConnect();
            } else {
                connectionDisconnect();
            }

        });
    }

    private void initToolbar() {
        MenuItem serverMenuItem = new MenuItem(Dict.SERVER.toString());
        serverMenuItem.setDisable(true);
        getToolbarControlsLeft().addAll(
                new ToolbarItem(Dict.CONNECTION.toString(),
                        ActionUtils.createMenuItem(mClientConnectAction),
                        ActionUtils.createMenuItem(mClientDisconnectAction),
                        new SeparatorMenuItem(),
                        serverMenuItem,
                        new SeparatorMenuItem(),
                        ActionUtils.createMenuItem(mServerStartAction),
                        ActionUtils.createMenuItem(mServerShutdownAction),
                        ActionUtils.createMenuItem(mServerShutdownQuitAction)
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
                    displayEditor();
                }
        );

        getToolbarControlsRight().addAll(
                dummyRunToolbarItem,
                editorToolbarItem
        );
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

    private void updateStartServerState() {
        boolean connectedToAutstartServer = mManager.isConnected()
                && mClient.getHost().equalsIgnoreCase(SystemHelper.getHostname())
                && mClient.getPortHost() == mOptions.getAutostartServerPort();

        mServerStartAction.setDisabled(connectedToAutstartServer);
    }

    private void updateWindowTitle() {
        mStage.setTitle(String.format(mBundle.getString("windowTitle"), mManager.getClient().getHost(), mManager.getClient().getPortHost()));
    }
}
