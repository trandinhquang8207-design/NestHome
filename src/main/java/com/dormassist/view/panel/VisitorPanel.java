package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.StudentDAO;
import com.dormassist.dao.VisitorDAO;
import com.dormassist.model.Student;
import com.dormassist.model.Visitor;
import com.dormassist.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.ParseException;
import java.util.List;

public class VisitorPanel extends JPanel {

    private final VisitorDAO dao = new VisitorDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private JTable table;
    private JTextField searchField;
    private List<Visitor> allData;

    public VisitorPanel() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false); toolbar.setBorder(new EmptyBorder(0, 0, 14, 0));

        searchField = UIHelper.searchField("Tìm theo tên khách, sinh viên...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); left.setOpaque(false); left.add(searchField);
        toolbar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); right.setOpaque(false);
        JButton refreshBtn = UIHelper.grayButton("Làm mới"); refreshBtn.addActionListener(e -> loadData()); right.add(refreshBtn);
        JButton addBtn = UIHelper.primaryButton("+ Đăng ký khách"); addBtn.addActionListener(e -> showCreateDialog()); right.add(addBtn);
        toolbar.add(right, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Tên khách", "SĐT khách", "Sinh viên mời", "Ngày thăm", "Mục đích", "Trạng thái"};
        table = UIHelper.styledTable(cols);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        add(UIHelper.tableScroll(table), BorderLayout.CENTER);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) { if (e.isPopupTrigger()) showCtx(e); }
            public void mouseReleased(java.awt.event.MouseEvent e) { if (e.isPopupTrigger()) showCtx(e); }
        });
    }

    private void loadData() {
        UIHelper.runAsync(dao::getAll, data -> { allData = data; renderTable(allData); });
    }

    private void renderTable(List<Visitor> list) {
        UIHelper.clearTable(table);
        for (Visitor v : list)
            UIHelper.addRow(table, v.getId(), v.getVisitorName(), v.getVisitorPhone() != null ? v.getVisitorPhone() : "",
                    v.getStudentName() != null ? v.getStudentName() : "",
                    UIHelper.formatDate(v.getVisitDate()),
                    v.getPurpose() != null ? v.getPurpose() : "",
                    AppConstants.getStatusDisplay(v.getStatus()));
    }

    private void filter() {
        if (allData == null) return;
        String q = searchField.getText().toLowerCase();
        if (q.isBlank() || q.startsWith("tìm")) { renderTable(allData); return; }
        List<Visitor> f = new java.util.ArrayList<>();
        for (Visitor v : allData) {
            if (v.getVisitorName().toLowerCase().contains(q) || (v.getStudentName() != null && v.getStudentName().toLowerCase().contains(q)))
                f.add(v);
        }
        renderTable(f);
    }

    private void showCtx(java.awt.event.MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint()); if (row < 0) return;
        table.setRowSelectionInterval(row, row);
        JPopupMenu menu = new JPopupMenu();
        if (Session.canApproveVisitors()) {
            JMenuItem approve = new JMenuItem("Duyệt");
            approve.addActionListener(ev -> updateStatus((int)table.getValueAt(row, 0), "APPROVED")); menu.add(approve);
            JMenuItem reject = new JMenuItem("Từ chối");
            reject.setForeground(AppConstants.DANGER);
            reject.addActionListener(ev -> updateStatus((int)table.getValueAt(row, 0), "REJECTED")); menu.add(reject);
            JMenuItem checkin = new JMenuItem("Check-in");
            checkin.addActionListener(ev -> updateStatus((int)table.getValueAt(row, 0), "CHECKED_IN")); menu.add(checkin);
            JMenuItem checkout = new JMenuItem("Check-out");
            checkout.addActionListener(ev -> updateStatus((int)table.getValueAt(row, 0), "CHECKED_OUT")); menu.add(checkout);
            menu.addSeparator();
        }
        JMenuItem del = new JMenuItem("Xóa"); del.setForeground(AppConstants.DANGER);
        del.addActionListener(ev -> deleteSelected()); menu.add(del);
        menu.show(table, e.getX(), e.getY());
    }

    private void updateStatus(int id, String status) {
        if (dao.updateStatus(id, status, Session.getUserId())) { UIHelper.showSuccess(this, "Đã cập nhật trạng thái!"); loadData(); }
        else UIHelper.showError(this, "Cập nhật thất bại!");
    }

    private void deleteSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);
        if (!UIHelper.confirm(this, "Xóa đăng ký khách này?")) return;
        if (dao.delete(id)) { loadData(); }
        else UIHelper.showError(this, "Xóa thất bại!");
    }

    private void showCreateDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Đăng ký khách thăm", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(460, 400); dlg.setLocationRelativeTo(this);

        JPanel main = new JPanel(new BorderLayout()); main.setBackground(AppConstants.BG_MAIN);
        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(7, 6, 7, 6); g.fill = GridBagConstraints.HORIZONTAL;

        JTextField visitorNameField = UIHelper.styledField();
        JTextField visitorPhoneField = UIHelper.styledField();
        JTextField visitorIdCardField = UIHelper.styledField();
        JTextField visitDateField = UIHelper.styledField(); visitDateField.setText(UIHelper.DATE_FMT.format(new java.util.Date()));
        JTextField timeStartField = UIHelper.styledField(); timeStartField.setText("08:00");
        JTextField timeEndField   = UIHelper.styledField(); timeEndField.setText("21:00");
        JTextArea purposeArea = UIHelper.styledTextArea(3, 20);

        List<Student> students = studentDAO.getAll();
        JComboBox<Student> studentCombo = new JComboBox<>(); students.forEach(studentCombo::addItem); studentCombo.setFont(AppConstants.FONT_BODY);

        UIHelper.addFormRow(form, g, 0, "Sinh viên mời: *", studentCombo);
        UIHelper.addFormRow(form, g, 1, "Tên khách: *", visitorNameField);
        UIHelper.addFormRow(form, g, 2, "SĐT khách:", visitorPhoneField);
        UIHelper.addFormRow(form, g, 3, "CMND/CCCD khách:", visitorIdCardField);
        UIHelper.addFormRow(form, g, 4, "Ngày thăm (dd/MM/yyyy):", visitDateField);
        UIHelper.addFormRow(form, g, 5, "Giờ vào (HH:mm):", timeStartField);
        UIHelper.addFormRow(form, g, 6, "Giờ ra (HH:mm):", timeEndField);
        UIHelper.addFormRow(form, g, 7, "Mục đích:", UIHelper.smoothScroll(new JScrollPane(purposeArea)));
        main.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12)); btnPanel.setBackground(AppConstants.BG_MAIN);
        JButton cancelBtn = UIHelper.grayButton("Hủy"); cancelBtn.addActionListener(e -> dlg.dispose());
        JButton saveBtn = UIHelper.primaryButton("Đăng ký");
        saveBtn.addActionListener(e -> {
            String vname = visitorNameField.getText().trim();
            if (vname.isEmpty() || studentCombo.getSelectedItem() == null) { UIHelper.showWarning(dlg, "Điền đầy đủ thông tin!"); return; }
            Visitor v = new Visitor();
            v.setVisitorName(vname); v.setVisitorPhone(visitorPhoneField.getText().trim());
            v.setVisitorIdCard(visitorIdCardField.getText().trim()); v.setPurpose(purposeArea.getText().trim());
            Student st = (Student) studentCombo.getSelectedItem(); v.setStudentId(st.getId());
            try { v.setVisitDate(UIHelper.DATE_FMT.parse(visitDateField.getText().trim())); } catch (ParseException ignored) {}
            try { v.setVisitTimeStart(java.sql.Time.valueOf(timeStartField.getText().trim() + ":00")); } catch (Exception ignored) {}
            try { v.setVisitTimeEnd(java.sql.Time.valueOf(timeEndField.getText().trim() + ":00")); } catch (Exception ignored) {}
            if (dao.insert(v)) { UIHelper.showSuccess(dlg, "Đã đăng ký! Chờ quản trị viên duyệt."); dlg.dispose(); loadData(); }
            else UIHelper.showError(dlg, "Đăng ký thất bại!");
        });
        btnPanel.add(cancelBtn); btnPanel.add(saveBtn);
        main.add(btnPanel, BorderLayout.SOUTH);
        dlg.setContentPane(main); dlg.setVisible(true);
    }
}