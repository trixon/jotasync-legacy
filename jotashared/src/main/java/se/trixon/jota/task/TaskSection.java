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
package se.trixon.jota.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import se.trixon.jota.JsonHelper;

/**
 *
 * @author Patrik Karlsson
 */
public abstract class TaskSection implements Serializable {

    protected final List<String> mCommand = new ArrayList<>();

    public abstract List<String> getCommand();

    public abstract JSONObject getJson();

    public abstract void loadFromJson(JSONObject jsonObject);

    public boolean optBoolean(JSONObject jsonObject, String key) {
        return JsonHelper.optBoolean(jsonObject, key);
    }

    public int optInt(JSONObject jsonObject, String key) {
        return JsonHelper.optInt(jsonObject, key);
    }

    public String optString(JSONObject jsonObject, String key) {
        return JsonHelper.optString(jsonObject, key);
    }

    protected void add(String command) {
        if (!mCommand.contains(command)) {
            mCommand.add(command);
        }
    }
}