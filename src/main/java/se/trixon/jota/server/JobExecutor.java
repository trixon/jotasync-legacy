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
package se.trixon.jota.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import se.trixon.jota.shared.Jota;
import se.trixon.jota.shared.ProcessEvent;
import se.trixon.jota.shared.job.Job;
import se.trixon.jota.shared.job.JobExecuteSection;
import se.trixon.jota.shared.task.Task;
import se.trixon.jota.shared.task.TaskExecuteSection;
import se.trixon.util.SystemHelper;
import se.trixon.util.Xlog;

/**
 *
 * @author Patrik Karlsson
 */
class JobExecutor extends Thread {

    private Process mCurrentProcess;
    private final StringBuffer mErrBuffer;
    private StringBuilder mHistoryBuilder;
    private final Job mJob;
    private final JotaManager mJotaManager = JotaManager.INSTANCE;
    private long mLastRun;
    private int mNumOfFailedTasks;
    private ServerOptions mOptions = ServerOptions.INSTANCE;
    private final StringBuffer mOutBuffer;
    private final Server mServer;
    private boolean mTaskFailed;
    private boolean mDryRun;

    JobExecutor(Server server, Job job, boolean dryRun) {
        mJob = job;
        mServer = server;
        mDryRun = dryRun;

        mErrBuffer = new StringBuffer();
        mOutBuffer = new StringBuffer();
    }

    @Override
    public void run() {
        mLastRun = System.currentTimeMillis();
        mHistoryBuilder = new StringBuilder();
        mHistoryBuilder.append(String.format("%s started", Jota.millisToDateTime(System.currentTimeMillis()))).append("\n");
        send(ProcessEvent.OUT, String.format("%s: Starting job %s", Jota.millisToDateTime(mLastRun), mJob.getName()));
        JobExecuteSection jobExecute = mJob.getExecuteSection();

        try {
            String command;
            // run before first task
            command = jobExecute.getBeforeCommand();
            if (jobExecute.isBefore() && StringUtils.isNoneEmpty(command)) {
                run(command, jobExecute.isBeforeHaltOnError(), "BEFORE FIRST TASK");
            }

            runTasks();

            if (mNumOfFailedTasks == 0) {
                // run after last task - if all ok
                command = jobExecute.getAfterSuccessCommand();
                if (jobExecute.isAfterSuccess() && StringUtils.isNoneEmpty(command)) {
                    run(command, false, "AFTER LAST TASK (ALL OK)");
                }
            } else {
                send(ProcessEvent.OUT, String.format("%d tasks failed", mNumOfFailedTasks));

                // run after last task - if any failed
                command = jobExecute.getAfterFailureCommand();
                if (jobExecute.isAfterFailure() && StringUtils.isNoneEmpty(command)) {
                    run(command, false, "AFTER LAST TASK (ANY FAILED)");
                }
            }

            // run after last task
            command = jobExecute.getAfterCommand();
            if (jobExecute.isAfter() && StringUtils.isNoneEmpty(command)) {
                run(command, false, "AFTER LAST TASK");
            }
            Thread.sleep(500);
            mHistoryBuilder.append(String.format("%s finished", Jota.millisToDateTime(System.currentTimeMillis()))).append("\n");
            updateJobStatus(0);
            writelogs();
            send(ProcessEvent.FINISHED, "Job finished");
            Xlog.timedOut(String.format("Job finished: %s", mJob.getName()));
        } catch (InterruptedException ex) {
            mCurrentProcess.destroy();
            mHistoryBuilder.append(String.format("%s aborted", Jota.millisToDateTime(System.currentTimeMillis()))).append("\n");
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
            mHistoryBuilder.append(String.format("%s failed", Jota.millisToDateTime(System.currentTimeMillis()))).append("\n");
            updateJobStatus(1);
            writelogs();
            send(ProcessEvent.FAILED, "\n\nJob failed");
        }

        mServer.getJobExecutors().remove(mJob.getId());
    }

    public void stopJob() {
        mCurrentProcess.destroy();
        interrupt();
    }

