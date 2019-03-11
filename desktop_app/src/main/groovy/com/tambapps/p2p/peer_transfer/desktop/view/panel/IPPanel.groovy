package com.tambapps.p2p.peer_transfer.desktop.view.panel


import javax.swing.JPanel
import javax.swing.JTextField
import java.awt.Color
import java.awt.Dimension

class IPPanel extends JPanel {

    private JTextField[] ipFields = new JTextField[4]
    IPPanel() {

        final Dimension minDim = new Dimension(280, 180)
        for (int i = 0; i < ipFields.length; i++) {
            JTextField textField = new JTextField()
            textField.setMinimumSize(minDim)
            add(textField)
            ipFields[i] = textField
            if (i < ipFields.length - 1) {
                JTextField dot = new JTextField('.')
                dot.background = new Color(0,0,0,0)
                add(dot)
            }
        }
    }
}
