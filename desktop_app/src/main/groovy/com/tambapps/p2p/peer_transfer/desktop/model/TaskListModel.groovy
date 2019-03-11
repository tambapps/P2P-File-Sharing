package com.tambapps.p2p.peer_transfer.desktop.model

import com.tambapps.p2p.file_sharing.TransferListener
import com.tambapps.p2p.peer_transfer.desktop.model.TaskHolder

import javax.swing.AbstractListModel

class TaskListModel extends AbstractListModel<TaskHolder> {

    List<TaskHolder> tasks = new ArrayList<>(4)

    void addElement(final TaskHolder task) {
        tasks.add(task)
        final int index = tasks.size() - 1
        task.task.transferListener = new TransferListener() {
            @Override
            void onConnected(String remoteAddress, int remotePort, String fileName, long fileSize) {
                task.remotePeer = remoteAddress + ":$remotePort"
                task.fileName = fileName
                task.totalBytes = fileSize
                fireContentsChanged(index)
            }

            @Override
            void onProgressUpdate(int progress, long bytesTransferred, long totalBytes) {
                task.bytesTransferred = bytesTransferred
                task.progress = progress
                fireContentsChanged(index)
            }
        }
        fireContentsChanged(index)
    }

    void removeElement(int i) {
        TaskHolder task = tasks.remove(i)
        task.task.transferListener = null
    }

    void removeElement(TaskHolder task) {
        task.task.transferListener = null
        tasks.remove(task)
    }

    private void fireContentsChanged(int index) {
        this.fireContentsChanged(this, index, index)
    }
    @Override
    int getSize() {
        return tasks.size()
    }

    @Override
    TaskHolder getElementAt(int i) {
        return tasks.get(i)
    }
}
