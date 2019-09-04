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

import com.dlsc.workbenchfx.model.WorkbenchModule;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import se.trixon.jota.client.Client;
import se.trixon.jota.client.ClientOptions;
import se.trixon.jota.client.Manager;
import se.trixon.jota.client.Preferences;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseModule extends WorkbenchModule {

    protected final Client mClient;
    protected final Manager mManager = Manager.getInstance();
    protected final ClientOptions mOptions = ClientOptions.INSTANCE;
    protected final Preferences mPreferences = Preferences.getInstance();
    private final Scene mScene;

    public BaseModule(Scene scene, String name, Image icon) {
        super(name, icon);
        mScene = scene;
        mClient = mManager.getClient();
        initListeners();
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

    private void initListeners() {
        mPreferences.general().nightModeProperty().addListener((observable, oldValue, newValue) -> setNightMode(newValue));
    }
}
