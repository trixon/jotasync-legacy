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
package se.trixon.jota;

import java.util.Locale;
import org.json.simple.JSONObject;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class JsonHelper {

    public static int getInt(JSONObject object, String key) {
        return ((Long) object.get(key)).intValue();
    }

    public static String getLanguageSuffix() {
        return "-" + Locale.getDefault().getLanguage();
    }

    public static long getLong(JSONObject object, String key) {
        return (Long) object.get(key);
    }

    public static boolean optBoolean(JSONObject object, String key) {
        if (object.containsKey(key)) {
            return (boolean) object.get(key);
        } else {
            return false;
        }
    }

    public static int optInt(JSONObject object, String key) {
        if (object.containsKey(key)) {
            return getInt(object, key);
        } else {
            return 0;
        }
    }

    public static String optString(JSONObject object, String key) {
        if (object.containsKey(key)) {
            return (String) object.get(key);
        } else {
            return "";
        }
    }

    public static String parseLocalizedKey(JSONObject jsonObject, String key) {
        String value = "";
        String localizedKey = key + getLanguageSuffix();

        if (jsonObject.containsKey(localizedKey)) {
            value = (String) jsonObject.get(localizedKey);
        } else if (jsonObject.containsKey(key)) {
            value = (String) jsonObject.get(key);
        }

        return value;
    }
}
