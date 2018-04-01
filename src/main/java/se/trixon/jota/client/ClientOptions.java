/*
 * Copyright 2018 Patrik Karlström.
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
 * @author Patrik Karlström
 */
public enum ClientOptions {

    INSTANCE;
    public static final boolean DEFAULT_CUSTOM_COLORS = false;
    public static final String KEY_AUTOSTART_SERVER = "autostartServer";
    public static final String KEY_AUTOSTART_SERVER_CONNECT_DELAY = "autostartServerConnectDelay";
    public static final String KEY_AUTOSTART_SERVER_PORT = "autostartServerPort";
    public static final String KEY_CUSTOM_COLORS = "customColors";
    public static final String KEY_DISPLAY_DRY_RUN = "display_dry_run";
    public static final String KEY_HOSTS = "hosts";
    public static final String KEY_SPLIT_DELETIONS = "split_deletions";
    public static final String KEY_SPLIT_ERRORS = "split_errors";
    public static final String KEY_WORD_WRAP = "word_wrap";
    private static final boolean DEFAULT_AUTOSTART_SERVER = true;
    private static final int DEFAULT_AUTOSTART_SERVER_CONNECT_DELAY = 500;
    private static final int DEFAULT_AUTOSTART_SERVER_PORT = Jota.DEFAULT_PORT_HOST;
    private static final boolean DEFAULT_DISPLAY_DRY_RUN = false;
    private static final String DEFAULT_HOSTS = "localhost";
    private static final boolean DEFAULT_SPLIT_DELETIONS = true;
    private static final boolean DEFAULT_SPLIT_ERRORS = true;
    private static final boolean DEFAULT_WORD_WRAP = false;

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

    public Preferences getPreferences() {
        return mPreferences;
    }

    public boolean isAutostartServer() {
        return mPreferences.getBoolean(KEY_AUTOSTART_SERVER, DEFAULT_AUTOSTART_SERVER);
    }

    public boolean isCustomColors() {
        return mPreferences.getBoolean(KEY_CUSTOM_COLORS, DEFAULT_CUSTOM_COLORS);
    }

    public boolean isDisplayDryRun() {
        return mPreferences.getBoolean(KEY_DISPLAY_DRY_RUN, DEFAULT_DISPLAY_DRY_RUN);
    }

    public boolean isSplitDeletions() {
        return mPreferences.getBoolean(KEY_SPLIT_DELETIONS, DEFAULT_SPLIT_DELETIONS);
    }

    public boolean isSplitErrors() {
        return mPreferences.getBoolean(KEY_SPLIT_ERRORS, DEFAULT_SPLIT_ERRORS);
    }

    public boolean isWordWrap() {
        return mPreferences.getBoolean(KEY_WORD_WRAP, DEFAULT_WORD_WRAP);
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

    public void setDisplayDryRun(boolean value) {
        mPreferences.putBoolean(KEY_DISPLAY_DRY_RUN, value);
    }

    public void setHosts(String value) {
        mPreferences.put(KEY_HOSTS, value);
    }

    public void setSplitDeletions(boolean value) {
        mPreferences.putBoolean(KEY_SPLIT_DELETIONS, value);
    }

    public void setSplitErrors(boolean value) {
        mPreferences.putBoolean(KEY_SPLIT_ERRORS, value);
    }

    public void setWordWrap(boolean value) {
        mPreferences.putBoolean(KEY_WORD_WRAP, value);
    }
}
