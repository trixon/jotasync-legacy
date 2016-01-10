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
package se.trixon.jota.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.dgc.VMID;
import java.util.LinkedList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import se.trixon.jota.shared.job.Job;

/**
 *
 * @author Patrik Karlsson
 */
public interface ServerCommander extends Remote {

    Job getJob(long jobId) throws RemoteException;

    LinkedList<Job> getJobs() throws RemoteException;

    String getLogDir() throws RemoteException;

    String getRsyncPath() throws RemoteException;

    long getSpeedDial(int key) throws RemoteException;

    String getStatus() throws RemoteException;

    VMID getVMID() throws RemoteException;

    boolean hasJobs() throws RemoteException;

    boolean isCronActive() throws RemoteException;

    boolean isRunning(Job job) throws RemoteException;

    String listJobs() throws RemoteException;

    String listTasks() throws RemoteException;

    DefaultComboBoxModel populateJobModel(DefaultComboBoxModel model) throws RemoteException;

    DefaultListModel populateJobModel(DefaultListModel model) throws RemoteException;

    DefaultListModel populateTaskModel(DefaultListModel model) throws RemoteException;

    void registerClient(ClientCallbacks clientCallback, String hostname) throws RemoteException;

    void removeClient(ClientCallbacks clientCallback, String hostname) throws RemoteException;

    void saveJota() throws RemoteException;

    void setCronActive(boolean enable) throws RemoteException;

    void setJobs(DefaultListModel model) throws RemoteException;

    void setLogDir(String path) throws RemoteException;

    void setRsyncPath(String path) throws RemoteException;

    void setSpeedDial(int key, long jobId) throws RemoteException;

    void setTasks(DefaultListModel model) throws RemoteException;

    void shutdown() throws RemoteException;

    void startJob(Job job) throws RemoteException;

    void stopJob(Job job) throws RemoteException;

}
