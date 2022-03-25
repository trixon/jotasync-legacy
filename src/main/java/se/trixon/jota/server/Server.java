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
package se.trixon.jota.server;

import it.sauronsoftware.cron4j.Scheduler;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.dgc.VMID;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.Xlog;
import se.trixon.jota.shared.ClientCallbacks;
import se.trixon.jota.shared.Jota;
import se.trixon.jota.shared.JotaHelper;
import se.trixon.jota.shared.JotaServer;
import se.trixon.jota.shared.ProcessEvent;
import se.trixon.jota.shared.ServerCommander;
import se.trixon.jota.shared.ServerEvent;
import se.trixon.jota.shared.job.Job;
import se.trixon.jota.shared.task.Task;

/**
 *
 * @author Patrik Karlström
 */
class Server extends UnicastRemoteObject implements ServerCommander {

    private Set<ClientCallbacks> mClientCallbacks = Collections.newSetFromMap(new ConcurrentHashMap<ClientCallbacks, Boolean>());
    private HashMap<Long, JobExecutor> mJobExecutors = new HashMap<>();
    private final JobManager mJobManager = JobManager.INSTANCE;
    private final ResourceBundle mJotaBundle = Jota.getBundle();
    private final JotaManager mJotaManager = JotaManager.getInstance();
    private final ServerOptions mOptions = ServerOptions.getInstance();
    private int mPort = Jota.DEFAULT_PORT_HOST;
    private String mRmiNameServer;
    private Scheduler mScheduler = new Scheduler();
    private VMID mServerVmid;
    private final TaskManager mTaskManager = TaskManager.INSTANCE;

    Server(CommandLine cmd) throws RemoteException, IOException {
        super(0);

        if (cmd.hasOption("port")) {
            String port = cmd.getOptionValue("port");
            try {
                mPort = Integer.valueOf(port);
            } catch (NumberFormatException e) {
                Xlog.timedErr(String.format(mJotaBundle.getString("invalid_port"), port, Jota.DEFAULT_PORT_HOST));
            }
        }

        mJotaManager.load();
        intiListeners();
        startServer();
    }

