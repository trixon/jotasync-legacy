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

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import com.dlsc.preferencesfx.view.PreferencesFxView;
import java.rmi.RemoteException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.jota.client.ui.App;
import se.trixon.jota.shared.ServerCommander;
import se.trixon.jota.shared.ServerEvent;
import se.trixon.jota.shared.ServerEventAdapter;

/**
 * Model object for Preferences.
 */
public class PreferencesServer {

    private final ResourceBundle mBundle = SystemHelper.getBundle(App.class, "Bundle");
    private final ClientOptions mClientOptions = ClientOptions.getInstance();
    private final Manager mManager = Manager.getInstance();
    private final PreferencesFx mPreferencesFx;
    private final Server mServer = new Server();

    public static PreferencesServer getInstance() {
        return Holder.INSTANCE;
    }

    private PreferencesServer() {
        mPreferencesFx = createPreferences();
    }

    public void discardChanges() {
        mPreferencesFx.discardChanges();
    }

    public PreferencesFx getPreferencesFx() {
        return mPreferencesFx;
    }

    public PreferencesFxView getPreferencesFxView() {
        return mPreferencesFx.getView();
    }

    public void save() {
        mPreferencesFx.saveSettings();
    }

    public Server server() {
        return mServer;
    }

    private PreferencesFx createPreferences() {
        return PreferencesFx.of(PreferencesServer.class, Category.of("",
                mServer.getGroup()
        )).persistWindowState(false)
                .saveSettings(true)
                .instantPersistent(true)
                .debugHistoryMode(false)
                .buttonsVisibility(false);
    }

    private static class Holder {

        private static final PreferencesServer INSTANCE = new PreferencesServer();
    }

    public class Server {

        protected final Logger LOGGER = Logger.getLogger(getClass().getName());
        private ChangeListener<String> logPathChangeListener;

        private final Group mGroup;
        private final SimpleStringProperty mLogPathProperty = new SimpleStringProperty("a/b/c");
        private final SimpleStringProperty mRsyncPathProperty = new SimpleStringProperty("rsync");
        private final BooleanProperty mScheduledSyncProperty = new SimpleBooleanProperty(true);
        private ChangeListener<String> rsyncPathChangeListener;
        private ChangeListener<Boolean> scheduledSyncChangeListener;

        public Server() {
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
            rsyncPathChangeListener = (ov, t, t1) -> {
                var serverCommander = getServerCommander();
                if (serverCommander != null) {
                    try {
                        serverCommander.setRsyncPath(t1);
                    } catch (RemoteException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            };

            logPathChangeListener = (ov, t, t1) -> {
                var serverCommander = getServerCommander();
                if (serverCommander != null) {
                    try {
                        serverCommander.setLogDir(t1);
                    } catch (RemoteException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            };

            scheduledSyncChangeListener = (ov, t, t1) -> {
                var serverCommander = getServerCommander();
                if (serverCommander != null) {
                    try {
                        serverCommander.setCronActive(t1);
                    } catch (RemoteException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            };

            mManager.connectedProperty().addListener((ov, t, t1) -> {
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
}
