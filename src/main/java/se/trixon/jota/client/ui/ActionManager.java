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

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import se.trixon.almond.util.AlmondAction;
import se.trixon.almond.util.AlmondActionManager;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.material.swing.MaterialIcon;
import se.trixon.almond.util.swing.dialogs.MenuModePanel;

/**
 *
 * @author Patrik Karlström
 */
public class ActionManager extends AlmondActionManager {

    static final String ABOUT_RSYNC = "about_rsync";
    static final String CLOSE_TAB = "closeTab";
    static final String CONNECT = "connect";
    static final String CRON = "cron";
    static final String DISCONNECT = "disconnect";
    static final String JOB_EDITOR = "jobeditor";
    static final String SAVE_TAB = "saveTab";
    static final String SHUTDOWN_SERVER = "shutdownServer";
    static final String SHUTDOWN_SERVER_QUIT = "shutdownServerAndQuit";
    static final String START_SERVER = "startServer";

    private final HashSet<AppListener> mAppListeners = new HashSet<>();
    private final ResourceBundle mBundle = SystemHelper.getBundle(MainFrame.class, "Bundle");

    public static ActionManager getInstance() {
        return Holder.INSTANCE;
    }

    private ActionManager() {
    }

    public void addAppListener(AppListener appListener) {
        mAppListeners.add(appListener);
    }

