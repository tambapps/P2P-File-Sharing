package com.tambapps.p2p.peer_transfer.desktop.view.list

import com.tambapps.p2p.fandem.util.FileUtils
import com.tambapps.p2p.peer_transfer.desktop.model.TaskHolder
import com.tambapps.p2p.peer_transfer.desktop.model.TaskListModel
import com.tambapps.p2p.peer_transfer.desktop.style.Colors
import groovy.swing.SwingBuilder

import javax.swing.BorderFactory
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.ListModel
import java.awt.Color
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class TaskList extends JList<TaskHolder> implements ListCellRenderer<TaskHolder> {

    TaskList() {
        cellRenderer = this
        addMouseListener(new MouseAdapter() {
            @Override
            void mouseClicked(MouseEvent mouseEvent) {
                int index = locationToIndex(mouseEvent.point)
                clickedItem(index)
            }
        })
    }

    void clickedItem(int i) {
        TaskHolder holder = model.getElementAt(i)
        if (holder.canceled || holder.error || holder.progress == 100) { //if it's finished, remove
            model.removeElement(i)
        } else { //cancel
            holder.cancel()
        }
        fireContentsChanged(i)
    }

    @Override
    Component getListCellRendererComponent(JList<? extends TaskHolder> jList, TaskHolder holder, int i, boolean b, boolean b1) {
        SwingBuilder builder = new SwingBuilder()
        Color borderColor = i < model.size - 1 ? Colors.BORDER : Colors.GRADIANT_BOTTOM

       return builder.panel(background: Colors.GRADIANT_BOTTOM,
           border: BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor)) {
           if (holder.error) {
               builder.label(text: "$holder.header An error occured: $holder.error.message. Click to close")
               return this
           }

           if (holder.canceled) {
               builder.label(text: "$holder.header Task was canceled. Click to close")
               return this
           }

           if (!holder.connected) {
               if (holder.sender) {
                   builder.label(text: "$holder.header Waiting for other peer to connect on $holder.peer...")
               } else {
                   builder.label(text: "$holder.header Connecting to $holder.peer...")
               }
               return this
           }

           if (holder.progress < 100) {
               builder.vbox() {
                   String text
                   if (holder.sender) {
                       text = "Sending $holder.fileName to $holder.remotePeer"
                   } else {
                       text = "Receiving $holder.fileName from $holder.peer"
                   }
                   builder.label(text: text)
                   if (!holder.sender) {
                       builder.label(text: "File name: $holder.fileName")
                   }
               }
               String progressString = FileUtils.bytesToString(holder.bytesTransferred) + ' / ' + FileUtils.bytesToString(holder.totalBytes)
               builder.progressBar(value: holder.progress, stringPainted: true, string: progressString, maximum: holder.totalBytes)
           } else {
               builder.label(text: "$holder.fileName was successfully " + (holder.sender ? 'sent' : 'received') + '. Click to close')
           }
        }
    }

    private void fireContentsChanged(int i) {
        model.fireContentsChanged(i)
    }

}
