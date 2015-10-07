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
package se.trixon.jotaclient;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.dgc.VMID;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public final class Client extends UnicastRemoteObject implements ClientCallbacks {

    private VMID mClientVmid;
    private String mRmiNameServer;
    private ServerCommander mServerCommander;
    private final ResourceBundle mJotaBundle = Jota.getBundle();

    private String mHost = SystemHelper.getHostname();
    private int mPort = Jota.DEFAULT_PORT;

    public Client(CommandLine cmd) throws RemoteException {
        super(0);
        if (cmd.hasOption("host")) {
            mHost = cmd.getOptionValue("host");
        }

        if (cmd.hasOption("port")) {
            String port = cmd.getOptionValue("port");
            try {
                mPort = Integer.valueOf(port);
            } catch (NumberFormatException e) {
                Xlog.timedErr(String.format(mJotaBundle.getString("invalid_port"), port, Jota.DEFAULT_PORT));
            }
        }

        try {
            connectToServer();
        } catch (NotBoundException | MalformedURLException | UnknownHostException ex) {
            Xlog.timedErr(ex.getLocalizedMessage());
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            Jota.exit();
        }

        if (cmd.hasOption("status")) {
            execute(Command.DISPLAY_STATUS);
            Jota.exit();
        } else if (cmd.hasOption("shutdown")) {
            execute(Command.SHUTDOWN);
            Jota.exit();
        } else if (cmd.hasOption("enable")) {
            execute(Command.START_CRON);
            Jota.exit();
        } else if (cmd.hasOption("disable")) {
            execute(Command.STOP_CRON);
            Jota.exit();
        } else {
            //displayGui();
        }
    }

    @Override
    public void onServerEvent(ServerEvent serverEvent) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onTimeWillTell(Date date) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void connectToServer() throws NotBoundException, MalformedURLException, RemoteException, java.net.UnknownHostException {
        Xlog.timedOut("connectToServer()");
        mRmiNameServer = JotaHelper.getRmiName(mHost, mPort, JotaServer.class);
        Xlog.timedOut(mRmiNameServer);
        mServerCommander = (ServerCommander) Naming.lookup(mRmiNameServer);
        //mServerOptions = mServerCommander.loadServerOptions();
        mClientVmid = new VMID();

        String message;
        message = String.format("server found at %s.", mRmiNameServer);
        Xlog.timedOut(message);
        message = "server vmid: " + mServerCommander.getVMID();
        Xlog.timedOut(message);
        message = String.format("client connected to %s", mRmiNameServer);
        Xlog.timedOut(message);
        message = "client vmid: " + mClientVmid.toString();
        Xlog.timedOut(message);

        mServerCommander.registerClient(this, SystemHelper.getHostname());

//        mConnectionListeners.stream().forEach((connectionListener) -> {
//            connectionListener.onConnectionClientConnect();
//        });
    }

    public void execute(Command command) {
        Xlog.timedOut(command.getMessage());
        try {
            switch (command) {
                case DISPLAY_STATUS:
                    mServerCommander.displayStatus();
                    break;

                case START_CRON:
                    mServerCommander.setCronActive(true);
                    break;

                case STOP_CRON:
                    mServerCommander.setCronActive(false);
                    break;

                case SHUTDOWN:
                    mServerCommander.shutdown();
                    break;

                case DIR_HOME:
                    mServerCommander.dirHome();
                    break;
            }
        } catch (RemoteException ex) {
            if (command != Command.SHUTDOWN) {
                Xlog.timedErr(ex.getLocalizedMessage());
            }
        }
    }

    public enum Command {

        DIR_HOME("ls /home"),
        DISPLAY_STATUS("Request status information"),
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
