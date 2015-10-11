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

import java.util.HashSet;
import se.trixon.jota.ServerCommander;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class Manager {

    private ServerCommander mServerCommander;
    private final HashSet<ConnectionListener> mConnectionListeners = new HashSet<>();
private Client mClient;

    public Client getClient() {
        return mClient;
    }

    public void setClient(Client client) {
       mClient = client;
    }

    private Manager() {
    }
    public boolean addConnectionListeners(ConnectionListener connectionListener) {
        return mConnectionListeners.add(connectionListener);
    }

    public ServerCommander getServerCommander() {
        return mServerCommander;
    }

    public void setServerCommander(ServerCommander serverCommander) {
        mServerCommander = serverCommander;
    }

    public static Manager getInstance() {
        return ManagerHolder.INSTANCE;
    }
    public boolean isConnected() {
        return mServerCommander != null;
    }

    void connected() {
        mConnectionListeners.stream().forEach((connectionListener) -> {
            connectionListener.onConnectionClientConnect();
        });
    }

    private static class ManagerHolder {

        private static final Manager INSTANCE = new Manager();
    }
}
