package com.tambapps.p2p.peer_transfer.desktop.view.list

import com.tambapps.p2p.file_sharing.TransferListener
import com.tambapps.p2p.peer_transfer.desktop.model.TaskHolder
import com.tambapps.p2p.peer_transfer.desktop.style.Colors
import groovy.swing.SwingBuilder

import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.ListModel
import java.awt.Component

class TaskList extends JList<TaskHolder> implements ListCellRenderer<TaskHolder> {

    TaskList() {
        cellRenderer = this
    }

    @Override
    void setModel(ListModel<TaskHolder> listModel) {
        super.setModel(listModel)
    }

    @Override
    Component getListCellRendererComponent(JList<? extends TaskHolder> jList, TaskHolder holder, int i, boolean b, boolean b1) {
        SwingBuilder builder = new SwingBuilder()
       return builder.panel(background: Colors.GRADIANT_BOTTOM) {
            if (holder.remotePeer == null) {
                String text
                if (holder.sender) {
                    text = "About to send $holder.fileName"
                } else {
                    text = 'About to receive file'
                }
                builder.label(text: text + ', Waiting for other peer to connect...')
                return this
            }
           if (holder.progress < 100) {
               builder.vbox() {
                   String text
                   if (holder.sender) {
                       text = "Sending $holder.fileName to $holder.remotePeer"
                   } else {
                       text = "Receiving $holder.fileName from $holder.remotePeer"
                   }
                   builder.label(text: text)
                   if (!holder.sender) {
                       builder.label(text: "File name: $holder.fileName")
                   }
               }
               String progressString = TransferListener.bytesToString(holder.bytesTransferred) + " / " + TransferListener.bytesToString(holder.totalBytes)
               builder.progressBar(value: holder.progress, stringPainted: true, string: progressString, maximum: holder.totalBytes)
           } else {
               builder.label(text: "$holder.fileName was successfully " + (holder.sender ? "sent" : "received"))
           }
        }
    }

}