    @Override
    public String getAboutRsync() throws RemoteException {
        ProcessBuilder processBuilder = new ProcessBuilder(new String[]{mOptions.getRsyncPath()});
        String result = "";

        try {
            Process p = processBuilder.start();
            result = IOUtils.toString(p.getErrorStream());
            p.waitFor();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result.split("Usage: rsync")[0].trim();
    }

    @Override
    public String getHistory() throws RemoteException {
        String history = "";

        try {
            history = FileUtils.readFileToString(mJotaManager.getHistoryFile(), Charset.defaultCharset());
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        return history;
    }

    @Override
    public Job getJob(long jobId) throws RemoteException {
        return mJobManager.getJobById(jobId);
    }

    @Override
    public LinkedList<Job> getJobs() throws RemoteException {
        return mJobManager.getJobs();
    }

    @Override
    public String getLogDir() throws RemoteException {
        return mOptions.getLogDir();
    }

    @Override
    public String getRsyncPath() throws RemoteException {
        return mOptions.getRsyncPath();
    }

    @Override
    public long getSpeedDial(int key) {
        return mOptions.getSpeedDial(key);
    }

    @Override
    public String getStatus() throws RemoteException {
        int pad = 13;
        StringBuilder builder = new StringBuilder("Status\n");
        builder.append(String.format("  %s%s", StringUtils.rightPad("vmid", pad), mServerVmid.toString())).append("\n");
        builder.append(String.format("  %s%d", StringUtils.rightPad("clients", pad), mClientCallbacks.size())).append("\n");
        builder.append(String.format("  %s%s", StringUtils.rightPad("cron active", pad), mOptions.isCronActive())).append("\n");
        builder.append(String.format("  %s%s", StringUtils.rightPad("rsync", pad), mOptions.getRsyncPath())).append("\n");
        builder.append(String.format("  %s%d", StringUtils.rightPad("jobs", pad), mJobManager.getJobs().size())).append("\n");
        builder.append(String.format("  %s%d", StringUtils.rightPad("tasks", pad), mTaskManager.getTasks().size())).append("\n");
        String status = builder.toString();
        Xlog.timedOut(status);

        return status;
    }

    @Override
    public LinkedList<Task> getTasks() throws RemoteException {
        return mTaskManager.getTasks();
    }

    @Override
    public VMID getVMID() throws RemoteException {
        return mServerVmid;
    }

    @Override
    public boolean hasJobs() throws RemoteException {
        return mJobManager.hasJobs();
    }

    @Override
    public boolean isCronActive() throws RemoteException {
        return mOptions.isCronActive();
    }

    @Override
    public boolean isRunning(Job job) throws RemoteException {
        return mJobExecutors.containsKey(job.getId());
    }

    @Override
    public String listJobs() throws RemoteException {
        StringBuilder builder = new StringBuilder(String.format("Found %d job(s).\n", mJobManager.getJobs().size()));

        mJobManager.getJobs().stream().forEach((job) -> {
            builder.append(String.format("  %s - %s", StringUtils.rightPad(job.getName(), 20), job.getDescription())).append("\n");
        });

        String jobs = builder.toString();
        Xlog.timedOut(jobs);

        return jobs;
    }

    @Override
    public String listTasks() throws RemoteException {
        StringBuilder builder = new StringBuilder(String.format("Found %d task(s).\n", mTaskManager.getTasks().size()));

        mTaskManager.getTasks().stream().forEach((task) -> {
            builder.append(String.format("  %s - %s", StringUtils.rightPad(task.getName(), 20), task.getDescription())).append("\n");
        });

        String tasks = builder.toString();
        Xlog.timedOut(tasks);

        return tasks;
    }

    @Override
    public void registerClient(ClientCallbacks clientCallback, String hostname) throws RemoteException {
        Xlog.timedOut("client connected: " + hostname);
        mClientCallbacks.add(clientCallback);
    }

    @Override
    public void removeClient(ClientCallbacks clientCallback, String hostname) throws RemoteException {
        if (mClientCallbacks.contains(clientCallback)) {
            Xlog.timedOut("client disconnected: " + hostname);
            mClientCallbacks.remove(clientCallback);
        }
    }

    @Override
    public void saveJota() throws RemoteException {
        try {
            mJotaManager.save();
            mClientCallbacks.stream().forEach((clientCallback) -> {
                try {
                    clientCallback.onServerEvent(ServerEvent.JOTA_CHANGED);
                } catch (RemoteException ex) {
                    // nvm
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (mOptions.isCronActive()) {
            cronOn();
        }
    }

    @Override
    public void setCronActive(boolean enable) throws RemoteException {
        mOptions.setCronActive(enable);
        if (enable) {
            cronOn();
        } else {
            cronOff();
        }
    }

    @Override
    public void setJobs(LinkedList<Job> jobs) throws RemoteException {
        mJobManager.setJobs(jobs);
    }

    @Override
    public void setLogDir(String path) throws RemoteException {
        mOptions.setLogDir(path);
    }

    @Override
    public void setRsyncPath(String path) throws RemoteException {
        mOptions.setRsyncPath(path);
    }

    @Override
    public void setSpeedDial(int key, long jobId) {
        mOptions.setSpeedDial(key, jobId);
    }

    @Override
    public void setTasks(LinkedList<Task> tasks) throws RemoteException {
        mTaskManager.setTasks(tasks);
    }

    @Override
    public void shutdown() throws RemoteException {
        Xlog.timedOut("shutdown");

        notifyClientsShutdown();

        try {
            Naming.unbind(mRmiNameServer);
            Jota.exit(0);
        } catch (NotBoundException | MalformedURLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void startJob(Job job, boolean dryRun) throws RemoteException {
        Xlog.timedOut(String.format("Job started: %s", job.getName()));
        for (ClientCallbacks clientCallback : mClientCallbacks) {
            clientCallback.onProcessEvent(ProcessEvent.STARTED, job, null, null);
        }

        JobExecutor jobExecutor = new JobExecutor(this, job, dryRun);
        mJobExecutors.put(job.getId(), jobExecutor);
        jobExecutor.start();
    }

    @Override
    public void stopJob(Job job) throws RemoteException {
        Xlog.timedOut(String.format("Cancel job: %s", job.getName()));
        try {
            mJobExecutors.get(job.getId()).stopJob();
            mJobExecutors.remove(job.getId());
        } catch (NullPointerException e) {
        }
    }

    @Override
    public JobValidator validate(Job job) throws RemoteException {
        return new JobValidator(job);
    }

    private void cronOff() {
        if (mScheduler.isStarted()) {
            mScheduler.stop();
        }

        mScheduler = new Scheduler();
    }

    private void cronOn() {
        cronOff();

        for (Job job : mJobManager.getJobs()) {
            if (job.isCronActive()) {
                for (String cronString : StringUtils.split(job.getCronItems(), "|")) {
                    mScheduler.schedule(cronString, () -> {
                        try {
                            if (!isRunning(job)) {
                                startJob(job, false);
                            }
                        } catch (RemoteException ex) {
                            Xlog.timedErr(ex.getLocalizedMessage());
                        }
                    });
                }
            }
        }

        mScheduler.start();
    }

    private void intiListeners() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            notifyClientsShutdown();
        }));

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            HashSet<ClientCallbacks> invalidClientCallbacks = new HashSet<>();

            for (ClientCallbacks clientCallback : mClientCallbacks) {
                switch (evt.getKey()) {
                    case ServerOptions.KEY_CRON_ACTIVE: {
                        try {
                            clientCallback.onServerEvent(ServerEvent.CRON_CHANGED);
                        } catch (RemoteException ex) {
                            //Add invalid reference for removal
                            invalidClientCallbacks.add(clientCallback);
                        }
                    }
                    break;
                }
            }

            invalidClientCallbacks.stream().forEach((invalidClientCallback) -> {
                //Remove invalid reference
                mClientCallbacks.remove(invalidClientCallback);
            });
        });
    }

    private void notifyClientsShutdown() {
        mClientCallbacks.stream().forEach((clientCallback) -> {
            try {
                clientCallback.onServerEvent(ServerEvent.SHUTDOWN);
            } catch (RemoteException ex) {
                // nvm
            }
        });
    }

    private void startServer() {
        mRmiNameServer = JotaHelper.getRmiName(SystemHelper.getHostname(), mPort, JotaServer.class);

        try {
            LocateRegistry.createRegistry(mPort);
            mServerVmid = new VMID();
            //mServerOptions = new ServerOptions();
            Naming.rebind(mRmiNameServer, this);
            String message = String.format("started: %s (%s)", mRmiNameServer, mServerVmid.toString());
            Xlog.timedOut(message);
            listJobs();
            listTasks();
            getStatus();
            if (mOptions.isCronActive()) {
                cronOn();
            }
        } catch (IllegalArgumentException e) {
            Xlog.timedErr(e.getLocalizedMessage());
            Jota.exit();
        } catch (RemoteException e) {
            //nvm - server was running
            Xlog.timedErr(e.getLocalizedMessage());
            Jota.exit();
        } catch (MalformedURLException ex) {
            Xlog.timedErr(ex.getLocalizedMessage());
            Jota.exit();
        }
    }

    Set<ClientCallbacks> getClientCallbacks() {
        return mClientCallbacks;
    }

    HashMap<Long, JobExecutor> getJobExecutors() {
        return mJobExecutors;
    }
}
