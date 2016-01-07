/* 
 * Copyright 2016 Patrik Karlsson.
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

import java.util.prefs.Preferences;
import se.trixon.jota.shared.Jota;

/**
 *
 * @author Patrik Karlsson
 */
public enum ClientOptions {

    INSTANCE;
    public static final boolean DEFAULT_CUSTOM_COLORS = false;
    public static final String KEY_AUTOSTART_SERVER = "autostartServer";
    public static final String KEY_AUTOSTART_SERVER_CONNECT_DELAY = "autostartServerConnectDelay";
    public static final String KEY_AUTOSTART_SERVER_PORT = "autostartServerPort";
    public static final String KEY_CUSTOM_COLORS = "customColors";
    public static final String KEY_FORCE_LOOK_AND_FEEL = "forceLookAndFeel";
    public static final String KEY_HOSTS = "hosts";
    public static final String KEY_LOOK_AND_FEEL = "lookAndFeel";
    public static final String KEY_MENU_ICONS = "displayMenuIcons";
    private static final boolean DEFAULT_AUTOSTART_SERVER = false;
    private static final int DEFAULT_AUTOSTART_SERVER_CONNECT_DELAY = 500;
    private static final int DEFAULT_AUTOSTART_SERVER_PORT = Jota.DEFAULT_PORT_HOST;
    private static final boolean DEFAULT_FORCE_LOOK_AND_FEEL = true;
    private static final String DEFAULT_HOSTS = "localhost";
    private static final String DEFAULT_LOOK_AND_FEEL = "Nimbus";
    private static final boolean DEFAULT_MENU_ICONS = true;
    private final Preferences mPreferences;

    private ClientOptions() {
        mPreferences = Preferences.userNodeForPackage(this.getClass());
    }

    public int getAutostartServerConnectDelay() {
        return mPreferences.getInt(KEY_AUTOSTART_SERVER_CONNECT_DELAY, DEFAULT_AUTOSTART_SERVER_CONNECT_DELAY);
    }

    public int getAutostartServerPort() {
        return mPreferences.getInt(KEY_AUTOSTART_SERVER_PORT, DEFAULT_AUTOSTART_SERVER_PORT);
    }

    public String getHosts() {
        return mPreferences.get(KEY_HOSTS, DEFAULT_HOSTS);
    }

    public String getLookAndFeel() {
        return mPreferences.get(KEY_LOOK_AND_FEEL, DEFAULT_LOOK_AND_FEEL);
    }

    public Preferences getPreferences() {
        return mPreferences;
    }

    public boolean isAutostartServer() {
        return mPreferences.getBoolean(KEY_AUTOSTART_SERVER, DEFAULT_AUTOSTART_SERVER);
    }

    public boolean isCustomColors() {
        return mPreferences.getBoolean(KEY_CUSTOM_COLORS, DEFAULT_CUSTOM_COLORS);
    }

    public boolean isDisplayMenuIcons() {
        return mPreferences.getBoolean(KEY_MENU_ICONS, DEFAULT_MENU_ICONS);
    }

    public boolean isForceLookAndFeel() {
        return mPreferences.getBoolean(KEY_FORCE_LOOK_AND_FEEL, DEFAULT_FORCE_LOOK_AND_FEEL);
    }

    public void setAutostartServer(boolean value) {
        mPreferences.putBoolean(KEY_AUTOSTART_SERVER, value);
    }

    public void setAutostartServerConnectDelay(int value) {
        mPreferences.putInt(KEY_AUTOSTART_SERVER_CONNECT_DELAY, value);
    }

    public void setAutostartServerPort(int value) {
        mPreferences.putInt(KEY_AUTOSTART_SERVER_PORT, value);
    }

    public void setCustomColors(boolean value) {
        mPreferences.putBoolean(KEY_CUSTOM_COLORS, value);
    }

    public void setDisplayMenuIcons(boolean value) {
        mPreferences.putBoolean(KEY_MENU_ICONS, value);
    }

    public void setForceLookAndFeel(boolean value) {
        mPreferences.putBoolean(KEY_FORCE_LOOK_AND_FEEL, value);
    }

    public void setHosts(String value) {
        mPreferences.put(KEY_HOSTS, value);
    }

    public void setLookAndFeel(String value) {
        mPreferences.put(KEY_LOOK_AND_FEEL, value);
    }

    public enum ClientOptionsEvent {

        LOOK_AND_FEEL,
        MENU_ICONS;
    }
}
