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
package se.trixon.jota;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.dgc.VMID;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public interface ServerCommander extends Remote {

    void dirHome() throws RemoteException;

    String getRsyncPath() throws RemoteException;

    long getSpeedDial(int key) throws RemoteException;

    String getStatus() throws RemoteException;

    VMID getVMID() throws RemoteException;

    boolean isCronActive() throws RemoteException;

    void registerClient(ClientCallbacks clientCallback, String hostname) throws RemoteException;

    void removeClient(ClientCallbacks clientCallback, String hostname) throws RemoteException;

    void setCronActive(boolean enable) throws RemoteException;

    void setRsyncPath(String path) throws RemoteException;

    void setSpeedDial(int key, long jobId) throws RemoteException;

    void shutdown() throws RemoteException;
}
