package com.tambapps.p2p.peer_transfer.desktop.model

import com.tambapps.p2p.fandem.FileSharer
import com.tambapps.p2p.fandem.Peer
import com.tambapps.p2p.fandem.listener.ReceivingListener
import com.tambapps.p2p.fandem.listener.TransferListener

import javax.swing.AbstractListModel

class TaskListModel extends AbstractListModel<TaskHolder> {

    final FileSharer sharer = new FileSharer(4)
    List<TaskHolder> tasks = new ArrayList<>(4)

    void addElement(File directory, Peer peer) { //receive
        final TaskHolder holder = new TaskHolder(peer: peer, directory: directory)
        tasks.add(holder)
        holder.future = sharer.receiveFileInDirectory(directory, peer,
                new ReceivingListener() {

                    @Override
                    void onEnd(File file) {
                        holder.file = file
                    }

                    @Override
                    void onConnected(Peer selfPeer, Peer remotePeer, String fileName, long fileSize) {
                        holder.peer = remotePeer
                        holder.fileName = fileName
                        holder.totalBytes = fileSize
                        holder.connected = true
                        fireContentsChanged()
                    }

                    @Override
                    void onProgressUpdate(int percentage, long bytesTransferred, long totalBytes) {
                        holder.bytesTransferred = bytesTransferred
                        holder.progress = percentage
                        fireContentsChanged()
                    }
                }, {IOException e -> holder.error = e; fireContentsChanged() })
        fireContentsChanged()
    }

    void addElement(Peer peer, File file) { //send
        final TaskHolder holder = new TaskHolder(fileName: file.name, peer: peer, sender: true)
        TransferListener transferListener = new TransferListener() {

            @Override
            void onConnected(Peer selfPeer, Peer remotePeer, String fileName, long fileSize) {
                holder.connected = true
                holder.remotePeer = remotePeer
            }

            @Override
            void onProgressUpdate(int percentage, long bytesTransferred, long fileSize) {
                holder.bytesTransferred = bytesTransferred
                holder.progress = percentage
                fireContentsChanged()
            }
        }
        tasks.add(holder)
        holder.future = sharer.sendFile(file, peer, transferListener, {IOException e -> holder.error = e; fireContentsChanged() })
        fireContentsChanged()
    }

    void removeElement(int i) {
        tasks.remove(i)
        fireIntervalRemoved(this, i, i)
    }

    void fireContentsChanged() {
        this.fireContentsChanged(this, 0, getSize())
    }

    void fireContentsChanged(int i) {
        this.fireContentsChanged(this, i, i)
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
