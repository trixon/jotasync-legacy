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
package se.trixon.jotaserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import se.trixon.jota.ClientCallbacks;
import se.trixon.jota.Jota;
import se.trixon.jota.ProcessEvent;
import se.trixon.jota.job.Job;
import se.trixon.util.Xlog;

/**
 *
 * @author Patrik Karlsson
 */
public class JobExecutor extends Thread {

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
            boolean success = runPre();
            if (success) {
                //
                // run all tasks
                //

                //
                // run Post
                //
                //ProcessBuilder processBuilder = new ProcessBuilder("rsync", "--version");
//                ProcessBuilder processBuilder = new ProcessBuilder("/home/pata/bin/ticktock.sh");
//                mCurrentProcess = processBuilder.start();
//
//                new ProcessLogThread(mCurrentProcess.getInputStream(), ProcessEvent.OUT).start();
//                new ProcessLogThread(mCurrentProcess.getErrorStream(), ProcessEvent.ERR).start();
//
//                mCurrentProcess.waitFor();
            }

            updateJobStatus(0);
            mServer.getClientCallbacks().stream().forEach((clientCallback) -> {
                try {
                    clientCallback.onProcessEvent(ProcessEvent.FINISHED, mJob, null, null);
                } catch (RemoteException ex) {
                    Logger.getLogger(JobExecutor.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            Xlog.timedOut(String.format("Job finished: %s", mJob.getName()));
        } catch (InterruptedException ex) {
            mCurrentProcess.destroy();
            updateJobStatus(99);
            mServer.getClientCallbacks().stream().forEach((clientCallback) -> {
                try {
                    clientCallback.onProcessEvent(ProcessEvent.CANCELED, mJob, null, null);
                } catch (RemoteException ex1) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        writelogs();
    }

    private boolean runPre() throws IOException, InterruptedException {
        boolean result = true;

        if (mJob.isRunBefore() && mJob.getRunBeforeCommand().trim().length() > 0) {
            ProcessBuilder processBuilder = new ProcessBuilder(mJob.getRunBeforeCommand());
            mCurrentProcess = processBuilder.start();

            new ProcessLogThread(mCurrentProcess.getInputStream(), ProcessEvent.OUT).start();
            new ProcessLogThread(mCurrentProcess.getErrorStream(), ProcessEvent.ERR).start();

            mCurrentProcess.waitFor();
            if (mJob.isRunBeforeHaltOnError()) {
                result = mCurrentProcess.exitValue() == 0;
            }
        }

        return result;
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
                    if (mJob.isLogSeparateErrors()) {
                        if (mJob.isLogOutput() && mProcessEvent == ProcessEvent.OUT) {
                            mOutBuffer.append(mDateTimePrefix + line + System.lineSeparator());//Append will screw up sync
                        } else if (mJob.isLogErrors() && mProcessEvent == ProcessEvent.ERR) {
                            mErrBuffer.append(mDateTimePrefix + line + System.lineSeparator());//Append will screw up sync
                        }
                    } else if (!mJob.isLogSeparateErrors()) {
                        if (mJob.isLogOutput() && mProcessEvent == ProcessEvent.OUT) {
                            mOutBuffer.append(mDateTimePrefix + line + System.lineSeparator());//Append will screw up sync
                        } else if (mJob.isLogErrors() && mProcessEvent == ProcessEvent.ERR) {
                            mOutBuffer.append(mDateTimePrefix + line + System.lineSeparator());//Append will screw up sync
                        }
                    }

                    for (ClientCallbacks clientCallback : mServer.getClientCallbacks()) {
                        clientCallback.onProcessEvent(mProcessEvent, mJob, null, line);
                    }
                }
            } catch (IOException e) {
                Xlog.timedErr(e.getLocalizedMessage());
            }
        }
    }
}
