/* 
 * Copyright 2017 Patrik Karlsson.
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
package se.trixon.jota.shared.task;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

/**
 *
 * @author Patrik Karlsson
 */
public class ExcludeSection extends TaskSection {

    public static final String KEY = "exclude";
    private static final String KEY_MANUAL_FILE_PATH = "manualFilePath";
    private static final String KEY_MANUAL_FILE_USED = "manualFileUsed";
    private static final String KEY_OPTIONS = "options";

    private String mManualFilePath;
    private boolean mManualFileUsed;
    private String mOptions = "";

    @Override
    public List<String> getCommand() {
        mCommand.clear();

        for (String option : mOptions.split(" ")) {
            for (String option2 : option.split(OPT_SEPARATOR)) {
                if (StringUtils.isNotBlank(option2)) {
                    add(option2);
                }
            }
        }

        if (mManualFileUsed && StringUtils.isNotBlank(mManualFilePath)) {
            add("--exclude-from=" + mManualFilePath);
        }

        return mCommand;
    }

    @Override
    public JSONObject getJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(KEY_MANUAL_FILE_USED, mManualFileUsed);
        jsonObject.put(KEY_MANUAL_FILE_PATH, mManualFilePath);
        jsonObject.put(KEY_OPTIONS, mOptions);

        return jsonObject;
    }

    public String getManualFilePath() {
        return mManualFilePath;
    }

    public String getOptions() {
        return mOptions;
    }

    public boolean isManualFileUsed() {
        return mManualFileUsed;
    }

    @Override
    public void loadFromJson(JSONObject jsonObject) {
        mManualFileUsed = optBoolean(jsonObject, KEY_MANUAL_FILE_USED);
        mManualFilePath = optString(jsonObject, KEY_MANUAL_FILE_PATH);
        mOptions = optString(jsonObject, KEY_OPTIONS);
    }

    public void setManualFilePath(String value) {
        mManualFilePath = value;
    }

    public void setManualFileUsed(boolean value) {
        mManualFileUsed = value;
    }

    public void setOptions(String value) {
        mOptions = value;
    }
}
