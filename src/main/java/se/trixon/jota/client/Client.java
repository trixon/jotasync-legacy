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
package se.trixon.jota.client;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.dgc.VMID;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.jota.client.ui.MainFrame;
import se.trixon.jota.shared.ClientCallbacks;
import se.trixon.jota.shared.Jota;
import se.trixon.jota.shared.JotaClient;
import se.trixon.jota.shared.JotaHelper;
import se.trixon.jota.shared.JotaServer;
import se.trixon.jota.shared.ProcessEvent;
import se.trixon.jota.shared.ServerCommander;
import se.trixon.jota.shared.ServerEvent;
import se.trixon.jota.shared.ServerEventListener;
import se.trixon.jota.shared.job.Job;
import se.trixon.jota.shared.task.Task;
import se.trixon.util.SystemHelper;
import se.trixon.util.Xlog;
import se.trixon.util.dictionary.Dict;
import se.trixon.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlsson
 */
public final class Client extends UnicastRemoteObject implements ClientCallbacks {

    private VMID mClientVmid;
    private boolean mExitOnException;
    private String mHost = SystemHelper.getHostname();
    private final ResourceBundle mJotaBundle = Jota.getBundle();
    private MainFrame mMainFrame = null;
    private final Manager mManager = Manager.getInstance();
    private final ClientOptions mOptions = ClientOptions.INSTANCE;
    private int mPortClient = Jota.DEFAULT_PORT_CLIENT;
    private int mPortHost = Jota.DEFAULT_PORT_HOST;
    private String mRmiNameClient;
    private String mRmiNameServer;
    private ServerCommander mServerCommander;
    private final HashSet<ServerEventListener> mServerEventListeners = new HashSet<>();
    private boolean mShutdownRequested;

    public Client(CommandLine cmd) throws RemoteException {
        super(0);
        mManager.setClient(this);
        if (cmd.hasOption(Main.OPT_HOST)) {
            mHost = cmd.getOptionValue(Main.OPT_HOST);
        }

        if (cmd.hasOption(Main.OPT_PORT)) {
            String port = cmd.getOptionValue(Main.OPT_PORT);
            try {
                mPortHost = Integer.valueOf(port);
            } catch (NumberFormatException e) {
                Xlog.timedErr(String.format(mJotaBundle.getString("invalid_port"), port, Jota.DEFAULT_PORT_HOST));
            }
        }

        if (cmd.hasOption(Main.OPT_CLIENT_PORT)) {
            String port = cmd.getOptionValue(Main.OPT_CLIENT_PORT);
            try {
                mPortClient = Integer.valueOf(port);
            } catch (NumberFormatException e) {
                Xlog.timedErr(String.format(mJotaBundle.getString("invalid_port"), port, Jota.DEFAULT_PORT_CLIENT));
            }
        }

        mExitOnException = cmd.hasOption(Main.OPT_STATUS)
                || cmd.hasOption(Main.OPT_SHUTDOWN)
                || cmd.hasOption(Main.OPT_CRON)
                || cmd.hasOption(Main.OPT_LIST_JOBS)
                || cmd.hasOption(Main.OPT_LIST_TASKS);

        startRMI();

        if (cmd.hasOption(Main.OPT_STATUS)) {
            execute(Command.DISPLAY_STATUS);
            Jota.exit();
        } else if (cmd.hasOption(Main.OPT_SHUTDOWN)) {
            execute(Command.SHUTDOWN);
            Jota.exit();
        } else if (cmd.hasOption(Main.OPT_CRON)) {
            String state = cmd.getOptionValue(Main.OPT_CRON);
            if (state.equalsIgnoreCase("on")) {
                execute(Command.START_CRON);
            } else if (state.equalsIgnoreCase("off")) {
                execute(Command.STOP_CRON);
            } else {
                Xlog.timedOut("invalid cron argument");
            }
            Jota.exit();
        } else if (cmd.hasOption(Main.OPT_LIST_JOBS)) {
            execute(Command.LIST_JOBS);
            Jota.exit();
        } else if (cmd.hasOption(Main.OPT_LIST_TASKS)) {
            execute(Command.LIST_TASKS);
            Jota.exit();
        } else {
            displayGui();
        }
    }

    public boolean addServerEventListener(ServerEventListener serverEventListener) {
        return mServerEventListeners.add(serverEventListener);
    }

    public void execute(Command command) {
        Xlog.timedOut(command.getMessage());
        try {
            switch (command) {
                case DISPLAY_STATUS:
                    Xlog.timedOut(mServerCommander.getStatus());
                    break;

                case LIST_JOBS:
                    Xlog.timedOut(mServerCommander.listJobs());
                    break;

                case LIST_TASKS:
                    Xlog.timedOut(mServerCommander.listTasks());
                    break;

                case START_CRON:
                    mServerCommander.setCronActive(true);
                    break;

                case STOP_CRON:
                    mServerCommander.setCronActive(false);
                    break;

                case SHUTDOWN:
                    mShutdownRequested = true;
                    mServerCommander.shutdown();
                    break;
            }
        } catch (RemoteException ex) {
            if (command != Command.SHUTDOWN) {
                Xlog.timedErr(ex.getLocalizedMessage());
            }
        }
    }

    public String getHost() {
        return mHost;
    }

    public int getPortClient() {
        return mPortClient;
    }

    public int getPortHost() {
        return mPortHost;
    }

    @Override
    public void onProcessEvent(ProcessEvent processEvent, Job job, Task task, Object object) throws RemoteException {
        mServerEventListeners.stream().forEach((serverEventListener) -> {
            serverEventListener.onProcessEvent(processEvent, job, task, object);
        });
    }

