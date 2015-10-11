/* 
 * Copyright 2015 Patrik Karlsson.
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
package se.trixon.jotaclient.ui.speeddial;

import java.awt.Dimension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import se.trixon.jota.job.Job;
import se.trixon.jota.job.JobManager;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class SpeedDialButton extends JButton {

    private int mIndex;
    private Job mJob;
    private long mJobId;

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
        String template;
        template = "<html><center><h2><b>%s</b></h2></center>%s<br />%s</html>";
        template = "<html><center><h2><b>%s</b></h2></center><p>%s</p><p>%s</p></html>";
        template = "<html><center><h2><b>%s</b></h2>%s<br />%s</center></html>";
        mJobId = jobId;
        mJob = JobManager.INSTANCE.getJobById(jobId);
        String text = "";

        if (mJob != null) {
            long lastRun = mJob.getLastRun();
            String lastRunText = "-";

            if (lastRun > 0) {
                Date date = new Date(lastRun);
                DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
//            dateTime = formatter.format(date);
                lastRunText = new SimpleDateFormat("yyyy-MM-dd HH.mm").format(date);
            }

            String name = mJob.getName();
            String desc = mJob.getDescription();
            text = String.format(template, name, desc, lastRunText);
        }

        setText(text);
        setEnabled(mJob != null);
    }

    private void init() {
        setMinimumSize(new Dimension(220, 128));
        setPreferredSize(new Dimension(220, 128));
        //setHorizontalAlignment(SwingConstants.LEADING);
    }

}
