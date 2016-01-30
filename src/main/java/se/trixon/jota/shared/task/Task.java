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
package se.trixon.jota.shared.task;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlsson
 */
public class Task implements Comparable<Task>, Serializable {

    private final List<String> mCommand = new ArrayList<>();
    private String mDescription = "";
    private String mDestination;
    private String mDetails = "";
    private boolean mDryRun = true;
    private String mEnvironment = "";
    private final ExcludeSection mExcludeSection;
    private final TaskExecuteSection mExecuteSection;
    private String mHistory = "";
    private long mId = System.currentTimeMillis();
    private String mName = "";
    private final OptionSection mOptionSection;
    private String mSource;
    private int mType = 0;

    public Task() {
        mExecuteSection = new TaskExecuteSection();
        mExcludeSection = new ExcludeSection();
        mOptionSection = new OptionSection();

        mSource = new File(FileUtils.getTempDirectory(), "jotasync-source").getAbsolutePath();
        mDestination = new File(FileUtils.getTempDirectory(), "jotasync-dest").getAbsolutePath();
    }

    @Override
    public int compareTo(Task o) {
        return mName.compareTo(o.getName());
    }

    public List<String> getCommand() {
        mCommand.clear();

        if (mDryRun) {
            //add("--dry-run");
        }

        if (!StringUtils.isBlank(StringUtils.join(mOptionSection.getCommand(), ""))) {
            mCommand.addAll(mOptionSection.getCommand());
        }

        if (!StringUtils.isBlank(StringUtils.join(mExcludeSection.getCommand(), ""))) {
            mCommand.addAll(mExcludeSection.getCommand());
        }

        add(mSource);
        add(mDestination);

        return mCommand;
    }

    public String getCommandAsString() {
        return StringUtils.join(getCommand(), " ");
    }

    public String getDescription() {
        return mDescription;
    }

    public String getDestination() {
        return mDestination;
    }

    public String getDetails() {
        return mDetails;
    }

    public String getEnvironment() {
        return mEnvironment;
    }

    public ExcludeSection getExcludeSection() {
        return mExcludeSection;
    }

    public TaskExecuteSection getExecuteSection() {
        return mExecuteSection;
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

    public OptionSection getOptionSection() {
        return mOptionSection;
    }

    public String getSource() {
        return mSource;
    }

    public int getType() {
        return mType;
    }

    public boolean isDryRun() {
        return mDryRun;
    }

    public boolean isValid() {
        return !getName().isEmpty();
    }

    public void setDescription(String comment) {
        mDescription = comment;
    }

    public void setDestination(String destination) {
        mDestination = destination;
    }

    public void setDetails(String string) {
        mDetails = string;
    }

    public void setDryRun(boolean dryRun) {
        mDryRun = dryRun;
    }

    public void setEnvironment(String environment) {
        mEnvironment = environment;
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

    public void setSource(String source) {
        mSource = source;
    }

    public void setType(int value) {
        mType = value;
    }

    @Override
    public String toString() {
        String description = "";

        try {
            description = mDescription.split("\\n")[0];
        } catch (Exception e) {
            // nvm
        }

        return String.format("<html><b>%s</b><br /><i>%s</i></html>", mName, description);
    }

    private void add(String command) {
        if (!mCommand.contains(command)) {
            mCommand.add(command);
        }
    }
}
