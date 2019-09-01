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
import java.io.File;
import java.util.ResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.jota.client.ui.PreferencesModule;

/**
 *
 * @author Patrik Karlström
 */
public class PreferencesServer {

    private final ResourceBundle mBundle = SystemHelper.getBundle(PreferencesModule.class, "Bundle");
    private final Group mGroup;
    private final ObjectProperty<File> mLogPathFileProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<File> mRsyncPathFileProperty = new SimpleObjectProperty<>(new File("/usr/bin/rsync"));

    public PreferencesServer() {
        mGroup = Group.of(Dict.SERVER.toString(),
                Setting.of(mBundle.getString("prefs.server.rsync"), mRsyncPathFileProperty, false).customKey("server.path.rsync"),
                Setting.of(Dict.LOG_DIRECTORY.toString(), mLogPathFileProperty, true).customKey("server.path.log")
        );
    }

    public Group getGroup() {
        return mGroup;
    }

    public File getLogPath() {
        return mLogPathFileProperty.get();
    }

    public File getRsyncPath() {
        return mRsyncPathFileProperty.get();
    }

    public ObjectProperty<File> rsyncPathProperty() {
        return mRsyncPathFileProperty;
    }

    public void setLogPath(File file) {
        mLogPathFileProperty.set(file);
    }

    public void setRsyncPath(File file) {
        mRsyncPathFileProperty.set(file);
    }

}
