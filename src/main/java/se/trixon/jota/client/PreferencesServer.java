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
import java.rmi.RemoteException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.jota.client.ui.PreferencesModule;
import se.trixon.jota.shared.ServerCommander;
import se.trixon.jota.shared.ServerEvent;
import se.trixon.jota.shared.ServerEventAdapter;

/**
 *
 * @author Patrik Karlström
 */
public class PreferencesServer {

    protected final Logger LOGGER = Logger.getLogger(getClass().getName());
    private ChangeListener<String> logPathChangeListener;

    private final ResourceBundle mBundle = SystemHelper.getBundle(PreferencesModule.class, "Bundle");
    private final Group mGroup;
    private final SimpleStringProperty mLogPathProperty = new SimpleStringProperty("a/b/c");
    private Manager mManager = Manager.getInstance();
    private final SimpleStringProperty mRsyncPathProperty = new SimpleStringProperty("rsync");
    private final BooleanProperty mScheduledSyncProperty = new SimpleBooleanProperty(true);
    private ChangeListener<String> rsyncPathChangeListener;
    private ChangeListener<Boolean> scheduledSyncChangeListener;

    public PreferencesServer() {
        mGroup = Group.of(Dict.SERVER.toString(),
                Setting.of(mBundle.getString("prefs.general.scheduledSync"), mScheduledSyncProperty).customKey("general.scheduledSync"),
                Setting.of(mBundle.getString("prefs.server.rsync"), mRsyncPathProperty).customKey("server.path.rsync2"),
                Setting.of(Dict.LOG_DIRECTORY.toString(), mLogPathProperty).customKey("server.path.log2")
        );
        initListeners();
        if (mManager.isConnected()) {
            loadServerPreferences();
        }

    }

    public Group getGroup() {
        return mGroup;
    }

    public String getLogPath() {
        return mLogPathProperty.get();
    }

    public String getRsyncPath() {
        return mRsyncPathProperty.get();
    }

    public boolean isScheduledSync() {
        return mScheduledSyncProperty.get();
    }

    public SimpleStringProperty logPathProperty() {
        return mLogPathProperty;
    }

    public SimpleStringProperty rsyncPathProperty() {
        return mRsyncPathProperty;
    }

    public BooleanProperty scheduledSyncProperty() {
        return mScheduledSyncProperty;
    }

    public void setLogPath(String path) {
        mLogPathProperty.set(path);
    }

    public void setRsyncPath(String path) {
        mRsyncPathProperty.set(path);
    }

    public void setScheduledSync(boolean state) {
        mScheduledSyncProperty.set(state);
    }

    protected ServerCommander getServerCommander() {
        return mManager.getServerCommander();
    }

    private void addListeners() {
        rsyncPathProperty().addListener(rsyncPathChangeListener);
        logPathProperty().addListener(logPathChangeListener);
        scheduledSyncProperty().addListener(scheduledSyncChangeListener);
    }

    private void initListeners() {
        rsyncPathChangeListener = (ObservableValue<? extends String> ov, String t, String t1) -> {
            ServerCommander serverCommander = getServerCommander();
            if (serverCommander != null) {
                try {
                    serverCommander.setRsyncPath(t1);
                } catch (RemoteException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        };

        logPathChangeListener = (ObservableValue<? extends String> ov, String t, String t1) -> {
            ServerCommander serverCommander = getServerCommander();
            if (serverCommander != null) {
                try {
                    serverCommander.setLogDir(t1);
                } catch (RemoteException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        };

        scheduledSyncChangeListener = (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            ServerCommander serverCommander = getServerCommander();
            if (serverCommander != null) {
                try {
                    serverCommander.setCronActive(t1);
                } catch (RemoteException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        };

        mManager.connectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            if (t1) {
                loadServerPreferences();
            } else {
                removeListeners();
                setScheduledSync(false);
                setRsyncPath("-");
                setLogPath("-");
            }
        });
        mManager.getClient().addServerEventListener(new ServerEventAdapter() {

            @Override
            public void onServerEvent(ServerEvent serverEvent) {
                Platform.runLater(() -> {
                    if (serverEvent == ServerEvent.CRON_CHANGED) {
                        try {
                            setScheduledSync(mManager.getServerCommander().isCronActive());
                        } catch (RemoteException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }
        });
    }

    private void loadServerPreferences() {
        addListeners();

        try {
            setScheduledSync(getServerCommander().isCronActive());
            setRsyncPath(getServerCommander().getRsyncPath());
            setLogPath(getServerCommander().getLogDir());
        } catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void removeListeners() {
        rsyncPathProperty().removeListener(rsyncPathChangeListener);
        logPathProperty().removeListener(logPathChangeListener);
        scheduledSyncProperty().removeListener(scheduledSyncChangeListener);
    }

}
