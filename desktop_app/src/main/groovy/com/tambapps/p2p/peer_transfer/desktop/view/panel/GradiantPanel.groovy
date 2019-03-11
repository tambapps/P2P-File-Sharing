package com.tambapps.p2p.peer_transfer.desktop.view.panel

import com.tambapps.p2p.peer_transfer.desktop.style.Colors

import javax.swing.JPanel
import java.awt.Color
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints

class GradiantPanel extends JPanel {

    Color topColor = Colors.GRADIANT_TOP
    Color bottomColor = Colors.GRADIANT_BOTTOM

    /*
    @Override
    void paint(Graphics g) {
        //Graphics2D instance
        super.paint(g)
        Graphics2D graphics = (Graphics2D) g
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        int w = getWidth()
        int h = getHeight()
        GradientPaint gp = new GradientPaint(0, 0, topColor, 0, h, bottomColor, true)
        graphics.setPaint(gp)
        graphics.fillRect(0, 0, w, h)
    }

    */

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g)
        Graphics2D graphics = (Graphics2D) g
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        int w = getWidth()
        int h = getHeight()
        GradientPaint gp = new GradientPaint(0, 0, topColor, 0, h, bottomColor, true)
        graphics.setPaint(gp)
        graphics.fillRect(0, 0, w, h)
    }


}
