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
    private final BooleanProperty mIncludeDryRun = new SimpleBooleanProperty(false);
    private final BooleanProperty mSplitDeletions = new SimpleBooleanProperty(true);
    private final BooleanProperty mSplitErrors = new SimpleBooleanProperty(true);
    private final BooleanProperty mWordWrap = new SimpleBooleanProperty(false);

    public PreferencesGeneral() {
        mGroup = Group.of(Dict.GENERAL.toString(),
                Setting.of(Dict.DYNAMIC_WORD_WRAP.toString(), mWordWrap).customKey("general.wordWrap"),
                Setting.of(mBundle.getString("prefs.general.includeDryRun"), mIncludeDryRun).customKey("general.includeDryRun"),
                Setting.of(mBundle.getString("prefs.general.splitDeletions"), mSplitDeletions).customKey("general.splitDeletions"),
                Setting.of(mBundle.getString("prefs.general.splitErrors"), mSplitErrors).customKey("general.splitErrors")
        );
    }

    public Group getGroup() {
        return mGroup;
    }

    public BooleanProperty includeDryRunProperty() {
        return mIncludeDryRun;
    }

    public boolean isIncludeDryRun() {
        return mIncludeDryRun.get();
    }

    public boolean isSplitDeletions() {
        return mSplitDeletions.get();
    }

    public boolean isSplitErrors() {
        return mSplitErrors.get();
    }

    public boolean isWordWrap() {
        return mWordWrap.get();
    }

    public BooleanProperty splitDeletionsProperty() {
        return mSplitDeletions;
    }

    public BooleanProperty splitErrorsProperty() {
        return mSplitErrors;
    }

    public BooleanProperty wordWrapProperty() {
        return mWordWrap;
    }

}
