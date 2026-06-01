package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.RoomTransferDAO;
import com.dormassist.dao.StudentDAO;
import com.dormassist.dao.RoomDAO;
import com.dormassist.model.RoomTransferRequest;
import com.dormassist.model.Student;
import com.dormassist.model.Room;
import com.dormassist.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class TransferPanel extends JPanel {
    private final RoomTransferDAO dao = new RoomTransferDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private JTable table;
    private List<RoomTransferRequest> allData;
    private JButton approveBtn, rejectBtn, deleteBtn;

    public TransferPanel() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel tb = new JPanel(new BorderLayout(10, 0));
        tb.setOpaque(false); tb.setBorder(new EmptyBorder(0, 0, 14, 0));
        tb.add(UIHelper.sectionTitle("Đăng ký đổi phòng"), BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton refreshBtn = UIHelper.grayButton("Làm mới");
        refreshBtn.addActionListener(e -> loadData());
        JButton addBtn = UIHelper.primaryButton("+ Xin đổi phòng");
        addBtn.addActionListener(e -> showCreateDialog());

        approveBtn = UIHelper.successButton("Duyệt");
        rejectBtn  = UIHelper.dangerButton("Từ chối");
        deleteBtn  = UIHelper.grayButton("Xóa");
        approveBtn.addActionListener(e -> processSelected("APPROVED"));
        rejectBtn.addActionListener(e -> processSelected("REJECTED"));
        deleteBtn.addActionListener(e -> deleteSelected());
        approveBtn.setEnabled(false); rejectBtn.setEnabled(false); deleteBtn.setEnabled(false);

        right.add(refreshBtn); right.add(addBtn);
        if (Session.isAdmin() || Session.isBase1()) {
            right.add(approveBtn); right.add(rejectBtn);
        }
        right.add(deleteBtn);
        tb.add(right, BorderLayout.EAST);
        add(tb, BorderLayout.NORTH);

        String[] cols = {"ID", "Sinh viên", "Phòng hiện tại", "Phòng muốn chuyển", "Lý do", "Trạng thái", "Ngày yêu cầu"};
        table = UIHelper.styledTable(cols);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow(); boolean sel = row >= 0;
            deleteBtn.setEnabled(sel);
            if (sel) {
                String status = (String) table.getValueAt(row, 5);
                boolean isPending = "Chờ duyệt".equals(status);
                approveBtn.setEnabled(isPending);
                rejectBtn.setEnabled(isPending);
            } else { approveBtn.setEnabled(false); rejectBtn.setEnabled(false); }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) viewDetail();
            }
        });
        add(UIHelper.tableScroll(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT)); bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(6, 0, 0, 0));
        bottom.add(UIHelper.labelMuted("Chọn hàng để duyệt / từ chối / xóa  |  Nhấn đúp để xem chi tiết"));
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData() {
        if (Session.isAdmin() || Session.isBase1()) {
            allData = dao.getAll();
        } else {
            Student s = studentDAO.getByUserId(Session.getUserId());
            allData = s != null ? dao.getByStudent(s.getId()) : new java.util.ArrayList<>();
        }
        renderTable(allData);
    }

    private void renderTable(List<RoomTransferRequest> list) {
        UIHelper.clearTable(table);
        for (RoomTransferRequest r : list) {
            UIHelper.addRow(table,
                r.getId(),
                r.getStudentName() != null ? r.getStudentName() : "",
                r.getFromRoomNumber() != null ? r.getFromRoomNumber() : "Chưa có phòng",
                r.getToRoomNumber() != null ? r.getToRoomNumber() : "Bất kỳ",
                r.getReason(),
                AppConstants.getStatusDisplay(r.getStatus()),
                UIHelper.formatDateTime(r.getCreatedAt()));
        }
    }

    private void viewDetail() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);
        RoomTransferRequest r = allData.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
        if (r == null) return;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết yêu cầu đổi phòng", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(440, 360); dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(7, 6, 7, 6); g.fill = GridBagConstraints.HORIZONTAL;

        addDetailRow(p, g, 0, "Sinh viên:", r.getStudentName());
        addDetailRow(p, g, 1, "Phòng hiện tại:", r.getFromRoomNumber() != null ? r.getFromRoomNumber() : "Chưa có phòng");
        addDetailRow(p, g, 2, "Phòng muốn chuyển:", r.getToRoomNumber() != null ? r.getToRoomNumber() : "Bất kỳ");
        addDetailRow(p, g, 3, "Lý do:", r.getReason());
        addDetailRow(p, g, 4, "Trạng thái:", AppConstants.getStatusDisplay(r.getStatus()));
        addDetailRow(p, g, 5, "Ngày yêu cầu:", UIHelper.formatDateTime(r.getCreatedAt()));
        if (r.getAdminNotes() != null && !r.getAdminNotes().isEmpty())
            addDetailRow(p, g, 6, "Ghi chú admin:", r.getAdminNotes());

        JPanel btn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8)); btn.setBackground(Color.WHITE);
        JButton closeBtn = UIHelper.grayButton("Đóng"); closeBtn.addActionListener(e -> dlg.dispose()); btn.add(closeBtn);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(UIHelper.smoothScroll(new JScrollPane(p)), BorderLayout.CENTER); wrap.add(btn, BorderLayout.SOUTH);
        dlg.setContentPane(wrap); dlg.setVisible(true);
    }

    private void addDetailRow(JPanel p, GridBagConstraints g, int row, String label, String value) {
        g.gridx = 0; g.gridy = row; g.weightx = 0.35;
        p.add(UIHelper.labelBold(label), g);
        g.gridx = 1; g.weightx = 0.65;
        p.add(UIHelper.label(value != null ? value : ""), g);
    }

    private void processSelected(String status) {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);
        String label = "APPROVED".equals(status) ? "duyệt" : "từ chối";
        if (!UIHelper.confirm(this, "Bạn có chắc muốn " + label + " yêu cầu này?")) return;

        String notes = JOptionPane.showInputDialog(this, "Ghi chú (nếu có):", "");
        if (notes == null) return;

        if (dao.updateStatus(id, status, Session.getUserId(), notes)) {
            UIHelper.showSuccess(this, "APPROVED".equals(status) ? "Đã duyệt yêu cầu!" : "Đã từ chối yêu cầu!");
            loadData();
        } else UIHelper.showError(this, "Thao tác thất bại!");
    }

    private void deleteSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);
        if (!UIHelper.confirm(this, "Xóa yêu cầu đổi phòng này?")) return;
        if (dao.delete(id)) { loadData(); }
        else UIHelper.showError(this, "Không thể xóa (đã xử lý)!");
    }

    private void showCreateDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Xin đổi phòng", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480, 380); dlg.setLocationRelativeTo(this);

        JPanel main = new JPanel(new BorderLayout()); main.setBackground(AppConstants.BG_MAIN);

        // Header xanh
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        hdr.setBackground(AppConstants.PRIMARY);
        JPanel htxt = new JPanel(); htxt.setOpaque(false); htxt.setLayout(new BoxLayout(htxt, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Đơn xin đổi phòng"); t.setFont(AppConstants.FONT_HEADER); t.setForeground(Color.WHITE);
        JLabel s = new JLabel("Yêu cầu sẽ được quản trị viên xem xét"); s.setFont(AppConstants.FONT_SMALL); s.setForeground(new Color(200, 220, 255));
        htxt.add(t); htxt.add(s); hdr.add(htxt);
        main.add(hdr, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(8, 6, 8, 6); g.fill = GridBagConstraints.HORIZONTAL;

        // Sinh viên (nếu admin thì chọn, nếu là SV thì tự lấy)
        JComboBox<Student> studentCb = null;
        Student currentStudent = null;

        if (Session.isAdmin() || Session.isBase1() || Session.isBase2()) {
            List<Student> students = studentDAO.getAll();
            studentCb = new JComboBox<>();
            for (Student st : students) studentCb.addItem(st);
            studentCb.setFont(AppConstants.FONT_BODY);
            UIHelper.addFormRow(form, g, 0, "Sinh viên: *", studentCb);
        } else {
            currentStudent = studentDAO.getByUserId(Session.getUserId());
            String name = currentStudent != null ? currentStudent.getFullName() : "Không tìm thấy";
            JLabel nameLabel = UIHelper.label(name);
            UIHelper.addFormRow(form, g, 0, "Sinh viên:", nameLabel);
        }

        // Phòng hiện tại (tự điền)
        List<Room> rooms = roomDAO.getAvailable();
        JComboBox<Object> fromCb = new JComboBox<>();
        fromCb.addItem("-- Phòng hiện tại --");
        List<Room> allRooms = roomDAO.getAll();
        allRooms.forEach(fromCb::addItem);
        fromCb.setFont(AppConstants.FONT_BODY);

        JComboBox<Object> toCb = new JComboBox<>();
        toCb.addItem("-- Bất kỳ phòng trống --");
        rooms.forEach(toCb::addItem);
        toCb.setFont(AppConstants.FONT_BODY);

        // Lý do (BẮT BUỘC)
        JTextArea reasonArea = UIHelper.styledTextArea(4, 30);
        JScrollPane rsp = UIHelper.smoothScroll(new JScrollPane(reasonArea));
        rsp.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));

        UIHelper.addFormRow(form, g, 1, "Phòng hiện tại:", fromCb);
        UIHelper.addFormRow(form, g, 2, "Phòng muốn chuyển:", toCb);
        g.gridx = 0; g.gridy = 3; g.weightx = 0.35;
        form.add(UIHelper.labelBold("Lý do: *"), g);
        g.gridx = 1; g.weightx = 0.65; g.ipady = 60;
        form.add(rsp, g);
        g.ipady = 0;

        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        form.add(UIHelper.labelMuted("(*) Bắt buộc - Ghi rõ lý do xin đổi phòng"), g);

        main.add(form, BorderLayout.CENTER);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12)); btnP.setBackground(AppConstants.BG_MAIN);
        JButton cancelBtn = UIHelper.grayButton("Hủy"); cancelBtn.addActionListener(e -> dlg.dispose());
        JButton sendBtn = UIHelper.primaryButton("Gửi yêu cầu");

        final JComboBox<Student> finalStudentCb = studentCb;
        final Student finalCurrentStudent = currentStudent;

        sendBtn.addActionListener(e -> {
            String reason = reasonArea.getText().trim();
            if (reason.isEmpty()) { UIHelper.showWarning(dlg, "Vui lòng nhập lý do xin đổi phòng!"); return; }

            RoomTransferRequest req = new RoomTransferRequest();
            req.setReason(reason); req.setStatus("PENDING");

            if (finalStudentCb != null) {
                Student selected = (Student) finalStudentCb.getSelectedItem();
                if (selected == null) { UIHelper.showWarning(dlg, "Chọn sinh viên!"); return; }
                req.setStudentId(selected.getId());
            } else {
                if (finalCurrentStudent == null) { UIHelper.showError(dlg, "Không tìm thấy thông tin sinh viên!"); return; }
                req.setStudentId(finalCurrentStudent.getId());
            }

            Object from = fromCb.getSelectedItem();
            if (from instanceof Room) req.setFromRoomId(((Room) from).getId());
            Object to = toCb.getSelectedItem();
            if (to instanceof Room) req.setToRoomId(((Room) to).getId());

            if (dao.insert(req)) {
                UIHelper.showSuccess(dlg, "Đã gửi yêu cầu đổi phòng!\nVui lòng chờ quản trị viên duyệt.");
                dlg.dispose(); loadData();
            } else UIHelper.showError(dlg, "Gửi thất bại!");
        });
        btnP.add(cancelBtn); btnP.add(sendBtn);
        main.add(btnP, BorderLayout.SOUTH);
        dlg.setContentPane(main); dlg.setVisible(true);
    }
}
