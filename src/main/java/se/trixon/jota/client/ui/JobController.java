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
package se.trixon.jota.client.ui;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.model.WorkbenchDialog;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import se.trixon.almond.util.Dict;
import se.trixon.jota.client.Manager;
import se.trixon.jota.server.JobValidator;
import se.trixon.jota.shared.ProcessEvent;
import se.trixon.jota.shared.ServerEvent;
import se.trixon.jota.shared.ServerEventListener;
import se.trixon.jota.shared.job.Job;
import se.trixon.jota.shared.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class JobController implements ServerEventListener {

    private final Logger LOGGER = Logger.getLogger(getClass().getName());
    private final HashMap<Long, JobModule> mJobMap = new HashMap<>();
    private final Manager mManager = Manager.getInstance();
    private Workbench mWorkbench;

    public static JobController getInstance() {
        return Holder.INSTANCE;
    }

    private JobController() {
    }

    @Override
    public void onProcessEvent(ProcessEvent processEvent, Job job, Task task, Object object) {
        Platform.runLater(() -> {
            JobModule jobModule = mJobMap.computeIfAbsent(job.getId(), k -> {
                JobModule module = new JobModule(mWorkbench.getScene(), job);
                mWorkbench.getModules().add(module);
                mWorkbench.openModule(module);

                return module;
            });

            switch (processEvent) {
                case STARTED:
                    jobModule.start();
                    mWorkbench.openModule(jobModule);
//                    updateTitle(job, "b");
//                    updateActionStates();
                    break;
                case OUT:
                case ERR:
                    jobModule.log(processEvent, (String) object);
                    break;
                case CANCELED:
                    jobModule.log(ProcessEvent.OUT, String.format("\n\n%s", Dict.JOB_INTERRUPTED.toString()));
                    jobModule.enableSave();
//                    updateTitle(job, "i");
//                    updateActionStates();
                    break;
                case FAILED:
                    jobModule.log(ProcessEvent.OUT, String.format("\n\n%s", Dict.JOB_FAILED.toString()));
                    jobModule.enableSave();
//                    updateTitle(job, "strike");
//                    updateActionStates();
                    break;
                case FINISHED:
                    if (object != null) {
                        jobModule.log(ProcessEvent.OUT, (String) object);
                    }
                    jobModule.enableSave();
//                    updateTitle(job, "normal");
//                    updateActionStates();
                    break;
            }
        });
    }

    @Override
    public void onServerEvent(ServerEvent serverEvent) {
        //nvm
    }

    public void run(Job job) {
        try {
            JobValidator validator = mManager.getServerCommander().validate(job);

            if (validator.isValid()) {
                HtmlPane previewPanel = new HtmlPane(job.getSummaryAsHtml());

                ButtonType runButtonType = new ButtonType(Dict.RUN.toString());
                ButtonType dryRunButtonType = new ButtonType(Dict.DRY_RUN.toString(), ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelButtonType = new ButtonType(Dict.CANCEL.toString(), ButtonBar.ButtonData.CANCEL_CLOSE);

                String title = String.format(Dict.Dialog.TITLE_PROFILE_RUN.toString(), job.getName());

                WorkbenchDialog dialog = WorkbenchDialog.builder(title, previewPanel, runButtonType, dryRunButtonType, cancelButtonType).onResult(buttonType -> {
                    try {
                        if (buttonType != cancelButtonType) {
                            boolean dryRun = buttonType == dryRunButtonType;
                            mManager.getServerCommander().startJob(job, dryRun);
                        }
                    } catch (RemoteException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }).build();
                mWorkbench.showDialog(dialog);
            } else {
                WorkbenchDialog dialog = WorkbenchDialog.builder(
                        Dict.Dialog.ERROR_VALIDATION.toString(),
                        new HtmlPane(validator.getSummaryAsHtml()),
                        WorkbenchDialog.Type.ERROR).build();

                mWorkbench.showDialog(dialog);
            }
        } catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public void setWorkbench(Workbench workbench) {
        mWorkbench = workbench;
    }

    private static class Holder {

        private static final JobController INSTANCE = new JobController();
    }

}
