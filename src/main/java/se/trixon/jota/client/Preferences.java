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
package se.trixon.jota.client;

import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import com.dlsc.preferencesfx.view.PreferencesFxView;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jota.client.ui.App;

/**
 * Model object for Preferences.
 */
public class Preferences {

    private final ResourceBundle mBundle = SystemHelper.getBundle(App.class, "Bundle");
    private final Client mClient = new Client();
    private final ClientOptions mClientOptions = ClientOptions.getInstance();
    private final General mGeneral = new General();
    private final Manager mManager = Manager.getInstance();
    private final PreferencesFx mPreferencesFx;

    public static Preferences getInstance() {
        return Holder.INSTANCE;
    }

    private Preferences() {
        mPreferencesFx = createPreferences();
    }

    public Client client() {
        return mClient;
    }

    public void discardChanges() {
        mPreferencesFx.discardChanges();
    }

    public General general() {
        return mGeneral;
    }

    public PreferencesFx getPreferencesFx() {
        return mPreferencesFx;
    }

    public PreferencesFxView getPreferencesFxView() {
        return mPreferencesFx.getView();
    }

    public Color getThemedIconColor() {
        if (general().isNightMode()) {
            return Color.LIGHTGRAY;
        } else {
            return Color.DIMGRAY.darker();
        }
    }

    public void save() {
        mPreferencesFx.saveSettings();
    }

    private PreferencesFx createPreferences() {
        return PreferencesFx.of(Preferences.class, Category.of("",
                mGeneral.getGroup(),
                mClient.getGroup()
        )).persistWindowState(false)
                .saveSettings(true)
                .instantPersistent(true)
                .debugHistoryMode(false)
                .buttonsVisibility(false);
    }

    private static class Holder {

        private static final Preferences INSTANCE = new Preferences();
    }

    public class Client {

        private final BooleanProperty mAutoStartProperty = new SimpleBooleanProperty(true);
        private final IntegerProperty mDelayProperty = new SimpleIntegerProperty(500);
        private final Group mGroup;
        private final IntegerProperty mPortProperty = new SimpleIntegerProperty(1099);

        public Client() {
            //TODO Replace errorMessage
            mGroup = Group.of(Dict.CLIENT.toString(),
                    Setting.of(mBundle.getString("prefs.client.autostartServer"), mAutoStartProperty).customKey("client.autostart"),
                    Setting.of(Dict.PORT.toString(), mPortProperty).customKey("client.port")
                            .validate(IntegerRangeValidator.between(1024, 65535, "errorMessage")),
                    Setting.of(mBundle.getString("prefs.client.connectDelay"), mDelayProperty).customKey("client.delay")
                            .validate(IntegerRangeValidator.between(100, 3000, "errorMessage"))
            );

            initListeners();
        }

        public BooleanProperty autoStartProperty() {
            return mAutoStartProperty;
        }

        public int getDelay() {
            return mDelayProperty.get();
        }

        public Group getGroup() {
            return mGroup;
        }

        public int getPort() {
            return mPortProperty.get();
        }

        public boolean isAutoStart() {
            return mAutoStartProperty.get();
        }

        private void initListeners() {
            //TODO Deprecate when resolved: https://github.com/dlsc-software-consulting-gmbh/PreferencesFX/issues/85
            mAutoStartProperty.addListener((ov, t, t1) -> {
                mClientOptions.setAutostartServer(t1);
            });

            mPortProperty.addListener((ov, t, t1) -> {
                mClientOptions.setAutostartServerPort(t1.intValue());
            });

            mDelayProperty.addListener((ov, t, t1) -> {
                mClientOptions.setAutostartServerConnectDelay(t1.intValue());
            });
        }
    }

    public class General {

        private final Group mGroup;
        private final ObjectProperty<Color> mIconColorProperty = new SimpleObjectProperty<>();
        private final BooleanProperty mIncludeDryRunProperty = new SimpleBooleanProperty(false);
        private final BooleanProperty mNightModeProperty = new SimpleBooleanProperty(true);
        private final BooleanProperty mSplitDeletionsProperty = new SimpleBooleanProperty(true);
        private final BooleanProperty mSplitErrorsProperty = new SimpleBooleanProperty(true);
        private final BooleanProperty mWordWrapProperty = new SimpleBooleanProperty(false);

        public General() {
            mGroup = Group.of(Dict.GENERAL.toString(),
                    Setting.of(Dict.NIGHT_MODE.toString(), mNightModeProperty).customKey("general.darkTheme"),
                    Setting.of(Dict.DYNAMIC_WORD_WRAP.toString(), mWordWrapProperty).customKey("general.wordWrap"),
                    Setting.of(mBundle.getString("prefs.general.includeDryRun"), mIncludeDryRunProperty).customKey("general.includeDryRun"),
                    Setting.of(mBundle.getString("prefs.general.splitDeletions"), mSplitDeletionsProperty).customKey("general.splitDeletions"),
                    Setting.of(mBundle.getString("prefs.general.splitErrors"), mSplitErrorsProperty).customKey("general.splitErrors")
            );

            mNightModeProperty.addListener((observable, oldValue, newValue) -> {
                MaterialIcon.setDefaultColor(newValue ? Color.LIGHTGRAY : Color.BLACK);
                mIconColorProperty.set(MaterialIcon.getDefaultColor());
            });

            MaterialIcon.setDefaultColor(isNightMode() ? Color.LIGHTGRAY : Color.BLACK);
        }

        public Group getGroup() {
            return mGroup;
        }

        public ObjectProperty iconColorProperty() {
            return mIconColorProperty;
        }

        public BooleanProperty includeDryRunProperty() {
            return mIncludeDryRunProperty;
        }

        public boolean isIncludeDryRun() {
            return mIncludeDryRunProperty.get();
        }

        public boolean isNightMode() {
            return mNightModeProperty.get();
        }

        public boolean isSplitDeletions() {
            return mSplitDeletionsProperty.get();
        }

        public boolean isSplitErrors() {
            return mSplitErrorsProperty.get();
        }

        public boolean isWordWrap() {
            return mWordWrapProperty.get();
        }

        public BooleanProperty nightModeProperty() {
            return mNightModeProperty;
        }

        public BooleanProperty splitDeletionsProperty() {
            return mSplitDeletionsProperty;
        }

        public BooleanProperty splitErrorsProperty() {
            return mSplitErrorsProperty;
        }

        public BooleanProperty wordWrapProperty() {
            return mWordWrapProperty;
        }

    }
}
