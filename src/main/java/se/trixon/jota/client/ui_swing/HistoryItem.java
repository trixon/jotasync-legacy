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
package se.trixon.jota.client.ui_swing;

/**
 *
 * @author Patrik Karlström
 */
public class HistoryItem {

    private String mHistory;
    private long mId;
    private String mName;

    public HistoryItem() {
    }

    public HistoryItem(long id, String name, String history) {
        mId = id;
        mName = name;
        mHistory = history;
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

    public void setHistory(String history) {
        mHistory = history;
    }

    public void setId(long id) {
        mId = id;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }
}