    public ActionManager init(ActionMap actionMap, InputMap inputMap) {
        mActionMap = actionMap;
        mInputMap = inputMap;
        AlmondAction action;
        KeyStroke keyStroke;
        int commandMask = SystemHelper.getCommandMask();

        initHelpAction("https://trixon.se/projects/jotasync/documentation/");

        if (mAlmondOptions.getMenuMode() == MenuModePanel.MenuMode.BUTTON) {
            //menu
            int menuKey = KeyEvent.VK_M;
            keyStroke = KeyStroke.getKeyStroke(menuKey, commandMask);
            action = new AlmondAction(Dict.MENU.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mAppListeners.forEach((appActionListener) -> {
                        try {
                            appActionListener.onMenu(e);
                        } catch (Exception exception) {
                        }
                    });
                }
            };

            initAction(action, MENU, keyStroke, MaterialIcon._Navigation.MENU, true);
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0);
            mInputMap.put(keyStroke, MENU);
        }

        //options
        keyStroke = KeyStroke.getKeyStroke(getOptionsKey(), commandMask);
        keyStroke = IS_MAC ? null : keyStroke;
        action = new AlmondAction(Dict.OPTIONS.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appActionListener) -> {
                    try {
                        appActionListener.onOptions(e);
                    } catch (Exception exception) {
                    }
                });
            }

        };

        initAction(action, OPTIONS, keyStroke, MaterialIcon._Action.SETTINGS, true);

        //start
        keyStroke = null;
        action = new AlmondAction(Dict.START.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appActionListener) -> {
                    try {
                        appActionListener.onStart(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, START, keyStroke, MaterialIcon._Av.PLAY_ARROW, false);

        //cancel
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        action = new AlmondAction(Dict.CANCEL.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appActionListener) -> {
                    try {
                        appActionListener.onCancel(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, CANCEL, keyStroke, MaterialIcon._Content.CLEAR, false);

        //save tab
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, commandMask);
        action = new AlmondAction(Dict.SAVE.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appListener) -> {
                    try {
                        appListener.onSave(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, SAVE_TAB, keyStroke, MaterialIcon._Content.SAVE, false);

        //close tab
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_W, commandMask);
        action = new AlmondAction(Dict.TAB_CLOSE.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appListener) -> {
                    try {
                        appListener.onClose(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, CLOSE_TAB, keyStroke, MaterialIcon._Navigation.CLOSE, false);

        //start Server
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_O, commandMask | InputEvent.SHIFT_MASK);
        action = new AlmondAction(Dict.START.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appListener) -> {
                    try {
                        appListener.onServerStart(e);
                    } catch (Exception exception) {
                    }
                });

            }
        };

        initAction(action, START_SERVER, keyStroke, null, true);

        //shutdown Server
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, commandMask | InputEvent.SHIFT_MASK);
        action = new AlmondAction(Dict.SHUTDOWN.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appListener) -> {
                    try {
                        appListener.onServerShutdown(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, SHUTDOWN_SERVER, keyStroke, null, false);

        //shutdown server and quit
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, commandMask + InputEvent.SHIFT_MASK);
        action = new AlmondAction(Dict.SHUTDOWN_AND_QUIT.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appListener) -> {
                    try {
                        appListener.onServerShutdownAndQuit(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, SHUTDOWN_SERVER_QUIT, keyStroke, null, false);

        //connect
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_O, commandMask);
        action = new AlmondAction(Dict.CONNECT_TO_SERVER.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appListener) -> {
                    try {
                        appListener.onClientConnect(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, CONNECT, keyStroke, MaterialIcon._Communication.CALL_MADE, true);

        //disconnect
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, commandMask);
        action = new AlmondAction(Dict.DISCONNECT.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appListener) -> {
                    try {
                        appListener.onClientDisconnect(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, DISCONNECT, keyStroke, MaterialIcon._Communication.CALL_RECEIVED, false);

        //cron
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_T, commandMask);
        action = new AlmondAction(mBundle.getString("schedule")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appListener) -> {
                    try {
                        appListener.onCron(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, CRON, keyStroke, MaterialIcon._Action.SCHEDULE, false);
        action.putValue(Action.SELECTED_KEY, false);

        //jobEditor
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_J, commandMask);
        action = new AlmondAction(mBundle.getString("jobEditor")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appListener) -> {
                    try {
                        appListener.onEdit(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, JOB_EDITOR, keyStroke, MaterialIcon._Editor.MODE_EDIT, false);

        //about rsync
        keyStroke = null;
        action = new AlmondAction(String.format(Dict.ABOUT_S.toString(), "rsync")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appActionListener) -> {
                    try {
                        appActionListener.onAboutRsync(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, ABOUT_RSYNC, keyStroke, null, false);

        //quit
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, commandMask);
        action = new AlmondAction(Dict.QUIT.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appActionListener) -> {
                    try {
                        appActionListener.onQuit(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, QUIT, keyStroke, MaterialIcon._Content.CLEAR, true);

        return this;
    }

    public static class AppAdapter implements AppListener {

        @Override
        public void onAboutRsync(ActionEvent actionEvent) {
        }

        @Override
        public void onCancel(ActionEvent actionEvent) {
        }

        @Override
        public void onClientConnect(ActionEvent actionEvent) {
        }

        @Override
        public void onClientDisconnect(ActionEvent actionEvent) {
        }

        @Override
        public void onClose(ActionEvent actionEvent) {
        }

        @Override
        public void onCron(ActionEvent actionEvent) {
        }

        @Override
        public void onEdit(ActionEvent actionEvent) {
        }

        @Override
        public void onMenu(ActionEvent actionEvent) {
        }

        @Override
        public void onOptions(ActionEvent actionEvent) {
        }

        @Override
        public void onQuit(ActionEvent actionEvent) {
        }

        @Override
        public void onSave(ActionEvent actionEvent) {
        }

        @Override
        public void onServerShutdown(ActionEvent actionEvent) {
        }

        @Override
        public void onServerShutdownAndQuit(ActionEvent actionEvent) {
        }

        @Override
        public void onServerStart(ActionEvent actionEvent) {
        }

        @Override
        public void onStart(ActionEvent actionEvent) {
        }
    }

    public interface AppListener {

        void onAboutRsync(ActionEvent actionEvent);

        void onCancel(ActionEvent actionEvent);

        void onClientConnect(ActionEvent actionEvent);

        void onClientDisconnect(ActionEvent actionEvent);

        void onClose(ActionEvent actionEvent);

        void onCron(ActionEvent actionEvent);

        void onEdit(ActionEvent actionEvent);

        void onMenu(ActionEvent actionEvent);

        void onOptions(ActionEvent actionEvent);

        void onQuit(ActionEvent actionEvent);

        void onSave(ActionEvent actionEvent);

        void onServerShutdown(ActionEvent actionEvent);

        void onServerShutdownAndQuit(ActionEvent actionEvent);

        void onServerStart(ActionEvent actionEvent);

        void onStart(ActionEvent actionEvent);
    }

    private static class Holder {

        private static final ActionManager INSTANCE = new ActionManager();
    }
}
