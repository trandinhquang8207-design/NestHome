package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.TokenDAO;
import com.dormassist.dao.UserDAO;
import com.dormassist.dao.BillDAO;
import com.dormassist.model.Token;
import com.dormassist.model.User;
import com.dormassist.model.PriceConfig;
import com.dormassist.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;

public class UserPanel extends JPanel {
    private final UserDAO dao = new UserDAO();
    private JTable table;
    private List<User> allData;
    private JButton editBtn, toggleBtn, pwBtn;

    public UserPanel() {
        setLayout(new BorderLayout()); setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20)); buildUI(); loadData();
    }

    private void buildUI() {
        JPanel tb = new JPanel(new BorderLayout()); tb.setOpaque(false); tb.setBorder(new EmptyBorder(0, 0, 14, 0));
        tb.add(UIHelper.sectionTitle("Quản lý tài khoản người dùng"), BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); right.setOpaque(false);

        editBtn   = UIHelper.infoButton("Chỉnh sửa");
        toggleBtn = UIHelper.warningButton("Khóa / Mở khóa");
        pwBtn     = UIHelper.grayButton("Đổi mật khẩu");
        JButton addBtn = UIHelper.primaryButton("+ Tạo tài khoản");
        editBtn.addActionListener(e -> editSelected());
        toggleBtn.addActionListener(e -> toggleSelected());
        pwBtn.addActionListener(e -> changePw());
        addBtn.addActionListener(e -> showCreateDialog());
        editBtn.setEnabled(false); toggleBtn.setEnabled(false); pwBtn.setEnabled(false);

        JButton refreshBtn = UIHelper.grayButton("Làm mới"); refreshBtn.addActionListener(e -> loadData());
        right.add(refreshBtn); right.add(pwBtn); right.add(toggleBtn); right.add(editBtn); right.add(addBtn);
        tb.add(right, BorderLayout.EAST); add(tb, BorderLayout.NORTH);

        String[] cols = {"ID", "Tên ĐN", "Họ và tên", "Vai trò", "Email", "SĐT", "Trạng thái", "Đăng nhập cuối"};
        table = UIHelper.styledTable(cols);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = table.getSelectedRow() >= 0;
            editBtn.setEnabled(sel); toggleBtn.setEnabled(sel); pwBtn.setEnabled(sel);
        });
        table.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseClicked(java.awt.event.MouseEvent e){ if(e.getClickCount()==2) editSelected(); }
        });
        add(UIHelper.tableScroll(table), BorderLayout.CENTER);
    }

    private void loadData() { UIHelper.runAsync(dao::getAll, data -> { allData = data; renderTable(allData); }); }
    private void renderTable(List<User> list) {
        UIHelper.clearTable(table);
        for (User u : list)
            UIHelper.addRow(table, u.getId(), u.getUsername(), u.getFullName(),
                AppConstants.getRoleDisplay(u.getRole()),
                u.getEmail() != null ? u.getEmail() : "",
                u.getPhone() != null ? u.getPhone() : "",
                u.isActive() ? "Hoạt động" : "Đã khóa",
                UIHelper.formatDateTime(u.getLastLogin()));
    }

    private void editSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        User u = dao.getById((int)table.getValueAt(row, 0)); if (u == null) return;
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Chỉnh sửa tài khoản", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 320); dlg.setLocationRelativeTo(this);
        JPanel main = new JPanel(new BorderLayout()); main.setBackground(AppConstants.BG_MAIN);
        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(7,6,7,6); g.fill = GridBagConstraints.HORIZONTAL;
        JTextField nameF = UIHelper.styledField(); nameF.setText(u.getFullName());
        JTextField emailF = UIHelper.styledField(); emailF.setText(u.getEmail() != null ? u.getEmail() : "");
        JTextField phoneF = UIHelper.styledField(); phoneF.setText(u.getPhone() != null ? u.getPhone() : "");
        String[] roleVN = {"Quản trị viên","Trưởng tầng","Trưởng phòng","Sinh viên"};
        String[] roleCode = {AppConstants.ROLE_ADMIN_SUPER,AppConstants.ROLE_BASE1,AppConstants.ROLE_BASE2,AppConstants.ROLE_BASE3};
        JComboBox<String> roleCb = UIHelper.styledCombo(roleVN);
        for (int i=0;i<roleCode.length;i++) if (roleCode[i].equals(u.getRole())) { roleCb.setSelectedIndex(i); break; }
        UIHelper.addFormRow(form,g,0,"Họ và tên:",nameF); UIHelper.addFormRow(form,g,1,"Email:",emailF);
        UIHelper.addFormRow(form,g,2,"SĐT:",phoneF); UIHelper.addFormRow(form,g,3,"Vai trò:",roleCb);
        main.add(form, BorderLayout.CENTER);
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,12)); btnP.setBackground(AppConstants.BG_MAIN);
        JButton cancelBtn = UIHelper.grayButton("Hủy"); cancelBtn.addActionListener(e->dlg.dispose());
        JButton saveBtn = UIHelper.primaryButton("Lưu");
        saveBtn.addActionListener(e -> {
            u.setFullName(nameF.getText().trim()); u.setEmail(emailF.getText().trim());
            u.setPhone(phoneF.getText().trim()); u.setRole(roleCode[roleCb.getSelectedIndex()]);
            if (dao.update(u)) { UIHelper.showSuccess(dlg,"Đã cập nhật!"); dlg.dispose(); loadData(); }
            else UIHelper.showError(dlg, "Thất bại!");
        });
        btnP.add(cancelBtn); btnP.add(saveBtn);
        main.add(btnP, BorderLayout.SOUTH); dlg.setContentPane(main); dlg.setVisible(true);
    }

    private void toggleSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int)table.getValueAt(row,0);
        boolean active = "Hoạt động".equals(table.getValueAt(row,6));
        String msg = active ? "Khóa tài khoản này?" : "Mở khóa tài khoản này?";
        if (!UIHelper.confirm(this, msg)) return;
        if (dao.setActive(id, !active)) { UIHelper.showSuccess(this, active?"Đã khóa!":"Đã mở khóa!"); loadData(); }
        else UIHelper.showError(this, "Thất bại!");
    }

    private void changePw() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int)table.getValueAt(row,0);
        JPasswordField pf = UIHelper.styledPasswordField(), cf = UIHelper.styledPasswordField();
        JPanel p = new JPanel(new GridLayout(4,1,6,6));
        p.add(UIHelper.label("Mật khẩu mới:")); p.add(pf);
        p.add(UIHelper.label("Xác nhận:")); p.add(cf);
        if (JOptionPane.showConfirmDialog(this,p,"Đổi mật khẩu",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;
        String pw1=new String(pf.getPassword()), pw2=new String(cf.getPassword());
        if (!pw1.equals(pw2)){ UIHelper.showError(this,"Mật khẩu không khớp!"); return; }
        if (pw1.length()<6){ UIHelper.showWarning(this,"Mật khẩu phải >= 6 ký tự!"); return; }
        if (dao.changePassword(id,pw1)) UIHelper.showSuccess(this,"Đã đổi mật khẩu!");
        else UIHelper.showError(this,"Thất bại!");
    }

    private void showCreateDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),"Tạo tài khoản mới",Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(440,360); dlg.setLocationRelativeTo(this);
        JPanel main = new JPanel(new BorderLayout()); main.setBackground(AppConstants.BG_MAIN);
        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(20,24,20,24));
        GridBagConstraints g = new GridBagConstraints(); g.insets=new Insets(7,6,7,6); g.fill=GridBagConstraints.HORIZONTAL;
        JTextField unF=UIHelper.styledField(), nameF=UIHelper.styledField(), emailF=UIHelper.styledField(), phoneF=UIHelper.styledField();
        JPasswordField pwF=UIHelper.styledPasswordField();
        String[] roleVN={"Quản trị viên","Trưởng tầng","Trưởng phòng","Sinh viên"};
        String[] roleCode={AppConstants.ROLE_ADMIN_SUPER,AppConstants.ROLE_BASE1,AppConstants.ROLE_BASE2,AppConstants.ROLE_BASE3};
        JComboBox<String> roleCb=UIHelper.styledCombo(roleVN);
        UIHelper.addFormRow(form,g,0,"Tên đăng nhập: *",unF); UIHelper.addFormRow(form,g,1,"Mật khẩu: *",pwF);
        UIHelper.addFormRow(form,g,2,"Họ và tên: *",nameF); UIHelper.addFormRow(form,g,3,"Vai trò:",roleCb);
        UIHelper.addFormRow(form,g,4,"Email:",emailF); UIHelper.addFormRow(form,g,5,"SĐT:",phoneF);
        main.add(form,BorderLayout.CENTER);
        JPanel btnP=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,12)); btnP.setBackground(AppConstants.BG_MAIN);
        JButton cancelBtn=UIHelper.grayButton("Hủy"); cancelBtn.addActionListener(e->dlg.dispose());
        JButton saveBtn=UIHelper.primaryButton("Tạo tài khoản");
        saveBtn.addActionListener(e -> {
            if (unF.getText().trim().isEmpty()||nameF.getText().trim().isEmpty()){ UIHelper.showWarning(dlg,"Điền đầy đủ!"); return; }
            if (dao.usernameExists(unF.getText().trim())){ UIHelper.showError(dlg,"Tên đăng nhập đã tồn tại!"); return; }
            boolean ok=dao.createUser(unF.getText().trim(),new String(pwF.getPassword()),nameF.getText().trim(),emailF.getText().trim(),phoneF.getText().trim(),roleCode[roleCb.getSelectedIndex()]);
            if (ok){ UIHelper.showSuccess(dlg,"Đã tạo!"); dlg.dispose(); loadData(); }
            else UIHelper.showError(dlg,"Thất bại!");
        });
        btnP.add(cancelBtn); btnP.add(saveBtn); main.add(btnP,BorderLayout.SOUTH); dlg.setContentPane(main); dlg.setVisible(true);
    }
}
