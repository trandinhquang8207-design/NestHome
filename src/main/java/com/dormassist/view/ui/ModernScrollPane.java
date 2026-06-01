package com.dormassist.view.ui;

import com.dormassist.util.UIHelper;
import javax.swing.*;
import java.awt.*;

/** JScrollPane đã cấu hình tốc độ cuộn và thanh cuộn bo góc. */
public class ModernScrollPane extends JScrollPane {
    public ModernScrollPane(Component view) {
        super(view);
        UIHelper.smoothScroll(this);
    }
}
