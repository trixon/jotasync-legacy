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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.JOptionPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import static org.controlsfx.control.action.ActionUtils.ACTION_SEPARATOR;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SnapHelperFx;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.SystemHelperFx;
import se.trixon.almond.util.fx.AboutModel;
import se.trixon.almond.util.fx.AlmondFx;
import se.trixon.almond.util.fx.FxActionCheck;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.dialogs.about.AboutPane;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.jota.client.Client;
import se.trixon.jota.client.Client.Command;
import se.trixon.jota.client.ClientOptions;
import se.trixon.jota.client.Manager;
import se.trixon.jota.client.Preferences;
import se.trixon.jota.client.PreferencesServer;
import se.trixon.jota.client.ui.editor.EditorPanel;

/**
 *
 * @author Patrik Karlström
 */
public class App extends Application {

    public static final String APP_TITLE = "JotaSync";
    public static final int ICON_SIZE_MODULE = 32;
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    private final AlmondFx mAlmondFX = AlmondFx.getInstance();
    private AppForm mAppForm;
    private final ResourceBundle mBundle = SystemHelper.getBundle(App.class, "Bundle");
    private final Client mClient;
    private final Manager mManager = Manager.getInstance();
    private final ClientOptions mOptions = ClientOptions.getInstance();
    private final Preferences mPreferences = Preferences.getInstance();
    private final PreferencesServer mPreferencesServer = PreferencesServer.getInstance();
    private BorderPane mRoot;
    private Stage mStage;
    private FxActionCheck scheduleAction;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public App() {
        mClient = mManager.getClient();
    }

    @Override
    public void start(Stage stage) throws Exception {
        mStage = stage;
        stage.getIcons().add(new Image(App.class.getResourceAsStream("about_logo.png")));

        mAlmondFX.addStageWatcher(stage, App.class);
        createUI();

        updateNightMode();

        FxHelper.removeSceneInitFlicker(mStage);
        mStage.show();

        initListeners();

        SnapHelperFx.checkSnapStatus(App.class, "snap", mStage, "jotasync", "removable-media");
        updateWindowTitle();
    }

    @Override
    public void stop() throws Exception {
        mManager.disconnect();
        super.stop();
        System.exit(0);
    }

