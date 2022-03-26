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
import java.util.Collection;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.SystemUtils;
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
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jota.client.Client;
import se.trixon.jota.client.ClientOptions;
import se.trixon.jota.client.Manager;
import se.trixon.jota.client.Preferences;

/**
 *
 * @author Patrik Karlström
 */
public class App extends Application {

    public static final String APP_TITLE = "JotaSync";
    private static final boolean IS_MAC = SystemUtils.IS_OS_MAC;
    protected final Client mClient;
    protected final ClientOptions mOptions = ClientOptions.getInstance();
    private final Logger LOGGER = Logger.getLogger(getClass().getName());
    private final AlmondFx mAlmondFX = AlmondFx.getInstance();
    private final ResourceBundle mBundle = SystemHelper.getBundle(App.class, "Bundle");
    private final Manager mManager = Manager.getInstance();
    private Preferences mPreferences = Preferences.getInstance();
    private BorderPane mRoot;
    private boolean mServerShutdownRequested;
    private Stage mStage;

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

        if (IS_MAC) {
            initMac();
        }

        updateNightMode();

        mStage.setTitle(APP_TITLE);
        FxHelper.removeSceneInitFlicker(mStage);

        mStage.show();
        initListeners();
//        mAppForm.initAccelerators();

        SnapHelperFx.checkSnapStatus(App.class, "snap", mStage, "jotasync", "removable-media");
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

        mRoot = new BorderPane();

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
        serverShutdownQuitAction.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        serverShutdownQuitAction.disabledProperty().bind(mManager.connectedProperty().not());

        var quitAction = new Action(Dict.QUIT.toString(), actionEvent -> {
            quit();
        });
        quitAction.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));

        var fileActionGroup = new ActionGroup(Dict.FILE.toString(),
                connectAction,
                disconnectAction,
                new ActionGroup(Dict.SERVER.toString(),
                        serverStartAction,
                        serverShutdownAction,
                        serverShutdownQuitAction
                ),
                ACTION_SEPARATOR,
                quitAction
        );

        var scheduleAction = new FxActionCheck(mBundle.getString("schedule"), actionEvent -> {
        });
        scheduleAction.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN));

        var editorAction = new Action(mBundle.getString("jobEditor"), actionEvent -> {
        });
        editorAction.setAccelerator(new KeyCodeCombination(KeyCode.J, KeyCombination.SHORTCUT_DOWN));

        var optionsAction = new Action(Dict.OPTIONS.toString(), actionEvent -> {
            displayOptions();
        });
        optionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));

        var toolsActionGroup = new ActionGroup(Dict.TOOLS.toString(),
                scheduleAction,
                editorAction,
                ACTION_SEPARATOR,
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

        Collection< ? extends Action> actions = Arrays.asList(fileActionGroup, toolsActionGroup, helpActionGroup);
        var menuBar = ActionUtils.createMenuBar(actions);
        var scene = new Scene(mRoot);

        mRoot.setTop(menuBar);
        mStage.setScene(scene);
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
        SystemHelper.desktopBrowse("https://trixon.se/projects/mapollage/documentation/");
    }

    private void displayOptions() {
//        if (mOptionsPanel == null) {
//            mOptionsPanel = new OptionsPanel();
//        }

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

    private void initListeners() {
        mPreferences.general().nightModeProperty().addListener((observable, oldValue, newValue) -> {
            updateNightMode();
        });
    }

    private void initMac() {
//        var menuToolkit = MenuToolkit.toolkit();
//        var applicationMenu = menuToolkit.createDefaultApplicationMenu(APP_TITLE);
//        menuToolkit.setApplicationMenu(applicationMenu);
//
//        applicationMenu.getItems().remove(0);
//        MenuItem aboutMenuItem = new MenuItem(String.format(Dict.ABOUT_S.toString(), APP_TITLE));
//        aboutMenuItem.setOnAction(mAboutAction);
//
//        var settingsMenuItem = new MenuItem(Dict.PREFERENCES.toString());
//        settingsMenuItem.setOnAction(mOptionsAction);
//        settingsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
//
//        applicationMenu.getItems().add(0, aboutMenuItem);
//        applicationMenu.getItems().add(2, settingsMenuItem);
//
//        int cnt = applicationMenu.getItems().size();
//        applicationMenu.getItems().get(cnt - 1).setText(String.format("%s %s", Dict.QUIT.toString(), APP_TITLE));
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
        mServerShutdownRequested = true;
        mClient.execute(Client.Command.SHUTDOWN);
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

        MaterialIcon.setDefaultColor(nightMode ? Color.LIGHTGRAY : Color.BLACK);
        FxHelper.setDarkThemeEnabled(nightMode);

        if (nightMode) {
            FxHelper.loadDarkTheme(mStage.getScene());
        } else {
            FxHelper.unloadDarkTheme(mStage.getScene());
        }
    }

}
