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

import com.dlsc.workbenchfx.model.WorkbenchModule;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import se.trixon.almond.util.fx.CategoryBooleanManager;
import se.trixon.jota.client.Client;
import se.trixon.jota.client.ClientOptions;
import se.trixon.jota.client.Manager;
import se.trixon.jota.client.Preferences;
import se.trixon.jota.shared.ServerCommander;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseModule extends WorkbenchModule {

    protected final Logger LOGGER = Logger.getLogger(getClass().getName());
    protected CategoryBooleanManager mCategoryActionManager = CategoryBooleanManager.getInstance();
    protected final Client mClient;
    protected Font mDefaultFont = Font.getDefault();
    protected JobController mJobController = JobController.getInstance();
    protected final Manager mManager = Manager.getInstance();
    protected final ClientOptions mOptions = ClientOptions.INSTANCE;
    protected final Preferences mPreferences = Preferences.getInstance();
    protected Stage mStage;
    private final Scene mScene;

    public BaseModule(Scene scene, String name, Image icon) {
        super(name, icon);
        mScene = scene;
        mStage = (Stage) scene.getWindow();
        mClient = mManager.getClient();
        initListeners();
    }

    public void connectionConnect() {
    }

    public void connectionDisconnect() {
    }

    public Scene getScene() {
        return mScene;
    }

    public Stage getStage() {
        return (Stage) getWorkbench().getScene().getWindow();
    }

    public void postInit() {
        setNightMode(mPreferences.general().isNightMode());
    }

    public void setNightMode(boolean state) {
    }

    protected ServerCommander getServerCommander() {
        return mManager.getServerCommander();
    }

    private void initListeners() {
        mManager.connectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean connected) -> {
            if (connected) {
                connectionConnect();
            } else {
                connectionDisconnect();
            }
        });

        mPreferences.general().nightModeProperty().addListener((observable, oldValue, newValue) -> setNightMode(newValue));
    }
}
