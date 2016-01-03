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

import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author Patrik Karlsson
 */
public class ExcludeSection extends TaskSection {

    public static final String KEY = "exclude";
    public static final String KEY_TEMPLATE_BACKUP = "templateBackup";
    public static final String KEY_TEMPLATE_CACHE = "templateCahce";
    public static final String KEY_TEMPLATE_GVFS = "templateGvfs";
    public static final String KEY_TEMPLATE_LOST_FOUND = "templateLostFound";
    public static final String KEY_TEMPLATE_SYSTEM_DIRS = "templateSystemDirs";
    public static final String KEY_TEMPLATE_SYSTEM_MOUNT_DIRS = "templateSystemMountDirs";
    public static final String KEY_TEMPLATE_TEMP = "templateTemp";
    public static final String KEY_TEMPLATE_TRASH = "templateTrash";

    private boolean mTemplateBackup;
    private boolean mTemplateCache;
    private boolean mTemplateGvfs;
    private boolean mTemplateLostFound;
    private boolean mTemplateSystemDirs;
    private boolean mTemplateSystemMountDirs;
    private boolean mTemplateTemp;
    private boolean mTemplateTrash;

    @Override
    public List<String> getCommand() {
        mCommand.clear();

        if (mTemplateBackup) {
            add("--exclude=**~");
        }

        if (mTemplateCache) {
            add("--exclude=**/*cache*/");
            add("--exclude=**/*Cache*/");
        }

        if (mTemplateGvfs) {
            add("--exclude=**/.gvfs/");
        }

        if (mTemplateLostFound) {
            add("--exclude=**/lost+found*/");
        }

        if (mTemplateSystemDirs) {
            add("--exclude=/var/**");
            add("--exclude=/proc/**");
            add("--exclude=/dev/**");
            add("--exclude=/sys/**");
        }

        if (mTemplateSystemMountDirs) {
            add("--exclude=/mnt/*/**");
            add("--exclude=/media/*/**");
        }

        if (mTemplateTemp) {
            add("--exclude=**/*tmp*/");
        }

        if (mTemplateTrash) {
            add("--exclude=**/*Trash*/");
            add("--exclude=**/*trash*/");
        }

        return mCommand;
    }

    @Override
    public JSONObject getJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(KEY_TEMPLATE_BACKUP, mTemplateBackup);
        jsonObject.put(KEY_TEMPLATE_CACHE, mTemplateCache);
        jsonObject.put(KEY_TEMPLATE_GVFS, mTemplateGvfs);
        jsonObject.put(KEY_TEMPLATE_LOST_FOUND, mTemplateLostFound);
        jsonObject.put(KEY_TEMPLATE_SYSTEM_DIRS, mTemplateSystemDirs);
        jsonObject.put(KEY_TEMPLATE_SYSTEM_MOUNT_DIRS, mTemplateSystemMountDirs);
        jsonObject.put(KEY_TEMPLATE_TEMP, mTemplateTemp);
        jsonObject.put(KEY_TEMPLATE_TRASH, mTemplateTrash);

        return jsonObject;
    }

    public boolean isTemplateBackup() {
        return mTemplateBackup;
    }

    public boolean isTemplateCache() {
        return mTemplateCache;
    }

    public boolean isTemplateGvfs() {
        return mTemplateGvfs;
    }

    public boolean isTemplateLostFound() {
        return mTemplateLostFound;
    }

    public boolean isTemplateSystemDirs() {
        return mTemplateSystemDirs;
    }

    public boolean isTemplateSystemMountDirs() {
        return mTemplateSystemMountDirs;
    }

    public boolean isTemplateTemp() {
        return mTemplateTemp;
    }

    public boolean isTemplateTrash() {
        return mTemplateTrash;
    }

    @Override
    public void loadFromJson(JSONObject jsonObject) {
        mTemplateBackup = optBoolean(jsonObject, KEY_TEMPLATE_BACKUP);
        mTemplateCache = optBoolean(jsonObject, KEY_TEMPLATE_CACHE);
        mTemplateGvfs = optBoolean(jsonObject, KEY_TEMPLATE_GVFS);
        mTemplateLostFound = optBoolean(jsonObject, KEY_TEMPLATE_LOST_FOUND);
        mTemplateSystemDirs = optBoolean(jsonObject, KEY_TEMPLATE_SYSTEM_DIRS);
        mTemplateSystemMountDirs = optBoolean(jsonObject, KEY_TEMPLATE_SYSTEM_MOUNT_DIRS);
        mTemplateTemp = optBoolean(jsonObject, KEY_TEMPLATE_TEMP);
        mTemplateTrash = optBoolean(jsonObject, KEY_TEMPLATE_TRASH);
    }

    public void setTemplateBackup(boolean value) {
        mTemplateBackup = value;
    }

    public void setTemplateCache(boolean value) {
        mTemplateCache = value;
    }

    public void setTemplateGvfs(boolean value) {
        mTemplateGvfs = value;
    }

    public void setTemplateLostFound(boolean value) {
        mTemplateLostFound = value;
    }

    public void setTemplateSystemDirs(boolean value) {
        mTemplateSystemDirs = value;
    }

    public void setTemplateSystemMountDirs(boolean value) {
        mTemplateSystemMountDirs = value;
    }

    public void setTemplateTemp(boolean value) {
        mTemplateTemp = value;
    }

    public void setTemplateTrash(boolean value) {
        mTemplateTrash = value;
    }
}
