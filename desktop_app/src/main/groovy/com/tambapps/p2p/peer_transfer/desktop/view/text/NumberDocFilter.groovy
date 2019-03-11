package com.tambapps.p2p.peer_transfer.desktop.view.text

import javax.swing.text.AttributeSet
import javax.swing.text.BadLocationException
import javax.swing.text.DocumentFilter

class NumberDocFilter extends DocumentFilter {

    private final int charLimit

    NumberDocFilter(int charLimit) {
        this.charLimit = charLimit
    }

    @Override
    void remove(FilterBypass filterBypass, int i, int i1) throws BadLocationException {
        super.remove(filterBypass, i, i1)
    }

    @Override
    void replace(FilterBypass filterBypass, int i, int i1, String s, AttributeSet attributeSet) throws BadLocationException {
       String text = filterBypass.document.getText(0, filterBypass.document.length)
        if (text.length() + i1 < charLimit && s.matches("[0-9]+")) {
            super.replace(filterBypass, i, i1, s, attributeSet)
        }
    }

    @Override
    void insertString(FilterBypass filterBypass, int i, String s, AttributeSet attributeSet) throws BadLocationException {
        if (s.matches("[0-9]+")) {
            super.insertString(filterBypass, i, s, attributeSet)
        }
    }
}