    private boolean run(String command, boolean stopOnError, String description) throws IOException, InterruptedException, ExecutionFailedException {
        send(ProcessEvent.OUT, String.format("Run %s: %s (stopOnError=%b)", description, command, stopOnError));
        boolean success = false;

        if (new File(command).exists()) {
            ArrayList<String> commandLine = new ArrayList<>();
            commandLine.add(command);
            runProcess(commandLine);

            Thread.sleep(100);

            if (mCurrentProcess.exitValue() == 0) {
                send(ProcessEvent.OUT, String.format("%s OK", description));
                success = true;
            } else {
                send(ProcessEvent.OUT, String.format("%s FAILED", description));
            }

            send(ProcessEvent.OUT, "");
            if (stopOnError && mCurrentProcess.exitValue() != 0) {
                throw new ExecutionFailedException("FAILED: Will not continue. exitValue=" + mCurrentProcess.exitValue());
            }
        } else {
            String fileNotExists = String.format("File does not exist: %s", command);
            if (stopOnError) {
                throw new ExecutionFailedException(fileNotExists);
            } else {
                send(ProcessEvent.ERR, fileNotExists);
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

    private int runRsync(Task task) {
        try {
            ArrayList<String> command = new ArrayList<>();
            command.add(mOptions.getRsyncPath());
            if (mDryRun) {
                command.add("--dry-run");
            }
            command.addAll(task.getCommand());
            send(ProcessEvent.OUT, StringUtils.join(command, " "));

            runProcess(command);

            Thread.sleep(500);
            send(ProcessEvent.OUT, "");
        } catch (IOException | InterruptedException ex) {
            //Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
            return 9999;
        }

        return mCurrentProcess.exitValue();
    }

    private boolean runTask(Task task) {
        StringBuilder taskHistoryBuilder = new StringBuilder();
        taskHistoryBuilder.append(String.format("%s started", Jota.millisToDateTime(System.currentTimeMillis()))).append("\n");
        send(ProcessEvent.OUT, "Run task: " + task.getName());
        mTaskFailed = false;
        boolean doNextStep = true;
        TaskExecuteSection taskExecute = task.getExecuteSection();
        String command;

        // run before 
        command = taskExecute.getBeforeCommand();
        if (taskExecute.isBefore() && StringUtils.isNoneEmpty(command)) {
            doNextStep = runTaskStep(command, taskExecute.isBeforeHaltOnError(), "BEFORE TASK");
            send(ProcessEvent.OUT, "********** before");
        }

        // run rsync
        if (doNextStep) {
            int exitValue = runRsync(task);
            boolean rsyncSuccess = exitValue == 0;
            send(ProcessEvent.OUT, "********** rsync");
            if (rsyncSuccess) {
                // run after success
                command = taskExecute.getAfterSuccessCommand();
                if (taskExecute.isAfterSuccess() && StringUtils.isNoneEmpty(command)) {
                    doNextStep = runTaskStep(command, taskExecute.isAfterSuccessHaltOnError(), "AFTER RSYNC SUCCESS");
                    send(ProcessEvent.OUT, "********** after success");
                }
            } else {
                // run after failure
                command = taskExecute.getAfterFailureCommand();
                if (taskExecute.isAfterFailure() && StringUtils.isNoneEmpty(command)) {
                    doNextStep = runTaskStep(command, taskExecute.isAfterFailureHaltOnError(), "AFTER RSYNC FAILURE");
                    send(ProcessEvent.OUT, "********** after failure");
                }
            }
        }

        if (doNextStep) {
            // run after
            command = taskExecute.getAfterCommand();
            if (taskExecute.isAfter() && StringUtils.isNoneEmpty(command)) {
                doNextStep = runTaskStep(command, taskExecute.isAfterHaltOnError(), "AFTER RSYNC");
                send(ProcessEvent.OUT, "********** after");
            }
        }

        send(ProcessEvent.OUT, "");
        if (mTaskFailed) {
            mNumOfFailedTasks++;
        }

        taskHistoryBuilder.append(String.format("%s finished", Jota.millisToDateTime(System.currentTimeMillis()))).append("\n");
        mJotaManager.getTaskManager().getTaskById(task.getId()).appendHistory(taskHistoryBuilder.append("\n").toString());

        boolean doNextTask = !(mTaskFailed && taskExecute.isJobHaltOnError());
        return doNextTask;
    }

    private boolean runTaskStep(String command, boolean stopOnError, String description) {
        boolean doNextStep = false;

        try {
            if (run(command, stopOnError, description)) {
            } else {
                mTaskFailed = true;
            }
            doNextStep = true;
        } catch (InterruptedException ex) {
            mTaskFailed = true;
            //Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ExecutionFailedException ex) {
            mTaskFailed = true;
            Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return doNextStep;
    }

    private void runTasks() {
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
        mJotaManager.getJobManager().getJobById(mJob.getId()).appendHistory(mHistoryBuilder.append("\n").toString());
        try {
            mJotaManager.save();
        } catch (IOException ex) {
            Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writelogs() {
        File directory = new File(ServerOptions.INSTANCE.getLogDir());
        String outFile = String.format("%s.log", mJob.getName());
        String errFile = String.format("%s.err", mJob.getName());

        int logMode = mJob.getLogMode();
        if (logMode == 2) {
            outFile = String.format("%s %s.log", mJob.getName(), mJob.getLastRunDateTime("", mLastRun));
            errFile = String.format("%s %s.err", mJob.getName(), mJob.getLastRunDateTime("", mLastRun));
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
                builder.insert(0, "Save log\n");
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

                    send(mProcessEvent, line);
                }
            } catch (IOException e) {
                Xlog.timedErr(e.getLocalizedMessage());
            }
        }
    }
}