    private void createUI() {
        var pomInfo = new PomInfo(App.class, "se.trixon", "jotasync");
        var aboutModel = new AboutModel(SystemHelper.getBundle(App.class, "about"), SystemHelperFx.getResourceAsImageView(App.class, "about_logo.png"));
        aboutModel.setAppVersion(pomInfo.getVersion());
        var aboutAction = AboutPane.getAction(mStage, aboutModel);

        var connectAction = new Action(Dict.CONNECT_TO_SERVER.toString(), actionEvent -> {
            requestConnect();
        });
        connectAction.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        connectAction.disabledProperty().bind(mManager.connectedProperty());

        var disconnectAction = new Action(Dict.DISCONNECT.toString(), actionEvent -> {
            mManager.disconnect();
        });
        disconnectAction.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN));
        disconnectAction.disabledProperty().bind(mManager.connectedProperty().not());

        var serverStartAction = new Action(Dict.SERVER_START.toString(), actionEvent -> {
            serverStart();
        });
        serverStartAction.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        serverStartAction.disabledProperty().bind(mManager.connectedProperty());

        var serverShutdownAction = new Action(Dict.SERVER_SHUTDOWN.toString(), actionEvent -> {
            serverShutdown();
        });
        serverShutdownAction.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        serverShutdownAction.disabledProperty().bind(mManager.connectedProperty().not());

        var serverShutdownQuitAction = new Action(Dict.SHUTDOWN_SERVER_AND_QUIT.toString(), actionEvent -> {
            serverShutdown();
            quit();
        });
        serverShutdownQuitAction.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        serverShutdownQuitAction.disabledProperty().bind(mManager.connectedProperty().not());

        var quitAction = new Action(Dict.QUIT.toString(), actionEvent -> {
            quit();
        });
        quitAction.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));

        var fileActionGroup = new ActionGroup(Dict.FILE.toString(),
                connectAction,
                disconnectAction,
                ACTION_SEPARATOR,
                quitAction
        );

        var editorAction = new Action(mBundle.getString("jobEditor"), actionEvent -> {
            displayEditor(-1L);
        });
        editorAction.setAccelerator(new KeyCodeCombination(KeyCode.J, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        editorAction.disabledProperty().bind(mManager.connectedProperty().not());

        var logViewerAction = new Action(mBundle.getString("logViewer"), actionEvent -> {
            displayLogViewer();
        });
        logViewerAction.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        logViewerAction.disabledProperty().bind(mManager.connectedProperty().not());

        var optionsAction = new Action(Dict.OPTIONS.toString(), actionEvent -> {
            displayOptions();
        });
        optionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));

        var optionsServerAction = new Action(mBundle.getString("serverOptions"), actionEvent -> {
            displayOptionsServer();
        });
        optionsServerAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        optionsServerAction.disabledProperty().bind(mManager.connectedProperty().not());

        var serverActionGroup = new ActionGroup(Dict.SERVER.toString(),
                editorAction,
                logViewerAction,
                ACTION_SEPARATOR,
                serverStartAction,
                serverShutdownAction,
                serverShutdownQuitAction,
                ACTION_SEPARATOR,
                optionsServerAction
        );

        var toolsActionGroup = new ActionGroup(Dict.TOOLS.toString(),
                optionsAction
        );

        var helpAction = new Action(Dict.HELP.toString(), actionEvent -> {
            displayHelp();
        });

        var aboutRsyncAction = new Action("About _rsync", actionEvent -> {
        });
        aboutRsyncAction.disabledProperty().bind(mManager.connectedProperty().not());

        var helpActionGroup = new ActionGroup(Dict.HELP.toString(),
                helpAction,
                ACTION_SEPARATOR,
                aboutRsyncAction,
                aboutAction
        );

        var actions = Arrays.asList(fileActionGroup, serverActionGroup, toolsActionGroup, helpActionGroup);
        var menuBar = ActionUtils.createMenuBar(actions);

        mAppForm = new AppForm();
        mRoot = new BorderPane(mAppForm);
        mRoot.setTop(menuBar);
        var scene = new Scene(mRoot);

        mStage.setScene(scene);
    }

    private void displayEditor(long jobId) {
        boolean openJob = jobId != -1;
        var editorPanel = new EditorPanel(jobId, openJob);
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

    private void displayErrorDialog(String message) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(mStage);
        alert.setTitle(Dict.Dialog.ERROR.toString());
        alert.setHeaderText(null);
        alert.setResizable(true);
        alert.setContentText(message);

        if (mPreferences.general().isNightMode()) {
            FxHelper.loadDarkTheme(alert.getDialogPane());
        }

        alert.showAndWait();
    }

    private void displayHelp() {
        SystemHelper.desktopBrowse("https://trixon.se/projects/jotasync/documentation/");
    }

    private void displayLogViewer() {
        displayErrorDialog("TODO");
    }

    private void displayOptions() {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(mStage);
        alert.setTitle(Dict.OPTIONS.toString());
        alert.setGraphic(null);
        alert.setHeaderText(null);
        alert.setResizable(true);

        var dialogPane = alert.getDialogPane();
        dialogPane.setContent(mPreferences.getPreferencesFxView());
        FxHelper.removeSceneInitFlicker(dialogPane);

        var button = (Button) dialogPane.lookupButton(ButtonType.OK);
        button.setText(Dict.CLOSE.toString());

        FxHelper.showAndWait(alert, mStage);
        mPreferences.save();
    }

    private void displayOptionsServer() {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(mStage);
        alert.setTitle(mBundle.getString("serverOptions"));
        alert.setGraphic(null);
        alert.setHeaderText(null);
        alert.setResizable(true);

        var dialogPane = alert.getDialogPane();
        dialogPane.setContent(mPreferencesServer.getPreferencesFxView());
        FxHelper.removeSceneInitFlicker(dialogPane);

        var button = (Button) dialogPane.lookupButton(ButtonType.OK);
        button.setText(Dict.CLOSE.toString());

        FxHelper.showAndWait(alert, mStage);
        mPreferencesServer.save();
    }

    private void initListeners() {
        mPreferences.general().nightModeProperty().addListener((observable, oldValue, newValue) -> {
            updateNightMode();
        });

        mManager.connectedProperty().addListener((observable, oldValue, connected) -> {
            updateWindowTitle();
//            scheduleAction.setSelected(mPreferences.client().isAutoStart());
            if (connected) {
            } else {
            }
        });
    }

    private void quit() {
        mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private void requestConnect() {
        var hosts = mOptions.getHosts().split(";");
        Arrays.sort(hosts);
        var hostLabel = new Label(Dict.HOST.toString());
        var hostComboBox = new ComboBox<>(FXCollections.observableArrayList(hosts));
        hostComboBox.setEditable(true);
        hostComboBox.setPrefWidth(Integer.MAX_VALUE);
        var portLabel = new Label(Dict.PORT.toString());
        var portSpinner = new Spinner<Integer>(1024, 65535, 1099, 1);
        portSpinner.setEditable(true);
        portSpinner.setPrefWidth(Integer.MAX_VALUE);
        FxHelper.autoCommitSpinner(portSpinner);

        hostComboBox.getSelectionModel().select(mClient.getHost());
        portSpinner.getValueFactory().setValue(mClient.getPortHost());

        var box = new VBox(
                hostLabel,
                hostComboBox,
                portLabel,
                portSpinner
        );

        box.setPrefWidth(300);

        var connectButtonType = new ButtonType(Dict.CONNECT.toString(), ButtonData.OK_DONE);
        var cancelButtonType = new ButtonType(Dict.CANCEL.toString(), ButtonData.CANCEL_CLOSE);

        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(mStage);
        alert.setTitle(Dict.CONNECT_TO_HOST.toString());
        alert.setGraphic(null);
        alert.setHeaderText(null);
        alert.setResizable(true);

        var dialogPane = alert.getDialogPane();
        dialogPane.setContent(box);
        FxHelper.removeSceneInitFlicker(dialogPane);
        alert.getButtonTypes().setAll(connectButtonType, cancelButtonType);

        var result = FxHelper.showAndWait(alert, mStage);
        if (result.get() == connectButtonType) {
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
                displayErrorDialog(ex.getMessage());
            } catch (NotBoundException | MalformedURLException | RemoteException | SocketException ex) {
                displayErrorDialog(ex.getMessage());
                mClient.setHost(currentHost);
                mClient.setPortHost(currentPort);
            }
        }
    }

    private void serverShutdown() {
        mClient.setShutdownRequested(true);
        mClient.execute(Command.SHUTDOWN);
    }

    private void serverStart() {
        try {
            if (mClient.serverStart()) {
                mManager.connect(SystemHelper.getHostname(), mOptions.getAutostartServerPort());
            }
        } catch (URISyntaxException | IOException | NotBoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void updateNightMode() {
        boolean nightMode = mPreferences.general().isNightMode();
        FxHelper.setDarkThemeEnabled(nightMode);

        if (nightMode) {
            FxHelper.loadDarkTheme(mStage.getScene());
        } else {
            FxHelper.unloadDarkTheme(mStage.getScene());
        }
    }

    private void updateWindowTitle() {
        if (mManager.isConnected()) {
            mStage.setTitle(String.format(mBundle.getString("windowTitle"), mManager.getClient().getHost(), mManager.getClient().getPortHost()));
        } else {
            mStage.setTitle(APP_TITLE);
        }
    }

}
