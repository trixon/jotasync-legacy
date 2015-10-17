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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.dgc.VMID;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import org.apache.commons.cli.CommandLine;
import se.trixon.jota.ClientCallbacks;
import se.trixon.jota.Jota;
import se.trixon.jota.JotaHelper;
import se.trixon.jota.JotaServer;
import se.trixon.jota.ServerCommander;
import se.trixon.jota.ServerEvent;
import se.trixon.util.SystemHelper;
import se.trixon.util.Xlog;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class Server extends UnicastRemoteObject implements ServerCommander {

    private Set<ClientCallbacks> mClientCallbacks = Collections.newSetFromMap(new ConcurrentHashMap<ClientCallbacks, Boolean>());
    private final ResourceBundle mJotaBundle = Jota.getBundle();
    private final Options mOptions = Options.INSTANCE;
    private int mPort = Jota.DEFAULT_PORT_HOST;
    private String mRmiNameServer;
    private VMID mServerVmid;

    public Server(CommandLine cmd) throws RemoteException {
        super(0);
        if (cmd.hasOption("port")) {
            String port = cmd.getOptionValue("port");
            try {
                mPort = Integer.valueOf(port);
            } catch (NumberFormatException e) {
                Xlog.timedErr(String.format(mJotaBundle.getString("invalid_port"), port, Jota.DEFAULT_PORT_HOST));
            }
        }
        intiListeners();
        startServer();
    }

    @Override
    public void dirHome() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        StringBuilder builder = new StringBuilder("status\n");
        builder.append(String.format("vmid\t%s", mServerVmid.toString())).append("\n");
        builder.append(String.format("clients\t%d", mClientCallbacks.size())).append("\n");
        builder.append(String.format("cron\t%s", mOptions.isCronActive())).append("\n");
        builder.append(String.format("rsync\t%s", mOptions.getRsyncPath())).append("\n");
        String status = builder.toString();
        Xlog.timedOut("return " + status);

        return status;
    }

    @Override
    public VMID getVMID() throws RemoteException {
        return mServerVmid;
    }

    @Override
    public boolean isCronActive() throws RemoteException {
        return mOptions.isCronActive();
    }

    @Override
    public void registerClient(ClientCallbacks clientCallback, String hostname) throws RemoteException {
        Xlog.timedOut("registerClient(): " + hostname);
        mClientCallbacks.add(clientCallback);
    }

    @Override
    public void removeClient(ClientCallbacks clientCallback, String hostname) throws RemoteException {
        Xlog.timedOut("unregisterClient(): " + hostname);
        mClientCallbacks.remove(clientCallback);
    }

    @Override
    public void setCronActive(boolean enable) throws RemoteException {
        mOptions.setCronActive(enable);
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
    public void shutdown() throws RemoteException {
        Xlog.timedOut("shutdown");

        mClientCallbacks.stream().forEach((clientCallback) -> {
            try {
                clientCallback.onServerEvent(ServerEvent.SHUTDOWN);
            } catch (RemoteException ex) {
                // nvm
            }
        });

        try {
            Naming.unbind(mRmiNameServer);
            Jota.exit(0);
        } catch (NotBoundException | MalformedURLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void intiListeners() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdown();
            } catch (RemoteException ex) {
                // nvm
            }
        }));

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            Xlog.timedOut(String.format(">>> %s on %s key=%s", "preferenceChange", SystemHelper.getHostname(), evt.getKey()));
            HashSet<ClientCallbacks> invalidClientCallbacks = new HashSet<>();

            for (ClientCallbacks clientCallback : mClientCallbacks) {
                switch (evt.getKey()) {
                    case Options.KEY_CRON_ACTIVE: {
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

    private void startServer() {
        mRmiNameServer = JotaHelper.getRmiName(SystemHelper.getHostname(), mPort, JotaServer.class);

        try {
            LocateRegistry.createRegistry(mPort);
            mServerVmid = new VMID();
            //mServerOptions = new ServerOptions();
            Naming.rebind(mRmiNameServer, this);
            String message = String.format("started: %s (%s)", mRmiNameServer, mServerVmid.toString());
            Xlog.timedOut(message);
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
}