    @Override
    public void onServerEvent(ServerEvent serverEvent) throws RemoteException {
        mServerEventListeners.stream().forEach((serverEventListener) -> {
            serverEventListener.onServerEvent(serverEvent);
        });
    }

    public boolean removeServerEventListener(ServerEventListener serverEventListener) {
        return mServerEventListeners.remove(serverEventListener);
    }

    public boolean serverStart() throws URISyntaxException, IOException, NotBoundException {
        boolean serverProbablyStarted = false;
        Xlog.timedOut(Dict.SERVER_START.toString());

        StringBuilder java = new StringBuilder();
        java.append(System.getProperty("java.home")).append(SystemUtils.FILE_SEPARATOR)
                .append("bin").append(SystemUtils.FILE_SEPARATOR)
                .append("java");

        if (SystemUtils.IS_OS_WINDOWS) {
            java.append(".exe");
        }

        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        File jarFile = new File(codeSource.getLocation().toURI().getPath());
        if (jarFile.getAbsolutePath().endsWith(".jar")) {
            ArrayList<String> command = new ArrayList<>();
            command.add(java.toString());
            command.add("-cp");
            command.add(jarFile.getAbsolutePath());
            command.add("Server");
            command.add("--port");
            command.add(String.valueOf(mOptions.getAutostartServerPort()));

            Xlog.timedOut(StringUtils.join(command, " "));
            ProcessBuilder processBuilder = new ProcessBuilder(command).inheritIO();
            processBuilder.start();

            try {
                Thread.sleep(mOptions.getAutostartServerConnectDelay());
            } catch (InterruptedException ex) {
                Xlog.timedErr(ex.getLocalizedMessage());
            }

            serverProbablyStarted = true;
        } else {
            Xlog.timedErr("Autostart of Server only works from within a jar file.");
        }

        return serverProbablyStarted;
    }

    public void setHost(String host) {
        mHost = host;
    }

    public void setPortClient(int portClient) {
        mPortClient = portClient;
    }

    public void setPortHost(int portHost) {
        mPortHost = portHost;
    }

    void connectToServer() throws NotBoundException, MalformedURLException, RemoteException, java.rmi.ConnectException, java.rmi.ConnectIOException, java.rmi.UnknownHostException, SocketException {
        mRmiNameServer = JotaHelper.getRmiName(mHost, mPortHost, JotaServer.class);
        mServerCommander = (ServerCommander) Naming.lookup(mRmiNameServer);
        mManager.setServerCommander(mServerCommander);
        mClientVmid = new VMID();

        Xlog.timedOut(String.format("server found at %s.", mRmiNameServer));
        Xlog.timedOut(String.format("server vmid: %s", mServerCommander.getVMID()));
        Xlog.timedOut(String.format("client connected to %s", mRmiNameServer));
        Xlog.timedOut(String.format("client vmid: %s", mClientVmid.toString()));

        mServerCommander.registerClient(this, SystemHelper.getHostname());
    }

    private void displayGui() {
        if (GraphicsEnvironment.isHeadless()) {
            Xlog.timedErr("Can't open gui when in headless mode.");
            Jota.exit(1);

            return;
        }

        if (mOptions.isAutostartServer() && !mManager.isConnected()) {
            try {
                if (serverStart()) {
                    mManager.connect(SystemHelper.getHostname(), mOptions.getAutostartServerPort());
                }
            } catch (URISyntaxException | IOException | NotBoundException ex) {
                Xlog.timedErr(ex.getLocalizedMessage());
            }
        }
        if (mOptions.isForceLookAndFeel()) {
            try {
                UIManager.setLookAndFeel(SwingHelper.getLookAndFeelClassName(mOptions.getLookAndFeel()));
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                Xlog.timedErr(ex.getMessage());
            }
        }

        java.awt.EventQueue.invokeLater(() -> {
            mMainFrame = new MainFrame();
            addServerEventListener(mMainFrame);
            mMainFrame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    mManager.disconnect();
                }
            });
            mMainFrame.setVisible(true);
        });
    }

    private void initCallbackServer() throws RemoteException, MalformedURLException, java.rmi.server.ExportException {
        mRmiNameClient = JotaHelper.getRmiName(SystemHelper.getHostname(), mPortClient, JotaClient.class);
        LocateRegistry.createRegistry(mPortClient);
        Naming.rebind(mRmiNameClient, this);
    }

    private void startRMI() throws RemoteException {
        try {
            initCallbackServer();
            mManager.connect(mHost, mPortHost);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                Xlog.timedOut("Shutting down client jvm...");
                try {
                    mServerCommander.removeClient(Client.this, SystemHelper.getHostname());
                } catch (RemoteException ex) {
                    if (!mShutdownRequested) {
                        Xlog.timedErr(ex.getLocalizedMessage());
                    }
                }
            }));
        } catch (NotBoundException | MalformedURLException | java.rmi.server.ExportException | java.rmi.ConnectException | java.rmi.ConnectIOException | java.rmi.UnknownHostException | SocketException ex) {
            if (mExitOnException) {
                Xlog.timedErr(ex.getLocalizedMessage());
                Jota.exit();
            }
        }
    }

    public enum Command {

        DISPLAY_STATUS("Request status information"),
        LIST_JOBS("List jobs"),
        LIST_TASKS("List tasks"),
        SHUTDOWN("Request shutdown"),
        START_CRON("Request start cron"),
        STOP_CRON("Request stop cron");
        private final String mMessage;

        private Command(String message) {
            mMessage = message;
        }

        public String getMessage() {
            return mMessage;
        }
    }
}
