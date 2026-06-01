package com.dormassist.view;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Databaseconfig;
import com.dormassist.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Chỉ hiển thị lần đầu khi chưa có file db.properties */
public class SetupDialog extends JDialog {
    private boolean confirmed = false;
    private JTextField hostF, portF, dbF, userF;
    private JPasswordField passF;

    public SetupDialog(Frame parent) {
        super(parent, "Cài đặt kết nối cơ sở dữ liệu", true);
        setSize(460, 360);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppConstants.BG_MAIN);

        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        hdr.setBackground(AppConstants.PRIMARY);
        JPanel htxt = new JPanel(); htxt.setOpaque(false); htxt.setLayout(new BoxLayout(htxt, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Cài đặt lần đầu - Kết nối SQL Server");
        t.setFont(AppConstants.FONT_HEADER); t.setForeground(Color.WHITE);
        JLabel s = new JLabel("Nhập thông tin kết nối cơ sở dữ liệu SQL Server");
        s.setFont(AppConstants.FONT_SMALL); s.setForeground(new Color(200, 220, 255));
        htxt.add(t); htxt.add(s); hdr.add(htxt);
        root.add(hdr, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(20, 30, 10, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 6, 7, 6); g.fill = GridBagConstraints.HORIZONTAL;

        hostF = UIHelper.styledField(); hostF.setText(Databaseconfig.getHost());
        portF = UIHelper.styledField(); portF.setText(Databaseconfig.getPort());
        dbF   = UIHelper.styledField(); dbF.setText(Databaseconfig.getDbName());
        userF = UIHelper.styledField(); userF.setText(Databaseconfig.getUser());
        passF = UIHelper.styledPasswordField();

        UIHelper.addFormRow(form, g, 0, "Host:",           hostF);
        UIHelper.addFormRow(form, g, 1, "Port:",           portF);
        UIHelper.addFormRow(form, g, 2, "Tên cơ sở dữ liệu:", dbF);
        UIHelper.addFormRow(form, g, 3, "Tên đăng nhập:",  userF);
        UIHelper.addFormRow(form, g, 4, "Mật khẩu:",       passF);

        JScrollPane sp = UIHelper.smoothScroll(new JScrollPane(form)); sp.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        root.add(sp, BorderLayout.CENTER);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnP.setBackground(AppConstants.BG_MAIN);
        btnP.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppConstants.BORDER));

        JButton testBtn = UIHelper.grayButton("Kiểm tra kết nối");
        JButton saveBtn = UIHelper.primaryButton("Lưu và tiếp tục");
        JButton cancelBtn = UIHelper.dangerButton("Thoát");

        testBtn.addActionListener(e -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            boolean ok = Databaseconfig.testConnection(hostF.getText().trim(), portF.getText().trim(),
                dbF.getText().trim(), userF.getText().trim(), new String(passF.getPassword()));
            setCursor(Cursor.getDefaultCursor());
            if (ok) UIHelper.showSuccess(this, "Kết nối thành công!");
            else UIHelper.showError(this, "Không thể kết nối!\nKiểm tra lại thông tin và đảm bảo SQL Server đang chạy.");
        });
        saveBtn.addActionListener(e -> {
            if (hostF.getText().trim().isEmpty() || dbF.getText().trim().isEmpty()) {
                UIHelper.showWarning(this, "Điền đầy đủ thông tin!"); return;
            }
            Databaseconfig.saveConfig(hostF.getText().trim(), portF.getText().trim(),
                dbF.getText().trim(), userF.getText().trim(), new String(passF.getPassword()));
            confirmed = true;
            UIHelper.showSuccess(this, "Đã lưu cấu hình!");
            dispose();
        });
        cancelBtn.addActionListener(e -> dispose());

        btnP.add(cancelBtn); btnP.add(testBtn); btnP.add(saveBtn);
        root.add(btnP, BorderLayout.SOUTH);
        setContentPane(root);
    }

    public boolean isConfirmed() { return confirmed; }
}
