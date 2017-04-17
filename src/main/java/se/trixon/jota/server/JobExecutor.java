/*
 * Copyright 2017 Patrik Karlsson.
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
package se.trixon.jota.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.FileHelper;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.Xlog;
import se.trixon.jota.client.ui.editor.module.job.JobExecutePanel;
import se.trixon.jota.client.ui.editor.module.task.TaskExecutePanel;
import se.trixon.jota.shared.Jota;
import se.trixon.jota.shared.ProcessEvent;
import se.trixon.jota.shared.job.Job;
import se.trixon.jota.shared.job.JobExecuteSection;
import se.trixon.jota.shared.task.Task;
import se.trixon.jota.shared.task.TaskExecuteSection;

/**
 *
 * @author Patrik Karlsson
 */
class JobExecutor extends Thread {

    private Process mCurrentProcess;
    private boolean mDryRun;
    private final StringBuffer mErrBuffer;
    private final Job mJob;
    private final ResourceBundle mJobExecBundle;
    private final JotaManager mJotaManager = JotaManager.INSTANCE;
    private long mLastRun;
    private int mNumOfFailedTasks;
    private ServerOptions mOptions = ServerOptions.INSTANCE;
    private final StringBuffer mOutBuffer;
    private final Server mServer;
    private final ResourceBundle mTaskExecBundle;
    private boolean mTaskFailed;

    JobExecutor(Server server, Job job, boolean dryRun) {
        mJob = job;
        mServer = server;
        mDryRun = dryRun;

        mErrBuffer = new StringBuffer();
        mOutBuffer = new StringBuffer();

        mJobExecBundle = SystemHelper.getBundle(JobExecutePanel.class, "Bundle");
        mTaskExecBundle = SystemHelper.getBundle(TaskExecutePanel.class, "Bundle");
    }

    @Override
    public void run() {
        mLastRun = System.currentTimeMillis();
        String dryRunIndicator = "";
        if (mDryRun) {
            dryRunIndicator = String.format(" (%s)", Dict.DRY_RUN.toString());
        }

        appendHistoryFile(getHistoryLine(mJob.getId(), Dict.STARTED.toString(), dryRunIndicator));
        String s = String.format("%s %s: '%s'='%s'", Jota.nowToDateTime(), Dict.START.toString(), Dict.JOB.toString(), mJob.getName());
        mOutBuffer.append(s).append("\n");
        send(ProcessEvent.OUT, s);
        JobExecuteSection jobExecute = mJob.getExecuteSection();

        try {
            String command;
            // run before first task
            command = jobExecute.getBeforeCommand();
            if (jobExecute.isBefore() && StringUtils.isNoneEmpty(command)) {
                run(command, jobExecute.isBeforeHaltOnError(), mJobExecBundle.getString("JobPanel.beforePanel.header"));
            }

            runTasks();

            if (mNumOfFailedTasks == 0) {
                // run after last task - if all ok
                command = jobExecute.getAfterSuccessCommand();
                if (jobExecute.isAfterSuccess() && StringUtils.isNoneEmpty(command)) {
                    run(command, false, mJobExecBundle.getString("JobPanel.afterSuccessPanel.header"));
                }
            } else {
                s = String.format(Dict.TASKS_FAILED.toString(), mNumOfFailedTasks);
                mOutBuffer.append(s).append("\n");
                send(ProcessEvent.OUT, s);

                // run after last task - if any failed
                command = jobExecute.getAfterFailureCommand();
                if (jobExecute.isAfterFailure() && StringUtils.isNoneEmpty(command)) {
                    run(command, false, mJobExecBundle.getString("JobPanel.afterFailurePanel.header"));
                }
            }

            // run after last task
            command = jobExecute.getAfterCommand();
            if (jobExecute.isAfter() && StringUtils.isNoneEmpty(command)) {
                run(command, false, mJobExecBundle.getString("JobPanel.afterPanel.header"));
            }

            Thread.sleep(500);

            appendHistoryFile(getHistoryLine(mJob.getId(), Dict.DONE.toString(), dryRunIndicator));
            s = String.format("%s %s: %s", Jota.nowToDateTime(), Dict.DONE.toString(), Dict.JOB.toString());
            mOutBuffer.append(s).append("\n");
            updateJobStatus(0);
            writelogs();
            send(ProcessEvent.FINISHED, s);
            Xlog.timedOut(String.format(Dict.JOB_FINISHED.toString(), mJob.getName()));
        } catch (InterruptedException ex) {
            mCurrentProcess.destroy();
            appendHistoryFile(getHistoryLine(mJob.getId(), Dict.CANCEL.toString(), dryRunIndicator));
            updateJobStatus(99);
            writelogs();
            mServer.getClientCallbacks().stream().forEach((clientCallback) -> {
                try {
                    clientCallback.onProcessEvent(ProcessEvent.CANCELED, mJob, null, null);
                } catch (RemoteException ex1) {
                    // nvm Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
                }
            });
        } catch (IOException ex) {
            writelogs();
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionFailedException ex) {
            //Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
            //send(ProcessEvent.OUT, "before failed and will not continue");
            appendHistoryFile(getHistoryLine(mJob.getId(), Dict.FAILED.toString(), dryRunIndicator));
            updateJobStatus(1);
            writelogs();
            send(ProcessEvent.FAILED, String.format("\n\n%s", Dict.JOB_FAILED.toString()));
        }

        mServer.getJobExecutors().remove(mJob.getId());
    }

