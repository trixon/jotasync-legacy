/*
 * Copyright 2015 Patrik Karlsson.
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
package se.trixon.jotaserver;

import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class OptionsOLD {

    public static final String KEY_SPEED_DIAL = "speedDial_";

    private final Options mBackend = Options.INSTANCE;
    private HashMap<Integer, Long> mSpeedDials;

    public OptionsOLD() {
        load();
    }

    public long getSpeedDial(int key) {
        return mSpeedDials.getOrDefault(key, new Long(-1));
    }

    public OptionsOLD load() {
        try {
            mSpeedDials = mBackend.getSpeedDials();
            return this;
        } catch (BackingStoreException ex) {
            Logger.getLogger(OptionsOLD.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public void setSpeedDial(int key, long jobId) {
        mSpeedDials.put(key, jobId);
        mBackend.setSpeedDials(mSpeedDials);
    }
}
