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

import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.jota.shared.ServerCommander;
import se.trixon.util.SystemHelper;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class Manager {

    private Client mClient;
    private final HashSet<ConnectionListener> mConnectionListeners = new HashSet<>();
    private ServerCommander mServerCommander;

    public static Manager getInstance() {
        return ManagerHolder.INSTANCE;
    }

    private Manager() {
    }

    public boolean addConnectionListeners(ConnectionListener connectionListener) {
        return mConnectionListeners.add(connectionListener);
    }

    public void connect(String host, int port) throws NotBoundException, MalformedURLException, RemoteException, SocketException {
        mClient.setHost(host);
        mClient.setPortHost(port);
        mClient.connectToServer();

        mConnectionListeners.stream().forEach((connectionListener) -> {
            connectionListener.onConnectionConnect();
        });
    }

    public void disconnect() {
        if (mServerCommander != null) {
            try {
                mServerCommander.removeClient(mClient, SystemHelper.getHostname());
            } catch (RemoteException ex) {
                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
            }
            mServerCommander = null;

            mConnectionListeners.stream().forEach((connectionListener) -> {
                connectionListener.onConnectionDisconnect();
            });
        }
    }

    public Client getClient() {
        return mClient;
    }

    public ServerCommander getServerCommander() {
        return mServerCommander;
    }

    public boolean hasJobs() {
        boolean hasJobs = false;

        try {
            hasJobs = mServerCommander.hasJobs();
        } catch (RemoteException ex) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            hasJobs = false;
        }

        return hasJobs;
    }

    public boolean isConnected() {
        return mServerCommander != null;
    }

    public void setClient(Client client) {
        mClient = client;
    }

    public void setServerCommander(ServerCommander serverCommander) {
        mServerCommander = serverCommander;
    }

    private static class ManagerHolder {

        private static final Manager INSTANCE = new Manager();
    }
}
