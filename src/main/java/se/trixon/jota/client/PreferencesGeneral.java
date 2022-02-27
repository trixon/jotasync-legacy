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

import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.jota.client.ui.PreferencesModule;

/**
 *
 * @author Patrik Karlström
 */
public class PreferencesGeneral {

    private final ResourceBundle mBundle = SystemHelper.getBundle(PreferencesModule.class, "Bundle");

    private final Group mGroup;
    private final BooleanProperty mIncludeDryRunProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty mNightModeProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mSplitDeletionsProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mSplitErrorsProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mWordWrapProperty = new SimpleBooleanProperty(false);

    public PreferencesGeneral() {
        mGroup = Group.of(Dict.GENERAL.toString(),
                Setting.of(Dict.NIGHT_MODE.toString(), mNightModeProperty).customKey("general.darkTheme"),
                Setting.of(Dict.DYNAMIC_WORD_WRAP.toString(), mWordWrapProperty).customKey("general.wordWrap"),
                Setting.of(mBundle.getString("prefs.general.includeDryRun"), mIncludeDryRunProperty).customKey("general.includeDryRun"),
                Setting.of(mBundle.getString("prefs.general.splitDeletions"), mSplitDeletionsProperty).customKey("general.splitDeletions"),
                Setting.of(mBundle.getString("prefs.general.splitErrors"), mSplitErrorsProperty).customKey("general.splitErrors")
        );
    }

    public Group getGroup() {
        return mGroup;
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
