package com.dormassist.view.ui;

import com.dormassist.config.AppConstants;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Thông báo nhẹ dùng thay cho popup ở các thao tác nhỏ. */
public final class Toast {
    private Toast() {}
    public static void show(Component parent, String message) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog d = new JDialog(owner);
        d.setUndecorated(true);
        JLabel label = new JLabel(message);
        label.setFont(AppConstants.FONT_BOLD);
        label.setForeground(Color.WHITE);
        label.setBorder(new EmptyBorder(10,16,10,16));
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppConstants.PRIMARY_DARK);
        p.add(label, BorderLayout.CENTER);
        d.setContentPane(p);
        d.pack();
        d.setLocationRelativeTo(parent);
        Timer timer = new Timer(1600, e -> d.dispose());
        timer.setRepeats(false);
        timer.start();
        d.setVisible(true);
    }
}
