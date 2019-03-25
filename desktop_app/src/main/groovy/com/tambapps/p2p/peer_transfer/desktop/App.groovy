package com.tambapps.p2p.peer_transfer.desktop

import static java.awt.Component.CENTER_ALIGNMENT
import static javax.swing.JFrame.EXIT_ON_CLOSE

import com.tambapps.p2p.fandem.Peer
import com.tambapps.p2p.fandem.util.IPUtils

import com.tambapps.p2p.peer_transfer.desktop.model.ReceiveData
import com.tambapps.p2p.peer_transfer.desktop.model.SendData
import com.tambapps.p2p.peer_transfer.desktop.style.Colors
import com.tambapps.p2p.peer_transfer.desktop.style.Fonts
import com.tambapps.p2p.peer_transfer.desktop.view.list.TaskList
import com.tambapps.p2p.peer_transfer.desktop.model.TaskListModel
import com.tambapps.p2p.peer_transfer.desktop.view.panel.EmptyPanel
import com.tambapps.p2p.peer_transfer.desktop.view.panel.GradiantPanel
import com.tambapps.p2p.peer_transfer.desktop.view.panel.IPPanel
import com.tambapps.p2p.peer_transfer.desktop.view.text.NumberDocument

import groovy.swing.SwingBuilder

import javax.swing.ImageIcon
import javax.swing.JFileChooser
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JTextField
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent

final int MAX_TRANSFERS = 4

def builder = new SwingBuilder()
builder.registerBeanFactory( "gradiantPanel", GradiantPanel)
builder.registerBeanFactory( "emptyPanel", EmptyPanel)
builder.registerBeanFactory("ipPanel", IPPanel)
builder.registerBeanFactory("taskList", TaskList)
final int WIDTH = 1280
final int HEIGHT = 720

TaskListModel sharingTasks = new TaskListModel()

