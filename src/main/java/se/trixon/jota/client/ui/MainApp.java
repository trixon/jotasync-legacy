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

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.model.WorkbenchDialog;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import de.codecentric.centerdevice.MenuToolkit;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import se.trixon.almond.util.AboutModel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.AlmondFx;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.dialogs.about.AboutPane;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jota.client.Manager;
import se.trixon.jota.client.ui.module.LogModule;
import se.trixon.jota.client.ui.module.PreferencesModule;
import se.trixon.jota.client.ui.module.StartModule;
import se.trixon.jota.client.ui.module.TaskModule;

/**
 *
 * @author Patrik Karlström
 */
public class MainApp extends Application {

    public static final String APP_TITLE = "JotaSync";
    public static final int ICON_SIZE_PROFILE = 32;
    public static final int ICON_SIZE_TOOLBAR = 40;
    public static final int ICON_SIZE_DRAWER = ICON_SIZE_TOOLBAR / 2;
    public static final int MODULE_ICON_SIZE = 32;
    private static final boolean IS_MAC = SystemUtils.IS_OS_MAC;
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    private Action mAboutAction;
    private final AlmondFx mAlmondFX = AlmondFx.getInstance();
    private final ResourceBundle mBundle = SystemHelper.getBundle(MainApp.class, "Bundle");
    private Action mHelpAction;
    private LogModule mLogModule = new LogModule();
    private final Manager mManager = Manager.getInstance();
    private Action mOptionsAction;
    private ToolbarItem mOptionsToolbarItem;
    private PreferencesModule mPreferencesModule = new PreferencesModule();
    private Stage mStage;
    private Workbench mWorkbench;
    private StartModule mStartModule;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        mStage = stage;
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("logo.png")));

        mAlmondFX.addStageWatcher(stage, MainApp.class);
        createUI();
        if (IS_MAC) {
            initMac();
        }
        mStage.setTitle(APP_TITLE);
        mStage.show();

        initAccelerators();
        mWorkbench.openModule(mStartModule);
    }

    @Override
    public void stop() throws Exception {
        mManager.disconnect();
        super.stop();
    }

    private void activateModule(int moduleIndexOnPage) {
        if (moduleIndexOnPage == 0) {
            moduleIndexOnPage = 10;
        }

        int pageIndex = 0;//TODO get actual page index
        int moduleIndex = pageIndex * mWorkbench.getModulesPerPage() + moduleIndexOnPage - 1;
        try {
            mWorkbench.openModule(mWorkbench.getModules().get(moduleIndex));
        } catch (IndexOutOfBoundsException e) {
            //nvm
        }
    }

    private void activateOpenModule(int moduleIndexOnPage) {
        if (moduleIndexOnPage == 0) {
            moduleIndexOnPage = 10;
        }

        try {
            mWorkbench.openModule(mWorkbench.getOpenModules().get(moduleIndexOnPage - 1));
        } catch (IndexOutOfBoundsException e) {
            //nvm
        }
    }

    private void createUI() {
        mStartModule = new StartModule();
        mWorkbench = Workbench.builder(mStartModule, mLogModule, mPreferencesModule)
                .tabFactory(CustomTab::new)
                .build();
        mWorkbench.getStylesheets().add(MainApp.class.getResource("customTheme.css").toExternalForm());

        mWorkbench.openModule(mStartModule);
        mWorkbench.openModule(mLogModule);
        mWorkbench.openModule(mPreferencesModule);
        initToolbar();
        initWorkbenchDrawer();

        Scene scene = new Scene(mWorkbench);

        mStage.setScene(scene);
    }

    private void displayOptions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void initAccelerators() {
        final ObservableMap<KeyCombination, Runnable> accelerators = mStage.getScene().getAccelerators();
        for (int i = 0; i < 10; i++) {
            final int index = i;
            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("DIGIT" + i), KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), (Runnable) () -> {
                activateModule(index);
            });

            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("NUMPAD" + i), KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), (Runnable) () -> {
                activateModule(index);
            });

            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("DIGIT" + i), KeyCombination.SHORTCUT_DOWN), (Runnable) () -> {
                activateOpenModule(index);
            });

            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("NUMPAD" + i), KeyCombination.SHORTCUT_DOWN), (Runnable) () -> {
                activateOpenModule(index);
            });
        }

        accelerators.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN), (Runnable) () -> {
            mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        accelerators.put(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN), (Runnable) () -> {
            if (mWorkbench.getActiveModule() != null) {
                mWorkbench.closeModule(mWorkbench.getActiveModule());
            }
        });

        if (!IS_MAC) {
            accelerators.put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN), (Runnable) () -> {
                displayOptions();
            });
        }
    }

    private void initMac() {
        MenuToolkit menuToolkit = MenuToolkit.toolkit();
        Menu applicationMenu = menuToolkit.createDefaultApplicationMenu(APP_TITLE);
        menuToolkit.setApplicationMenu(applicationMenu);

        applicationMenu.getItems().remove(0);
        MenuItem aboutMenuItem = new MenuItem(String.format(Dict.ABOUT_S.toString(), APP_TITLE));
        aboutMenuItem.setOnAction(mAboutAction);

        MenuItem settingsMenuItem = new MenuItem(Dict.PREFERENCES.toString());
        settingsMenuItem.setOnAction(mOptionsAction);
        settingsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));

        applicationMenu.getItems().add(0, aboutMenuItem);
        applicationMenu.getItems().add(2, settingsMenuItem);

        int cnt = applicationMenu.getItems().size();
        applicationMenu.getItems().get(cnt - 1).setText(String.format("%s %s", Dict.QUIT.toString(), APP_TITLE));
    }

    private void initToolbar() {
        ToolbarItem dummyRunToolbarItem = new ToolbarItem(Dict.RUN.toString(), MaterialIcon._Maps.DIRECTIONS_RUN.getImageView(ICON_SIZE_TOOLBAR, Color.LIGHTGRAY),
                event -> {
                    TaskModule taskModule = new TaskModule();
                    mWorkbench.getModules().add(taskModule);
                    mWorkbench.openModule(taskModule);
                }
        );
        mOptionsToolbarItem = new ToolbarItem(Dict.OPTIONS.toString(), MaterialIcon._Action.SETTINGS.getImageView(ICON_SIZE_TOOLBAR, Color.LIGHTGRAY),
                event -> {
                    //mWorkbench.openModule(mPreferencesModule);
                }
        );

        mWorkbench.getToolbarControlsRight().addAll(dummyRunToolbarItem, mOptionsToolbarItem);
    }

    private void initWorkbenchDrawer() {
        //options
        mOptionsAction = new Action(Dict.OPTIONS.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            displayOptions();
        });
        mOptionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
        mOptionsAction.setGraphic(MaterialIcon._Action.SETTINGS.getImageView(ICON_SIZE_DRAWER));

        //help
        mHelpAction = new Action(Dict.HELP.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            SystemHelper.desktopBrowse("https://trixon.se/projects/jotasync/documentation/");
        });
        //mHelpAction.setAccelerator(new KeyCodeCombination(KeyCode.F1, KeyCombination.SHORTCUT_ANY));
        mHelpAction.setAccelerator(KeyCombination.keyCombination("F1"));

        //about
        mAboutAction = new Action(Dict.ABOUT.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            PomInfo pomInfo = new PomInfo(MainApp.class, "se.trixon", "jotasync");
            AboutModel aboutModel = new AboutModel(
                    SystemHelper.getBundle(getClass(), "about"),
                    SystemHelper.getResourceAsImageView(MainApp.class, "logo.png")
            );
            aboutModel.setAppVersion(pomInfo.getVersion());
            AboutPane aboutPane = new AboutPane(aboutModel);

            double scaledFontSize = FxHelper.getScaledFontSize();
            Label appLabel = new Label(aboutModel.getAppName());
            appLabel.setFont(new Font(scaledFontSize * 1.8));
            Label verLabel = new Label(String.format("%s %s", Dict.VERSION.toString(), aboutModel.getAppVersion()));
            verLabel.setFont(new Font(scaledFontSize * 1.2));
            Label dateLabel = new Label(aboutModel.getAppDate());
            dateLabel.setFont(new Font(scaledFontSize * 1.2));

            VBox box = new VBox(appLabel, verLabel, dateLabel);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPadding(new Insets(0, 0, 0, 22));
            BorderPane topBorderPane = new BorderPane(box);
            topBorderPane.setLeft(aboutModel.getImageView());
            topBorderPane.setPadding(new Insets(22));
            BorderPane mainBorderPane = new BorderPane(aboutPane);
            mainBorderPane.setTop(topBorderPane);

            WorkbenchDialog dialog = WorkbenchDialog.builder(Dict.ABOUT.toString(), mainBorderPane, ButtonType.CLOSE).build();
            mWorkbench.showDialog(dialog);
        });

        mWorkbench.getNavigationDrawerItems().setAll(
                ActionUtils.createMenuItem(mHelpAction),
                ActionUtils.createMenuItem(mAboutAction)
        );

        if (!IS_MAC) {
            mOptionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
        }
    }
}