    public void stopJob() {
        mCurrentProcess.destroy();
        interrupt();
    }

    private void appendHistoryFile(String string) {
        try {
            FileUtils.write(mJotaManager.getHistoryFile(), string, Charset.defaultCharset(), true);
        } catch (IOException ex) {
            Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getHistoryLine(long id, String status, String dryRunIndicator) {
        return String.format("%s %d %s%s\n", Jota.nowToDateTime(), id, status, dryRunIndicator);
    }

    private String getRsyncErrorCode(int exitValue) {
        ResourceBundle bundle = SystemHelper.getBundle(getClass(), "ExitValues");
        String key = String.valueOf(exitValue);

        return bundle.containsKey(key) ? bundle.getString(key) : String.format((Dict.SYSTEM_CODE.toString()), key);
    }

    private boolean run(String command, boolean stopOnError, String description) throws IOException, InterruptedException, ExecutionFailedException {
        //String s = String.format("%s %s: '%s'='%s' ('%s'=%s)", Jota.nowToDateTime(), Dict.START.toString(), description, command, Dict.STOP_ON_ERROR.toString(), StringHelper.booleanToYesNo(stopOnError));
        String s = String.format("%s %s: '%s'='%s'", Jota.nowToDateTime(), Dict.START.toString(), description, command);
        mOutBuffer.append(s).append("\n");
        send(ProcessEvent.OUT, s);
        boolean success = false;

        if (new File(command).exists()) {
            ArrayList<String> commandLine = new ArrayList<>();
            commandLine.add(command);
            runProcess(commandLine);

            Thread.sleep(100);

            String status;
            if (mCurrentProcess.exitValue() == 0) {
                status = Dict.DONE.toString();
                success = true;
            } else {
                status = Dict.Dialog.ERROR.toString();
            }
            s = String.format("%s %s: '%s'", Jota.nowToDateTime(), status, description);
            mOutBuffer.append(s).append("\n");
            send(ProcessEvent.OUT, s);

            if (stopOnError && mCurrentProcess.exitValue() != 0) {
                String string = String.format("%s: exitValue=%d", Dict.FAILED.toString(), mCurrentProcess.exitValue());
                throw new ExecutionFailedException(string);
            }
        } else {
            s = String.format("%s: %s", Dict.Dialog.TITLE_FILE_NOT_FOUND.toString(), command);
            if (stopOnError) {
                throw new ExecutionFailedException(s);
            } else {
                mOutBuffer.append(s).append("\n");
                send(ProcessEvent.ERR, s);
            }
        }

        return success;
    }

    private void runProcess(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        mCurrentProcess = processBuilder.start();

        new ProcessLogThread(mCurrentProcess.getInputStream(), ProcessEvent.OUT).start();
        new ProcessLogThread(mCurrentProcess.getErrorStream(), ProcessEvent.ERR).start();

        mCurrentProcess.waitFor();
    }

    private int runRsync(Task task) throws InterruptedException {
        try {
            ArrayList<String> command = new ArrayList<>();
            command.add(mOptions.getRsyncPath());
            if (mDryRun) {
                command.add("--dry-run");
            }
            command.addAll(task.getCommand());
            String s = String.format("%s %s: rsync\n\n%s\n", Jota.nowToDateTime(), Dict.START.toString(), StringUtils.join(command, " "));
            mOutBuffer.append(s).append("\n");
            send(ProcessEvent.OUT, s);

            runProcess(command);

            Thread.sleep(500);
            send(ProcessEvent.OUT, "");
        } catch (IOException ex) {
            Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
            return 9999;
        }

        return mCurrentProcess.exitValue();
    }

    private boolean runTask(Task task) throws InterruptedException {
        StringBuilder taskHistoryBuilder = new StringBuilder();

        String dryRunIndicator = "";
        if (mDryRun || task.isDryRun()) {
            dryRunIndicator = String.format(" (%s)", Dict.DRY_RUN.toString());
        }
        appendHistoryFile(getHistoryLine(task.getId(), Dict.STARTED.toString(), dryRunIndicator));

        String s = String.format("%s %s: %s='%s'", Jota.nowToDateTime(), Dict.START.toString(), Dict.TASK.toString(), task.getName());
        send(ProcessEvent.OUT, s);
        mTaskFailed = false;
        boolean doNextStep = true;
        TaskExecuteSection taskExecute = task.getExecuteSection();
        String command;

        // run before
        command = taskExecute.getBeforeCommand();
        if (taskExecute.isBefore() && StringUtils.isNoneEmpty(command)) {
            doNextStep = runTaskStep(command, taskExecute.isBeforeHaltOnError(), mTaskExecBundle.getString("TaskExecutePanel.beforePanel.header"));
        }

        // run rsync
        if (doNextStep) {
            int exitValue = runRsync(task);
            boolean rsyncSuccess = exitValue == 0;
            s = String.format("%s %s: rsync (%s)", Jota.nowToDateTime(), Dict.DONE.toString(), getRsyncErrorCode(exitValue));
            mOutBuffer.append(s).append("\n");
            send(ProcessEvent.OUT, s);
            if (rsyncSuccess) {
                // run after success
                command = taskExecute.getAfterSuccessCommand();
                if (taskExecute.isAfterSuccess() && StringUtils.isNoneEmpty(command)) {
                    doNextStep = runTaskStep(command, taskExecute.isAfterSuccessHaltOnError(), mTaskExecBundle.getString("TaskExecutePanel.afterSuccessPanel.header"));
                }
            } else {
                // run after failure
                command = taskExecute.getAfterFailureCommand();
                if (taskExecute.isAfterFailure() && StringUtils.isNoneEmpty(command)) {
                    doNextStep = runTaskStep(command, taskExecute.isAfterFailureHaltOnError(), mTaskExecBundle.getString("TaskExecutePanel.afterFailurePanel.header"));
                }
            }
        }

        if (doNextStep) {
            // run after
            command = taskExecute.getAfterCommand();
            if (taskExecute.isAfter() && StringUtils.isNoneEmpty(command)) {
                runTaskStep(command, taskExecute.isAfterHaltOnError(), mTaskExecBundle.getString("TaskExecutePanel.afterPanel.header"));
            }
        }

        if (mTaskFailed) {
            mNumOfFailedTasks++;
        }

        appendHistoryFile(getHistoryLine(task.getId(), Dict.DONE.toString(), dryRunIndicator));
        appendHistoryFile(taskHistoryBuilder.toString());

        s = String.format("%s %s: %s", Jota.nowToDateTime(), Dict.DONE.toString(), Dict.TASK.toString());
        send(ProcessEvent.OUT, s);

        boolean doNextTask = !(mTaskFailed && taskExecute.isJobHaltOnError());
        return doNextTask;
    }

    private boolean runTaskStep(String command, boolean stopOnError, String description) throws InterruptedException {
        boolean doNextStep = false;

        try {
            if (run(command, stopOnError, description)) {
            } else {
                mTaskFailed = true;
            }
            doNextStep = true;
        } catch (IOException | ExecutionFailedException ex) {
            mTaskFailed = true;
            Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return doNextStep;
    }

    private void runTasks() throws InterruptedException {
        for (Task task : mJob.getTasks()) {
            if (!runTask(task)) {
                break;
            }
        }
    }

    private synchronized void send(ProcessEvent processEvent, String line) {
        mServer.getClientCallbacks().stream().forEach((clientCallback) -> {
            try {
                clientCallback.onProcessEvent(processEvent, mJob, null, line);
            } catch (RemoteException ex) {
                // nvm Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void updateJobStatus(int exitCode) {
        mJotaManager.getJobManager().getJobById(mJob.getId()).setLastRun(mLastRun);
        mJotaManager.getJobManager().getJobById(mJob.getId()).setLastRunExitCode(exitCode);

        try {
            mJotaManager.save();
        } catch (IOException ex) {
            Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writelogs() {
        File directory = new File(ServerOptions.INSTANCE.getLogDir());
        String jobName = FileHelper.replaceInvalidChars(mJob.getName());
        String outFile = String.format("%s.log", jobName);
        String errFile = String.format("%s.err", jobName);

        int logMode = mJob.getLogMode();
        if (logMode == 2) {
            outFile = String.format("%s %s.log", jobName, mJob.getLastRunDateTime("", mLastRun));
            errFile = String.format("%s %s.err", jobName, mJob.getLastRunDateTime("", mLastRun));
        }

        boolean append = logMode == 0;

        try {
            FileUtils.forceMkdir(directory);
            File file = new File(directory, outFile);
            send(ProcessEvent.OUT, "");

            StringBuilder builder = new StringBuilder();
            if (mJob.isLogOutput() || mJob.isLogErrors() && !mJob.isLogSeparateErrors()) {
                FileUtils.writeStringToFile(file, mOutBuffer.toString(), append);
                String message = file.getAbsolutePath();
                Xlog.timedOut(message);
                builder.append(String.format("%s:%s", SystemHelper.getHostname(), message));
            }

            if (mJob.isLogErrors() && mJob.isLogSeparateErrors()) {
                if (builder.length() > 0) {
                    builder.append("\n");
                }
                file = new File(directory, errFile);
                FileUtils.writeStringToFile(file, mErrBuffer.toString(), append);
                String message = file.getAbsolutePath();
                Xlog.timedOut(message);
                builder.append(String.format("%s:%s", SystemHelper.getHostname(), message));
            }

            if (builder.length() > 0) {
                builder.insert(0, String.format("%s\n", Dict.SAVE_LOG.toString()));
                send(ProcessEvent.OUT, builder.toString());
            }
        } catch (IOException ex) {
            Xlog.timedErr(ex.getLocalizedMessage());
        }
    }

    class ExecutionFailedException extends Exception {

        public ExecutionFailedException() {
            super();
        }

        public ExecutionFailedException(String message) {
            super(message);
            mOutBuffer.append(message).append("\n");
            send(ProcessEvent.OUT, message);
        }

        public ExecutionFailedException(String message, Throwable cause) {
            super(message, cause);
        }

        public ExecutionFailedException(Throwable cause) {
            super(cause);
        }

        public ExecutionFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    class ProcessLogThread extends Thread {

        private final InputStream mInputStream;
        private final ProcessEvent mProcessEvent;
        private String mDateTimePrefix = "";

        public ProcessLogThread(InputStream inputStream, ProcessEvent processEvent) {
            mInputStream = inputStream;
            mProcessEvent = processEvent;
            if (mJob.getLogMode() == 0) {
                mDateTimePrefix = Jota.millisToDateTime(mLastRun) + " ";
            }
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(mInputStream), 1);
                String line;

                while ((line = reader.readLine()) != null) {
                    String string = String.format("%s%s%s", mDateTimePrefix, line, System.lineSeparator());
                    if (mJob.isLogSeparateErrors()) {
                        if (mJob.isLogOutput() && mProcessEvent == ProcessEvent.OUT) {
                            mOutBuffer.append(string);
                        } else if (mJob.isLogErrors() && mProcessEvent == ProcessEvent.ERR) {
                            mErrBuffer.append(string);
                        }
                    } else if (!mJob.isLogSeparateErrors()) {
                        if (mJob.isLogOutput() && mProcessEvent == ProcessEvent.OUT) {
                            mOutBuffer.append(string);
                        } else if (mJob.isLogErrors() && mProcessEvent == ProcessEvent.ERR) {
                            mOutBuffer.append(string);
                        }
                    }

                    //mOutBuffer.append(line).append("\n");
                    send(mProcessEvent, line);
                }
            } catch (IOException e) {
                Xlog.timedErr(e.getLocalizedMessage());
            }
        }
    }
}
