package com.dormassist.view.ui;

import com.dormassist.util.UIHelper;
import javax.swing.*;

/** JTable mặc định có sort, row height, style NestHome. */
public class ModernTable extends JTable {
    public ModernTable() {
        super();
        UIHelper.styleTable(this);
    }
}
