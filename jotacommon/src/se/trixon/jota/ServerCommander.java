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

//    JotaManager createJotaManager() throws RemoteException;
    void dirHome() throws RemoteException;

    void displayStatus() throws RemoteException;

    void setCronActive(boolean enable) throws RemoteException;

    VMID getVMID() throws RemoteException;

//    boolean isShowing() throws RemoteException;
//    ServerOptions loadServerOptions() throws RemoteException;
    void registerClient(ClientCallbacks clientCallback, String hostname) throws RemoteException;

    void removeClient(ClientCallbacks clientCallback, String hostname) throws RemoteException;

//    void saveServerOptions(ServerOptions serverOptions) throws RemoteException;
//    void setShowing(boolean showing) throws RemoteException;
    void shutdown() throws RemoteException;
}