def taskBar = {
    builder.taskList(model: sharingTasks, constraints: BorderLayout.SOUTH, preferredSize: [WIDTH, HEIGHT / 5 as int], background: Colors.GRADIANT_BOTTOM)
}
def header = {
    builder.panel(layout: new GridLayout(2, 2), constraints : BorderLayout.NORTH, background: Colors.GRADIANT_TOP) {
        builder.emptyPanel()
        builder.emptyPanel()
        builder.label('Send file', font: Fonts.TITLE_FONT, horizontalAlignment: JLabel.CENTER, foreground: Colors.TEXT_COLOR)
        builder.label('Receive file', font: Fonts.TITLE_FONT, horizontalAlignment: JLabel.CENTER, foreground: Colors.TEXT_COLOR)
    }
}
def sendView = {
    final SendData sendData = new SendData()
    builder.vbox() {
        builder.emptyPanel()
        builder.emptyPanel()
        builder.hbox(background: null) {
            builder.emptyPanel()
            builder.button('Pick file', actionPerformed: { ActionEvent e ->
                JFileChooser chooser = builder.fileChooser()
                if (chooser.showOpenDialog(e.source) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile()
                    sendData.file = file
                    fileText.text = 'File: ' + file.name
                }
            })
            builder.emptyPanel()
            builder.label(id: 'fileText', maximumSize: [Integer.MAX_VALUE, 40])
            builder.emptyPanel()

        }
        builder.emptyPanel()

        builder.button(text: 'Send', alignmentX: CENTER_ALIGNMENT, actionPerformed: {
            if (sharingTasks.size >= MAX_TRANSFERS) {
                JOptionPane.showMessageDialog(frame,
                    "You cannot have more than $MAX_TRANSFERS tasks at the same time",
                    "Maximum tasks reached",
                    JOptionPane.WARNING_MESSAGE)
                return
            }
            String error = sendData.checkErrors()
            if (error) {
                JOptionPane.showMessageDialog(frame,
                    error,
                    'Invalid input',
                    JOptionPane.WARNING_MESSAGE)
            } else {
                Peer peer
                try {
                    peer = IPUtils.availablePeer
                } catch (SocketException e) {
                    JOptionPane.showMessageDialog(frame,
                        "Error while trying to create peer: $e.message",
                        'Network error',
                        JOptionPane.ERROR_MESSAGE)
                    return
                }
                sharingTasks.addElement(peer, sendData.file)
                JOptionPane.showMessageDialog(frame,
                    'Sending task was started. Click on the task to cancel it',
                    'Started sending task',
                    JOptionPane.INFORMATION_MESSAGE)
                sendData.clear()
                fileText.text = ""
            }
        })
        builder.emptyPanel()

    }
}
def receiveView = {
    final ReceiveData receiveData = new ReceiveData()
    builder.vbox() {
        builder.emptyPanel()
        builder.emptyPanel()
        builder.hbox(maximumSize: [WIDTH, HEIGHT >> 4], background: null) {
            builder.emptyPanel()
            builder.label(text: 'Peer address: ', foreground: Colors.TEXT_COLOR, font: Fonts.TEXT_FONT)
            for (int i = 0; i < 5; i++) {
                final position = i
                NumberDocument document
                if (i == 4) { //port
                    document  = new NumberDocument(5)
                    document.addChangeListener {String text -> receiveData.port = text ? Integer.parseInt(text) : 0 }
                } else {
                    document  = new NumberDocument(3)
                    document.addChangeListener {String text -> receiveData.ipFields[position] = text }
                }
                builder.textField(id: "pField$i", opaque: false, horizontalAlignment: JTextField.CENTER, document: document, font: Fonts.TEXT_FONT, foreground: Colors.TEXT_COLOR)
                if (i < 3) {
                    builder.label('.', maximumSize: [50, 100], horizontalAlignment: JTextField.CENTER, foreground: Colors.TEXT_COLOR, font: Fonts.TITLE_FONT)
                } else if (i == 3) {
                    builder.label(':', maximumSize: [50, 100], horizontalAlignment: JTextField.CENTER, foreground: Colors.TEXT_COLOR, font:  Fonts.TITLE_FONT)
                }
            }
            builder.emptyPanel()
        }
        builder.emptyPanel()
        builder.hbox(background: null) {
            builder.emptyPanel()
            builder.button('Pick folder', actionPerformed: { ActionEvent e ->
                JFileChooser chooser = builder.fileChooser(fileSelectionMode: JFileChooser.DIRECTORIES_ONLY)
                if (chooser.showOpenDialog(e.source) == JFileChooser.APPROVE_OPTION) {
                    File folder = chooser.getSelectedFile()
                    receiveData.folder = folder
                    folderText.text = 'Folder: ' + folder.name
                }
            })
            builder.emptyPanel()
            builder.label(id: 'folderText', maximumSize: [Integer.MAX_VALUE, 40])
            builder.emptyPanel()

        }
        builder.emptyPanel()

        builder.button(text: 'Receive', alignmentX: CENTER_ALIGNMENT, actionPerformed: {
            if (sharingTasks.size >= MAX_TRANSFERS) {
                JOptionPane.showMessageDialog(frame,
                    "You cannot have more than $MAX_TRANSFERS tasks at the same time",
                    "Maximum tasks reached",
                    JOptionPane.WARNING_MESSAGE)
                return
            }
            String error = receiveData.checkErrors()
            if (error) {
                JOptionPane.showMessageDialog(frame,
                    error,
                    'Invalid input',
                    JOptionPane.WARNING_MESSAGE)
            } else {
                sharingTasks.addElement(receiveData.folder, Peer.of(receiveData.ip, receiveData.port))
                JOptionPane.showMessageDialog(frame,
                    'Receiving task was started. Click on the task to cancel it',
                    'Started sending task',
                    JOptionPane.INFORMATION_MESSAGE)
                sendData.clear()
                receiveData.clear()
                for (int i = 0; i < 5; i++) {
                    getProperty("pField$i").text = ''
                }
            }
        })
        builder.emptyPanel()

    }
}
builder.edt {
    frame(title: 'Fandem: P2P File Sharing', size: [WIDTH, HEIGHT], show: true, id: 'frame',
            defaultCloseOperation: EXIT_ON_CLOSE,
    iconImage: new ImageIcon(App.getResource('/appicon.png')).image) {
        panel(layout : new BorderLayout()) {
            header()
            gradiantPanel(layout: new GridLayout(1, 2), constraints : BorderLayout.CENTER) {
                sendView()
                receiveView()
            }
            taskBar()
        }
    }
}