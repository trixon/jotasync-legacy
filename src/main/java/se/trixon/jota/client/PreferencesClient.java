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
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.jota.client.ui.PreferencesModule;

/**
 *
 * @author Patrik Karlström
 */
public class PreferencesClient {

    private final BooleanProperty mAutoStartProperty = new SimpleBooleanProperty(true);
    private final ResourceBundle mBundle = SystemHelper.getBundle(PreferencesModule.class, "Bundle");
    private final ClientOptions mClientOptions = ClientOptions.INSTANCE;
    private final IntegerProperty mDelayProperty = new SimpleIntegerProperty(500);
    private final Group mGroup;
    private final IntegerProperty mPortProperty = new SimpleIntegerProperty(1099);

    public PreferencesClient() {
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
        mAutoStartProperty.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            mClientOptions.setAutostartServer(t1);
        });

        mPortProperty.addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            mClientOptions.setAutostartServerPort(t1.intValue());
        });

        mDelayProperty.addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            mClientOptions.setAutostartServerConnectDelay(t1.intValue());
        });
    }
}
