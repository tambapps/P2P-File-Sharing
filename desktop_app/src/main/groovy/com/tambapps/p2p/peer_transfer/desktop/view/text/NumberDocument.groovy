package com.tambapps.p2p.peer_transfer.desktop.view.text

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.PlainDocument

class NumberDocument extends PlainDocument {
    NumberDocument(int charLimit) {
        documentFilter = new NumberDocFilter(charLimit)
    }

    void addChangeListener(Closure closure) {
        addDocumentListener(new DocumentListener() {
            @Override
            void insertUpdate(DocumentEvent documentEvent) {
                closure(getText(0, length))
            }

            @Override
            void removeUpdate(DocumentEvent documentEvent) {
                closure(getText(0, length))
            }

            @Override
            void changedUpdate(DocumentEvent documentEvent) {
                closure(getText(0, length))
            }
        })
    }
}
