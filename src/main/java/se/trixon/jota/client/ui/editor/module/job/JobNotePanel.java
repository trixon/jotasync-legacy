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
package se.trixon.jota.client.ui.editor.module.job;

import se.trixon.jota.client.ui.editor.module.JobPersistor;
import se.trixon.jota.client.ui.editor.module.common.NotePanel;
import se.trixon.jota.shared.job.Job;

/**
 *
 * @author Patrik Karlström
 */
public class JobNotePanel extends NotePanel implements JobPersistor {

    @Override
    public void loadJob(Job job) {
        setText(job.getNote());
    }

    @Override
    public Job saveJob(Job job) {
        job.setNote(getText());

        return job;
    }
}
