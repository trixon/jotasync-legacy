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
package se.trixon.jota.shared;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Patrik Karlström
 */
public abstract class JotaBase {

    @SerializedName(value = "description")
    protected String mDescription = "";
    protected String mHistory = "";
    @SerializedName(value = "id")
    protected long mId = System.currentTimeMillis();
    @SerializedName(value = "name")
    protected String mName = "";
    @SerializedName(value = "note")
    protected String mNote = "";

    protected transient StringBuilder mSummaryBuilder;

    public String getDescription() {
        return mDescription;
    }

    public String getHistory() {
        return mHistory;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getNote() {
        return mNote;
    }

    public void setDescription(String comment) {
        mDescription = comment;
    }

    public void setHistory(String history) {
        mHistory = history == null ? "" : history;
    }

    public void setId(long id) {
        mId = id;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setNote(String string) {
        mNote = string;
    }

    protected void addOptionalToSummary(boolean active, String command, String header) {
        if (active) {
            mSummaryBuilder.append(String.format("<p><b>%s</b><br /><i>%s</i></p>", header, command));
        }
    }

}
