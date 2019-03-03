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
package se.trixon.jota.server;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
enum ServerOptions {

    INSTANCE;
    public static final boolean DEFAULT_CRON_ACTIVE = false;
    public static final String DEFAULT_LOG_DIR = new File(FileUtils.getUserDirectory(), ".config/jotasync/log").getAbsolutePath();
    public static final String DEFAULT_RSYNC_PATH = "rsync";
    public static final String KEY_CRON_ACTIVE = "cron_active";
    public static final String KEY_LOG_DIR = "logDir";
    public static final String KEY_RSYNC_PATH = "rsync";
    public static final String KEY_SELECTED_JOB = "job";
    public static final String KEY_SPEED_DIAL = "speedDial_";
    private final Preferences mPreferences;
    private HashMap<Integer, Long> mSpeedDials;

    private ServerOptions() {
        mPreferences = Preferences.userNodeForPackage(this.getClass());
        try {
            mSpeedDials = getSpeedDials();
        } catch (BackingStoreException ex) {
            Logger.getLogger(ServerOptions.class.getName()).log(Level.SEVERE, null, ex);
            mSpeedDials = new HashMap<>();
        }
    }

    long getJobId() {
        return mPreferences.getLong(KEY_SELECTED_JOB, 0);
    }

    String getLogDir() {
        String dir = mPreferences.get(KEY_LOG_DIR, DEFAULT_LOG_DIR);
        if (StringUtils.isBlank(dir)) {
            dir = DEFAULT_LOG_DIR;
        }

        return dir;
    }

    Preferences getPreferences() {
        return mPreferences;
    }

    String getRsyncPath() {
        return mPreferences.get(KEY_RSYNC_PATH, DEFAULT_RSYNC_PATH);
    }

    long getSpeedDial(int key) {
        return mSpeedDials.getOrDefault(key, new Long(-1));
    }

    HashMap<Integer, Long> getSpeedDials() throws BackingStoreException {
        HashMap<Integer, Long> speedDials = new HashMap<>();
        for (String key : mPreferences.keys()) {
            try {
                if (key.startsWith(KEY_SPEED_DIAL)) {
                    String[] index = key.split(KEY_SPEED_DIAL);
                    long value = mPreferences.getLong(key, -1);
                    speedDials.put(Integer.valueOf(index[1]), value);
                }
            } catch (NumberFormatException ex) {
                //Logger.getLogger(ServerOptions.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        //System.out.println("SpeedDials loaded: " + speedDials.size());
        //speedDials.keySet().stream().forEach((key) -> {
        //    System.out.println("loaded speedDial: " + key + " " + speedDials.get(key));
        //});
        return speedDials;
    }

    boolean isCronActive() {
        return mPreferences.getBoolean(KEY_CRON_ACTIVE, DEFAULT_CRON_ACTIVE);
    }

    void setCronActive(boolean value) {
        mPreferences.putBoolean(KEY_CRON_ACTIVE, value);
    }

    void setJobId(long jobId) {
        mPreferences.putLong(KEY_SELECTED_JOB, jobId);
    }

    void setLogDir(String value) {
        mPreferences.put(KEY_LOG_DIR, value);
    }

    void setRsyncPath(String value) {
        mPreferences.put(KEY_RSYNC_PATH, value);
    }

    void setSpeedDial(int key, long jobId) {
        mSpeedDials.put(key, jobId);
        setSpeedDials(mSpeedDials);
    }

    void setSpeedDials(HashMap<Integer, Long> speedDials) {
        //System.out.println("setSpeedDials():");
        speedDials.keySet().stream().forEach((key) -> {
            //System.out.println("put speedDial: " + key + " " + speedDials.get(key));
            mPreferences.putLong(KEY_SPEED_DIAL + String.valueOf(key), speedDials.get(key));
        });
    }
}
