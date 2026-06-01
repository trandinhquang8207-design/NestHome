package com.dormassist.view.ui;

import com.dormassist.config.AppConstants;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Card trắng kem bo góc, dùng được cho các màn hình mới. */
public class CardPanel extends JPanel {
    public CardPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16,16,18,18));
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0,0,0,12));
        g2.fill(new RoundRectangle2D.Float(2,3,getWidth()-5,getHeight()-5,AppConstants.RADIUS_CARD*2,AppConstants.RADIUS_CARD*2));
        g2.setColor(AppConstants.BG_CARD);
        g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-4,getHeight()-4,AppConstants.RADIUS_CARD*2,AppConstants.RADIUS_CARD*2));
        g2.setColor(AppConstants.BORDER);
        g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-5,getHeight()-5,AppConstants.RADIUS_CARD*2,AppConstants.RADIUS_CARD*2));
        g2.dispose();
        super.paintComponent(g);
    }
}
