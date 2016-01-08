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
import java.rmi.UnmarshalException;
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
import se.trixon.jota.shared.task.TaskExecuteSection;
import se.trixon.jota.shared.task.Task;
import se.trixon.util.Xlog;

/**
 *
 * @author Patrik Karlsson
 */
class JobExecutor extends Thread {

    private Process mCurrentProcess;
    private final StringBuffer mErrBuffer;
    private final Job mJob;
    private long mLastRun;
    private final StringBuffer mOutBuffer;
    private final Server mServer;

    JobExecutor(Server server, Job job) {
        mJob = job;
        mServer = server;

        mErrBuffer = new StringBuffer();
        mOutBuffer = new StringBuffer();
    }

    @Override
    public void run() {
        mLastRun = System.currentTimeMillis();
        try {
            if (runBeforeJob()) {
                if (runTasks()) {
                    // run after
                } else {
                    send(ProcessEvent.OUT, "one or more tasks failed");
                }
            } else {
                send(ProcessEvent.OUT, "before failed and will not continue");
            }

            updateJobStatus(0);
            send(ProcessEvent.FINISHED, "\n\nJob finished");
            Xlog.timedOut(String.format("Job finished: %s", mJob.getName()));
        } catch (InterruptedException ex) {
            mCurrentProcess.destroy();
            updateJobStatus(99);
            mServer.getClientCallbacks().stream().forEach((clientCallback) -> {
                try {
                    clientCallback.onProcessEvent(ProcessEvent.CANCELED, mJob, null, null);
                } catch (RemoteException ex1) {
                    // nvm Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        writelogs();
        mServer.getJobExecutors().remove(mJob.getId());
    }

    private boolean runBeforeJob() throws IOException, InterruptedException {
        boolean result = true;
        JobExecuteSection jobExecuteSection = mJob.getExecuteSection();
        if (jobExecuteSection.isRunBefore() && StringUtils.isNoneEmpty(jobExecuteSection.getRunBeforeCommand())) {
            send(ProcessEvent.OUT, "run before job...");
            ArrayList<String> command = new ArrayList<>();
            command.add(jobExecuteSection.getRunBeforeCommand());
            runProcess(command);

            if (jobExecuteSection.isRunBeforeHaltOnError()) {
                result = mCurrentProcess.exitValue() == 0;
            }

            Thread.sleep(100);

            if (mCurrentProcess.exitValue() == 0) {
                send(ProcessEvent.OUT, "before job OK");
            } else {
                send(ProcessEvent.OUT, "before job failed");
            }

            send(ProcessEvent.OUT, "");
        }

        return result;
    }

    private boolean runBeforeTask(Task task) throws IOException, InterruptedException {
        boolean result = true;
        TaskExecuteSection taskExecuteSection = task.getExecuteSection();
        send(ProcessEvent.OUT, "runBeforeTask");
        send(ProcessEvent.OUT, "" + taskExecuteSection.isBefore());
        send(ProcessEvent.OUT, taskExecuteSection.getBeforeCommand());
        send(ProcessEvent.OUT, "");
        send(ProcessEvent.OUT, "");
        if (taskExecuteSection.isBefore() && StringUtils.isNoneEmpty(taskExecuteSection.getBeforeCommand())) {
            send(ProcessEvent.OUT, "run before task...");
            ArrayList<String> command = new ArrayList<>();
            command.add(taskExecuteSection.getBeforeCommand());
            runProcess(command);

            if (taskExecuteSection.isBeforeHaltOnError()) {
                result = mCurrentProcess.exitValue() == 0;
            }

            Thread.sleep(100);

            if (mCurrentProcess.exitValue() == 0) {
                send(ProcessEvent.OUT, "before task OK");
            } else {
                send(ProcessEvent.OUT, "before task failed");
            }

            send(ProcessEvent.OUT, "");
        }

        return result;
    }

    private void runProcess(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        mCurrentProcess = processBuilder.start();

        new ProcessLogThread(mCurrentProcess.getInputStream(), ProcessEvent.OUT).start();
        new ProcessLogThread(mCurrentProcess.getErrorStream(), ProcessEvent.ERR).start();

        mCurrentProcess.waitFor();
    }

    private int runRsync() {
        int result = 0;

        try {
            ArrayList<String> command = new ArrayList<>();
            command.add("echo");
            command.add("rsync");
            runProcess(command);

            Thread.sleep(500);
            send(ProcessEvent.OUT, "");
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    private boolean runTask(Task task) throws IOException, InterruptedException {
        boolean result = true;

        send(ProcessEvent.OUT, "Run task: " + task.getName());
        if (runBeforeTask(task)) {
            runRsync();
        }

        return result;
    }

    private boolean runTasks() {
        boolean result = true;

        send(ProcessEvent.OUT, "begin to run all tasks");

        for (Task task : mJob.getTasks()) {

            try {
                if (!runTask(task)) {
                    break;
                }
                Thread.sleep(500);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return result;
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
        JotaManager.INSTANCE.getJobManager().getJobById(mJob.getId()).setLastRun(mLastRun);
        JotaManager.INSTANCE.getJobManager().getJobById(mJob.getId()).setLastRunExitCode(exitCode);
        try {
            JotaManager.INSTANCE.save();
        } catch (IOException ex) {
            Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writelogs() {
        File directory = JotaManager.INSTANCE.getLogDirectory();
        String outFile = String.format("%s.log", mJob.getName());
        String errFile = String.format("%s.err", mJob.getName());

        int logMode = mJob.getLogMode();
        if (logMode == 2) {
            outFile = String.format("%s %s.log", mJob.getName(), mJob.getLastRunDateTime("", mLastRun));
            errFile = String.format("%s %s.err", mJob.getName(), mJob.getLastRunDateTime("", mLastRun));
        }

        boolean append = logMode == 0;

        try {
            File file = new File(directory, outFile);

            if (mJob.isLogOutput() || mJob.isLogErrors() && !mJob.isLogSeparateErrors()) {
                FileUtils.writeStringToFile(file, mOutBuffer.toString(), append);
                Xlog.timedOut("Write log: " + file.getAbsolutePath());
            }

            if (mJob.isLogErrors() && mJob.isLogSeparateErrors()) {
                file = new File(directory, errFile);
                FileUtils.writeStringToFile(file, mErrBuffer.toString(), append);
                Xlog.timedOut("Write log: " + file.getAbsolutePath());
            }
        } catch (IOException ex) {
            Xlog.timedErr(ex.getLocalizedMessage());
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
