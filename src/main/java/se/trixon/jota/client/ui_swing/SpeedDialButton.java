/*
 * Copyright 2019 Patrik Karlström.
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

import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import se.trixon.jota.client.ClientOptions;
import se.trixon.jota.client.Manager;
import se.trixon.jota.client.ui_swing.editor.JobsPanel;
import se.trixon.jota.shared.job.Job;

/**
 *
 * @author Patrik Karlström
 */
public class SpeedDialButton extends JButton {

    private int mIndex;
    private Job mJob;
    private long mJobId;
    private final Manager mManager = Manager.getInstance();

    public SpeedDialButton() {
        init();
    }

    public SpeedDialButton(Icon icon) {
        super(icon);
        init();
    }

    public SpeedDialButton(String text) {
        super(text);
        init();
    }

    public SpeedDialButton(Action a) {
        super(a);
        init();
    }

    public SpeedDialButton(String text, Icon icon) {
        super(text, icon);
        init();
    }

    public int getIndex() {
        return mIndex;
    }

    public Job getJob() {
        return mJob;
    }

    public long getJobId() {
        return mJobId;
    }

    @Override
    public void setEnabled(boolean b) {
        if (b) {
            super.setEnabled(b && mJobId > -    1);
        } else {
            super.setEnabled(b);
        }
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public void setJob(Job job) {
        mJob = job;
    }

    public void setJobId(long jobId) {
        mJobId = jobId;
        try {
            mJob = mManager.getServerCommander().getJob(jobId);
        } catch (RemoteException ex) {
            Logger.getLogger(JobsPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            mJob = null;
        }

        updateText();
        updateColor();
        setEnabled(mJob != null);
    }

    private void init() {
        setMinimumSize(new Dimension(210, 128));
        setPreferredSize(new Dimension(210, 128));
        setFont(getFont().deriveFont(getFont().getStyle() & ~java.awt.Font.BOLD));
    }

    void updateColor() {
        if (mJob == null || !ClientOptions.INSTANCE.isCustomColors()) {
            setBackground(null);
            setForeground(null);
        } else {
            try {
                Job job = mManager.getServerCommander().getJob(mJobId);
                //if (StringUtils.isNotBlank(job.getColorBackground())) {
                //    setBackground(Color.decode(job.getColorBackground()));
                //}
            } catch (RemoteException | NumberFormatException ex) {
                setBackground(null);
                setForeground(null);
            }
        }
    }

    void updateText() {
        String text = "";

        if (mJob == null) {
            setText(text);
            return;
        }

        try {
            Job job = mManager.getServerCommander().getJob(mJobId);
            setText(job.getCaption(true));
        } catch (RemoteException ex) {
            setText(text);
        }
    }
}
