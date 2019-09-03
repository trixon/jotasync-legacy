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
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import static se.trixon.jota.client.ui.MainApp.*;

/**
 *
 * @author Patrik Karlström
 */
public class StartModule extends BaseModule {

    private BorderPane mBorderPane;
    private final ResourceBundle mBundle = SystemHelper.getBundle(MainApp.class, "Bundle");
    private Action mClientConnectAction;
    private Action mClientDisconnectAction;
    private Action mEditorAction;
    private ListView<String> mListView;
    private Action mServerShutdownAction;
    private Action mServerShutdownQuitAction;
    private Action mServerStartAction;
    private WebView mWebView;

    public StartModule() {
        super(Dict.HOME.toString(), MaterialIcon._Action.HOME.getImageView(MainApp.ICON_SIZE_MODULE).getImage());

        createUI();
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

    private void initAccelerators() {
        mEditorAction.setAccelerator(new KeyCodeCombination(KeyCode.J, KeyCombination.SHORTCUT_DOWN));
    }

    private void initActions() {
        // CONNECTION
        //Connect
        mClientConnectAction = new Action(Dict.CONNECT_TO_SERVER.toString(), (ActionEvent event) -> {
        });
        mClientConnectAction.setGraphic(MaterialIcon._Communication.CALL_MADE.getImageView(ICON_SIZE_DRAWER));

        //Disconnect
        mClientDisconnectAction = new Action(Dict.DISCONNECT.toString(), (ActionEvent event) -> {
        });
        mClientDisconnectAction.setGraphic(MaterialIcon._Communication.CALL_RECEIVED.getImageView(ICON_SIZE_DRAWER));

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
                    TaskModule taskModule = new TaskModule();
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
}
