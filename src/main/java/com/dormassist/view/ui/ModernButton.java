package com.dormassist.view.ui;

import com.dormassist.util.UIHelper;
import javax.swing.*;

/** Factory nút dùng chung cho giao diện NestHome. */
public final class ModernButton {
    private ModernButton() {}
    public static JButton primary(String text) { return UIHelper.primaryButton(text); }
    public static JButton secondary(String text) { return UIHelper.grayButton(text); }
    public static JButton danger(String text) { return UIHelper.dangerButton(text); }
    public static JButton outline(String text) { return UIHelper.outlineButton(text); }
}
