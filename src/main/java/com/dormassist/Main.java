package com.dormassist;

import com.dormassist.config.Databaseconfig;
import com.dormassist.util.UIHelper;
import com.dormassist.view.LoginFrame;
import com.dormassist.view.SetupDialog;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Look & Feel: ưu tiên FlatLaf nếu dependency có trong Maven, fallback an toàn khi chạy bằng JDK thuần.
        try {
            Class<?> flatLight = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flatLight.getMethod("setup").invoke(null);
        } catch (Exception flatUnavailable) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
                catch (Exception ignored) {}
            }
        }

        UIHelper.applyGlobalUI();

        SwingUtilities.invokeLater(() -> {
            if (!Databaseconfig.isConfigured()) {
                SetupDialog setup = new SetupDialog(null);
                setup.setVisible(true);
                if (!setup.isConfirmed()) System.exit(0);
            }
            new LoginFrame().setVisible(true);
        });
    }
}
