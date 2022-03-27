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

import javafx.scene.control.Tab;
import se.trixon.jota.client.Preferences;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseTab extends Tab {

    protected final Preferences mPreferences = Preferences.getInstance();

    public BaseTab() {
        initListeners();
    }

    protected abstract void updateIconColor();

    private void initListeners() {
        mPreferences.general().iconColorProperty().addListener((observable, oldValue, newValue) -> {
            updateIconColor();
        });
    }

}
