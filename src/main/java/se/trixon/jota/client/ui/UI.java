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
package se.trixon.jota.client.ui;

import java.util.prefs.PreferenceChangeEvent;
import se.trixon.jota.client.ClientOptions;
import se.trixon.almond.util.icons.IconColor;

/**
 *
 * @author Patrik Karlsson
 */
public class UI {

    private IconColor mIconColor = ClientOptions.INSTANCE.getIconTheme() == 0 ? IconColor.BLACK : IconColor.WHITE;

    public static final int ICON_SIZE_LARGE = 24;
    public static final int ICON_SIZE_SMALL = 16;

    public static UI getInstance() {
        return UIHolder.INSTANCE;
    }

    private UI() {
        ClientOptions.INSTANCE.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            String key = evt.getKey();
            if (key.equalsIgnoreCase(ClientOptions.KEY_ICON_THEME)) {
                mIconColor = ClientOptions.INSTANCE.getIconTheme() == 0 ? IconColor.BLACK : IconColor.WHITE;
            }
        });
    }

    public IconColor getIconColor() {
        return mIconColor;
    }

    private static class UIHolder {

        private static final UI INSTANCE = new UI();
    }
}
