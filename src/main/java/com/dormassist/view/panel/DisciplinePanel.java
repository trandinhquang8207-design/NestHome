package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.DisciplineDAO;
import com.dormassist.dao.StudentDAO;
import com.dormassist.model.DisciplinePoint;
import com.dormassist.model.Student;
import com.dormassist.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DisciplinePanel extends JPanel {
    private final DisciplineDAO dao = new DisciplineDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private JTable table;
    private JTable roomSummaryTable;
    private List<DisciplinePoint> allData;
    private JButton deleteBtn;

    public DisciplinePanel() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel tb = new JPanel(new BorderLayout(10, 0));
        tb.setOpaque(false); tb.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); left.setOpaque(false);
        left.add(UIHelper.sectionTitle("Điểm rèn luyện sinh viên"));
        left.add(Box.createHorizontalStrut(12));
        left.add(UIHelper.labelMuted("Tổng quan theo phòng và lịch sử ghi điểm"));
        tb.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); right.setOpaque(false);
        JButton refreshBtn = UIHelper.grayButton("Làm mới"); refreshBtn.addActionListener(e -> loadData());
        deleteBtn = UIHelper.dangerButton("Xóa");
        deleteBtn.addActionListener(e -> deleteSelected());
        deleteBtn.setEnabled(false);
        right.add(refreshBtn);

        if (Session.isAdmin() || Session.isBase1() || Session.isBase2()) {
            JButton addBtn = UIHelper.primaryButton("+ Ghi điểm");
            addBtn.addActionListener(e -> showAddDialog());
            JButton penaltyBtn = UIHelper.warningButton("- Trừ điểm");
            penaltyBtn.addActionListener(e -> showPenaltyDialog());
            right.add(penaltyBtn); right.add(addBtn);
        }
        right.add(deleteBtn);
        tb.add(right, BorderLayout.EAST);
        add(tb, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(0, 12));
        mainContent.setOpaque(false);
        mainContent.add(buildRoomSummaryCard(), BorderLayout.NORTH);

        String[] cols = {"ID", "Sinh viên", "Phòng", "Điểm", "Xếp loại", "Lý do", "Người ghi", "Thời gian"};
        table = UIHelper.styledTable(cols);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setMaxWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(260);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);
        table.getColumnModel().getColumn(7).setPreferredWidth(150);

        table.getSelectionModel().addListSelectionListener(e ->
            deleteBtn.setEnabled(table.getSelectedRow() >= 0 && Session.isAdmin()));

        mainContent.add(UIHelper.tableScroll(table), BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel buildRoomSummaryCard() {
        JPanel c = UIHelper.card();
        c.setLayout(new BorderLayout(0, 8));
        c.add(UIHelper.sectionTitle("Tổng quan điểm theo phòng"), BorderLayout.NORTH);
        roomSummaryTable = UIHelper.styledTable(new String[]{"Phòng", "Điểm trung bình", "Xuất sắc", "Tốt", "Cần nhắc nhở", "Tổng sinh viên"});
        roomSummaryTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        roomSummaryTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        roomSummaryTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        roomSummaryTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        roomSummaryTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        roomSummaryTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        JScrollPane sp = UIHelper.tableScroll(roomSummaryTable);
        sp.setPreferredSize(new Dimension(0, 150));
        c.add(sp, BorderLayout.CENTER);
        return c;
    }

    private void loadData() {
        if (Session.isBase3()) {
            Student s = studentDAO.getByUserId(Session.getUserId());
            allData = s != null ? dao.getByStudent(s.getId()) : new java.util.ArrayList<>();
        } else {
            allData = dao.getAll();
        }
        renderRoomSummary();
        renderTable(allData);
    }

    private void renderRoomSummary() {
        if (roomSummaryTable == null) return;
        UIHelper.clearTable(roomSummaryTable);
        List<Student> students = studentDAO.getAll();
        Map<String, RoomScore> byRoom = new LinkedHashMap<>();
        for (Student s : students) {
            String room = s.getRoomNumber() != null && !s.getRoomNumber().trim().isEmpty() ? s.getRoomNumber() : "Chưa xếp phòng";
            RoomScore rs = byRoom.computeIfAbsent(room, k -> new RoomScore());
            rs.total++;
            rs.sum += s.getDisciplinePoints();
            if (s.getDisciplinePoints() >= 90) rs.excellent++;
            else if (s.getDisciplinePoints() >= 80) rs.good++;
            if (s.getDisciplinePoints() < 65) rs.warning++;
        }
        if (byRoom.isEmpty()) {
            UIHelper.addRow(roomSummaryTable, "Chưa có sinh viên", "", "", "", "", "");
            return;
        }
        for (Map.Entry<String, RoomScore> e : byRoom.entrySet()) {
            RoomScore rs = e.getValue();
            double avg = rs.total == 0 ? 0 : rs.sum / (double) rs.total;
            UIHelper.addRow(roomSummaryTable, e.getKey(), String.format("%.1f", avg), rs.excellent, rs.good, rs.warning, rs.total);
        }
    }

    private void renderTable(List<DisciplinePoint> list) {
        UIHelper.clearTable(table);
        if (list == null || list.isEmpty()) {
            UIHelper.addRow(table, "", "Chưa có dữ liệu điểm", "", "", "", "", "", "");
            return;
        }
        for (DisciplinePoint d : list) {
            int score = d.getCurrentScore();
            String reason = d.getReason();
            String delta = "BONUS".equals(d.getType()) ? " +" + d.getPoints() : " -" + d.getPoints();
            if (reason == null) reason = "";
            reason = reason + delta;
            UIHelper.addRow(table,
                d.getId(),
                d.getStudentName() != null ? d.getStudentName() : "",
                d.getRoomNumber() != null ? d.getRoomNumber() : "",
                score,
                classify(score),
                reason,
                d.getCreatorName() != null ? d.getCreatorName() : "",
                UIHelper.formatDateTime(d.getCreatedAt()));
        }
    }

    private String classify(int score) {
        if (score >= 90) return "Xuất sắc";
        if (score >= 80) return "Tốt";
        if (score >= 65) return "Đạt";
        if (score >= 50) return "Cần nhắc nhở";
        return "Yếu";
    }

    private void deleteSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        Object idObj = table.getValueAt(row, 0);
        if (!(idObj instanceof Integer)) return;
        int id = (int) idObj;
        if (!UIHelper.confirm(this, "Xóa bản ghi điểm này?")) return;
        if (dao.delete(id)) { loadData(); }
        else UIHelper.showError(this, "Xóa thất bại!");
    }

    private void showAddDialog() {
        showDisciplineDialog("BONUS", "Ghi cộng điểm");
    }

    private void showPenaltyDialog() {
        showDisciplineDialog("PENALTY", "Trừ điểm - Kết quả kiểm tra");
    }

    private void showDisciplineDialog(String type, String title) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(500, 430); dlg.setLocationRelativeTo(this);

        JPanel main = new JPanel(new BorderLayout()); main.setBackground(AppConstants.BG_MAIN);
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        hdr.setBackground("BONUS".equals(type) ? AppConstants.SUCCESS : AppConstants.DANGER);
        JLabel hdrLbl = new JLabel("BONUS".equals(type) ? "Cộng điểm rèn luyện" : "Trừ điểm - Kết quả kiểm tra");
        hdrLbl.setFont(AppConstants.FONT_HEADER); hdrLbl.setForeground(Color.WHITE);
        hdr.add(hdrLbl); main.add(hdr, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(8, 6, 8, 6); g.fill = GridBagConstraints.HORIZONTAL;

        List<Student> students = studentDAO.getAll();
        JComboBox<Student> stCb = new JComboBox<>();
        students.forEach(stCb::addItem); stCb.setFont(AppConstants.FONT_BODY);

        JSpinner pointsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        ((JSpinner.DefaultEditor) pointsSpinner.getEditor()).getTextField().setFont(AppConstants.FONT_BODY);
        pointsSpinner.setPreferredSize(new Dimension(200, AppConstants.FIELD_H));

        JTextField reasonField = UIHelper.styledField();
        JTextArea detailArea = UIHelper.styledTextArea(4, 30);
        JScrollPane dsp = UIHelper.smoothScroll(new JScrollPane(detailArea)); dsp.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));

        JTextField checkDateField = null;
        if ("PENALTY".equals(type)) {
            checkDateField = UIHelper.styledField();
            checkDateField.setText(UIHelper.DATE_FMT.format(new java.util.Date()));
            checkDateField.setToolTipText("dd/MM/yyyy");
        }

        UIHelper.addFormRow(form, g, 0, "Sinh viên: *", stCb);
        UIHelper.addFormRow(form, g, 1, "Số điểm:", pointsSpinner);
        if (checkDateField != null) UIHelper.addFormRow(form, g, 2, "Ngày kiểm tra:", checkDateField);
        UIHelper.addFormRow(form, g, 3, "Lý do: *", reasonField);
        g.gridx = 0; g.gridy = 4; g.weightx = 0.35;
        form.add(UIHelper.labelBold("Chi tiết:"), g);
        g.gridx = 1; g.weightx = 0.65; g.ipady = 60;
        form.add(dsp, g); g.ipady = 0;

        JLabel currentPtsLbl = UIHelper.labelMuted("Điểm hiện tại: ...");
        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        form.add(currentPtsLbl, g);
        stCb.addActionListener(ev -> {
            Student selected = (Student) stCb.getSelectedItem();
            if (selected != null) {
                Student full = studentDAO.getById(selected.getId());
                if (full != null) currentPtsLbl.setText("Điểm hiện tại: " + full.getDisciplinePoints() + " / 100");
            }
        });
        if (stCb.getItemCount() > 0) stCb.setSelectedIndex(0);

        main.add(form, BorderLayout.CENTER);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12)); btnP.setBackground(AppConstants.BG_MAIN);
        JButton cancelBtn = UIHelper.grayButton("Hủy"); cancelBtn.addActionListener(e -> dlg.dispose());
        JButton saveBtn = "PENALTY".equals(type) ? UIHelper.dangerButton("Ghi trừ điểm") : UIHelper.successButton("Ghi cộng điểm");

        final JTextField finalCheckDate = checkDateField;
        saveBtn.addActionListener(e -> {
            if (stCb.getSelectedItem() == null) { UIHelper.showWarning(dlg, "Chọn sinh viên!"); return; }
            if (reasonField.getText().trim().isEmpty()) { UIHelper.showWarning(dlg, "Nhập lý do!"); return; }

            String detail = detailArea.getText().trim();
            if ("PENALTY".equals(type) && finalCheckDate != null && !finalCheckDate.getText().trim().isEmpty())
                detail = "Ngày kiểm tra: " + finalCheckDate.getText().trim() + "\n" + detail;

            DisciplinePoint dp = new DisciplinePoint();
            dp.setStudentId(((Student) stCb.getSelectedItem()).getId());
            dp.setType(type);
            dp.setPoints((int) pointsSpinner.getValue());
            dp.setReason(reasonField.getText().trim());
            dp.setDetail(detail);
            dp.setCreatedBy(Session.getUserId());

            if (dao.insert(dp)) {
                UIHelper.showSuccess(dlg, "Đã ghi nhận!");
                dlg.dispose(); loadData();
            } else UIHelper.showError(dlg, "Thất bại!");
        });
        btnP.add(cancelBtn); btnP.add(saveBtn);
        main.add(btnP, BorderLayout.SOUTH);
        dlg.setContentPane(main); dlg.setVisible(true);
    }

    private static class RoomScore {
        int total;
        int excellent;
        int good;
        int warning;
        int sum;
    }
}
