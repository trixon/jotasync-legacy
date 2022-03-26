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

import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
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
import se.trixon.jota.client.Manager;
import se.trixon.jota.client.Preferences;

/**
 *
 * @author Patrik Karlström
 */
public class App extends Application {

    public static final String APP_TITLE = "JotaSync";
    private static final boolean IS_MAC = SystemUtils.IS_OS_MAC;
    private final AlmondFx mAlmondFX = AlmondFx.getInstance();
    private final ResourceBundle mBundle = SystemHelper.getBundle(App.class, "Bundle");
    private final Manager mManager = Manager.getInstance();
    private Preferences mPreferences = Preferences.getInstance();
    private BorderPane mRoot;
    private Stage mStage;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
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
        });
        connectAction.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        connectAction.disabledProperty().bind(mManager.connectedProperty());

        var disconnectAction = new Action(Dict.DISCONNECT.toString(), actionEvent -> {
        });
        disconnectAction.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN));
        disconnectAction.disabledProperty().bind(mManager.connectedProperty().not());

        var serverStartAction = new Action(Dict.SERVER_START.toString(), actionEvent -> {
        });
        serverStartAction.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        serverStartAction.disabledProperty().bind(mManager.connectedProperty().not());

        var serverStopAction = new Action(Dict.SERVER_SHUTDOWN.toString(), actionEvent -> {
        });
        serverStopAction.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        serverStopAction.disabledProperty().bind(mManager.connectedProperty().not());

        var serverQuitAction = new Action(Dict.SHUTDOWN_SERVER_AND_QUIT.toString(), actionEvent -> {
        });
        serverQuitAction.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        serverQuitAction.disabledProperty().bind(mManager.connectedProperty().not());

        var quitAction = new Action(Dict.QUIT.toString(), actionEvent -> {
            mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
        quitAction.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));

        var fileActionGroup = new ActionGroup(Dict.FILE.toString(),
                connectAction,
                disconnectAction,
                new ActionGroup(Dict.SERVER.toString(),
                        serverStartAction,
                        serverStopAction,
                        serverQuitAction
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
