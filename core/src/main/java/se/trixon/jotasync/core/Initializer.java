/*
 * Copyright 2020 Patrik Karlström.
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
package se.trixon.jotasync.core;

import java.rmi.RemoteException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import se.trixon.almond.nbp.NbLog;
import se.trixon.almond.util.PrefsHelper;
import se.trixon.almond.util.SystemHelper;
import se.trixon.jotasync.core.api.Jota;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class Initializer implements Runnable {

    @Override
    public void run() {
        try {
            final String key = "laf";
            final String defaultLAF = "com.formdev.flatlaf.FlatDarkLaf";
            Preferences preferences = NbPreferences.root().node("laf");
            PrefsHelper.putIfAbsent(preferences, key, defaultLAF);
        } catch (BackingStoreException ex) {
            //Exceptions.printStackTrace(ex);
        }

        System.setProperty("netbeans.winsys.no_help_in_dialogs", "true");
        System.setProperty("netbeans.winsys.no_toolbars", "true");

        Jota.getLog().setUseTimestamps(false);
        NbLog.setUseGlobalTag(false);
        Jota.getLog().setOut(string -> {
            NbLog.i("", string);
        });
        Jota.getLog().setErr(string -> {
            NbLog.e("", string);
        });
        Jota.log(SystemHelper.getSystemInfo());

        try {
            se.trixon.jota.client.Main.main(null);
        } catch (RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
