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
package se.trixon.jota.shared.task;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
public class ExcludeSection extends TaskSection implements Serializable {

    @SerializedName("manual_file_path")
    private String mManualFilePath;
    @SerializedName("manual_file_used")
    private boolean mManualFileUsed;
    @SerializedName("options")
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

    public String getManualFilePath() {
        return mManualFilePath;
    }

    public String getOptions() {
        return mOptions;
    }

    public boolean isManualFileUsed() {
        return mManualFileUsed;
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
