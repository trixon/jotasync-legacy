/*
 * Copyright 2018 Patrik Karlström.
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

import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.AlmondFx;

/**
 *
 * @author Patrik Karlström
 */
public class MainApp extends Application {

    public static final String APP_TITLE = "FileByDate";
    private static final int ICON_SIZE_PROFILE = 32;
    private static final int ICON_SIZE_TOOLBAR = 40;
    private static final boolean IS_MAC = SystemUtils.IS_OS_MAC;
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    private Stage mStage;
    private final AlmondFx mAlmondFX = AlmondFx.getInstance();
    private final ResourceBundle mBundle = SystemHelper.getBundle(MainApp.class, "Bundle");
    private BorderPane mRoot;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        mStage = stage;
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("sync-256px.png")));

        mAlmondFX.addStageWatcher(stage, MainApp.class);
        createUI();
//        postInit();
//        initListeners();
        if (IS_MAC) {
//            initMac();
        }
        mStage.setTitle(APP_TITLE);
        mStage.show();
//        mListView.requestFocus();
//        initAccelerators();
    }

    private void createUI() {
        mRoot = new BorderPane();
        Scene scene = new Scene(mRoot);
        //scene.getStylesheets().add("css/modena_dark.css");

//        mDefaultFont = Font.getDefault();
//        initActions();
//
//        mListView = new ListView<>();
//        mListView.setItems(mItems);
//        mListView.setCellFactory((ListView<Profile> param) -> new ProfileListCell());
//        Label welcomeLabel = new Label(mBundle.getString("welcome"));
//        welcomeLabel.setFont(Font.font(mDefaultFont.getName(), FontPosture.ITALIC, 18));
//
//        mListView.setPlaceholder(welcomeLabel);
//
//        mPreviewPanel = new PreviewPanel();
//
//        mRoot.setCenter(mListView);
//        mRoot.setBottom(mPreviewPanel);
        mStage.setScene(scene);
    }

}
