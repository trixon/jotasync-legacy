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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.jota.ClientCallbacks;
import se.trixon.jota.ProcessEvent;
import se.trixon.jota.job.Job;
import se.trixon.util.Xlog;

/**
 *
 * @author Patrik Karlsson
 */
public class JobExecutor extends Thread {

    private final Job mJob;
    private long mLastRun;
    private final Server mServer;
    private Process mCurrentProcess;

    JobExecutor(Server server, Job job) {
        mJob = job;
        mServer = server;
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

            JotaManager.INSTANCE.getJobManager().getJobById(mJob.getId()).setLastRun(mLastRun);
            JotaManager.INSTANCE.save();

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

    class ProcessLogThread extends Thread {

        private final InputStream mInputStream;
        private final ProcessEvent mProcessEvent;

        public ProcessLogThread(InputStream inputStream, ProcessEvent processEvent) {
            mInputStream = inputStream;
            mProcessEvent = processEvent;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(mInputStream), 1);
                String line;
                while ((line = reader.readLine()) != null) {
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
