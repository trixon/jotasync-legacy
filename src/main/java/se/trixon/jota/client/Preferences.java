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
package se.trixon.jota.client;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.view.PreferencesFxView;

/**
 * Model object for Preferences.
 */
public class Preferences {

    private final PreferencesClient mPreferencesClient = new PreferencesClient();
    private final PreferencesFx mPreferencesFx;
    private final PreferencesGeneral mPreferencesGeneral = new PreferencesGeneral();
    private final PreferencesServer mPreferencesServer = new PreferencesServer();

    public static Preferences getInstance() {
        return Holder.INSTANCE;
    }

    private Preferences() {
        mPreferencesFx = createPreferences();
    }

    public PreferencesClient client() {
        return mPreferencesClient;
    }

    public void discardChanges() {
        mPreferencesFx.discardChanges();
    }

    public PreferencesGeneral general() {
        return mPreferencesGeneral;
    }

    public PreferencesFxView getPreferencesFxView() {
        return mPreferencesFx.getView();
    }

    public void save() {
        mPreferencesFx.saveSettings();
    }

    public PreferencesServer server() {
        return mPreferencesServer;
    }

    private PreferencesFx createPreferences() {
        return PreferencesFx
                .of(Preferences.class,
                        Category.of("",
                                mPreferencesGeneral.getGroup(),
                                mPreferencesClient.getGroup(),
                                mPreferencesServer.getGroup()
                        )
                )
                .persistWindowState(false)
                .saveSettings(true)
                .debugHistoryMode(false)
                .buttonsVisibility(true);
    }

    private static class Holder {

        private static final Preferences INSTANCE = new Preferences();
    }

}
