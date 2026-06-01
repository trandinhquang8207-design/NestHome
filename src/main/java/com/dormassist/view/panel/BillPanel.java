package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.BillDAO;
import com.dormassist.dao.RoomDAO;
import com.dormassist.model.Bill;
import com.dormassist.model.PriceConfig;
import com.dormassist.model.Room;
import com.dormassist.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Calendar;
import java.util.List;

public class BillPanel extends JPanel {
    private final BillDAO dao = new BillDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private JTable table;
    private JTextField searchField;
    private List<Bill> allData;
    private JButton viewBtn, payBtn, deleteBtn;

    public BillPanel() {
        setLayout(new BorderLayout()); setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20)); buildUI(); loadData();
    }

    private void buildUI() {
        JPanel tb = new JPanel(new BorderLayout(10, 0)); tb.setOpaque(false); tb.setBorder(new EmptyBorder(0, 0, 12, 0));
        searchField = UIHelper.searchField("Tìm phòng, tháng/năm...");
        searchField.getDocument().addDocumentListener(simpleListener());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); left.setOpaque(false); left.add(searchField);
        tb.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); right.setOpaque(false);
        JButton refreshBtn = UIHelper.grayButton("Làm mới"); refreshBtn.addActionListener(e -> loadData());
        viewBtn   = UIHelper.infoButton("Xem chi tiết");
        payBtn    = UIHelper.successButton("Đánh dấu đã thanh toán");
        deleteBtn = UIHelper.dangerButton("Xóa");
        viewBtn.addActionListener(e -> viewDetail());
        payBtn.addActionListener(e -> markPaid());
        deleteBtn.addActionListener(e -> deleteSelected());
        viewBtn.setEnabled(false); payBtn.setEnabled(false); deleteBtn.setEnabled(false);

        right.add(refreshBtn); right.add(viewBtn);
        if (Session.canManageFinance()) {
            JButton addBtn = UIHelper.primaryButton("+ Tạo hóa đơn"); addBtn.addActionListener(e -> showCreateDialog());
            right.add(addBtn); right.add(payBtn); right.add(deleteBtn);
        }
        tb.add(right, BorderLayout.EAST);

        // Summary bar
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        summary.setBackground(Color.WHITE);
        summary.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER, 1, true),
            new EmptyBorder(0, 8, 0, 8)));
        JLabel revLbl = new JLabel("Tổng doanh thu đã thu: " + UIHelper.formatCurrency(dao.getTotalRevenue()));
        revLbl.setFont(AppConstants.FONT_BOLD); revLbl.setForeground(AppConstants.SUCCESS);
        JLabel unpaidLbl = new JLabel("  |  Chưa thanh toán: " + dao.countUnpaid() + " hóa đơn");
        unpaidLbl.setFont(AppConstants.FONT_BOLD); unpaidLbl.setForeground(AppConstants.WARNING);
        summary.add(revLbl); summary.add(unpaidLbl);

        JPanel topSection = new JPanel(new BorderLayout(0, 8)); topSection.setOpaque(false);
        topSection.add(tb, BorderLayout.NORTH); topSection.add(summary, BorderLayout.SOUTH);
        add(topSection, BorderLayout.NORTH);

        String[] cols = {"ID", "Phòng", "Tháng/Năm", "Tiền điện", "Tiền nước", "Tiền phòng", "Tổng cộng", "Hạn TT", "Trạng thái"};
        table = UIHelper.styledTable(cols);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(8).setPreferredWidth(130);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow(); boolean sel = row >= 0;
            viewBtn.setEnabled(sel);
            if (sel) {
                String status = (String) table.getValueAt(row, 8);
                payBtn.setEnabled("Chưa thanh toán".equals(status));
                deleteBtn.setEnabled(sel);
            } else { payBtn.setEnabled(false); deleteBtn.setEnabled(false); }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) viewDetail(); }
        });
        add(UIHelper.tableScroll(table), BorderLayout.CENTER);
    }

    private void loadData() { UIHelper.runAsync(dao::getAll, data -> { allData = data; renderTable(allData); }); }

    private void renderTable(List<Bill> list) {
        UIHelper.clearTable(table);
        for (Bill b : list)
            UIHelper.addRow(table, b.getId(), b.getRoomNumber(), b.getPeriod(),
                UIHelper.formatCurrency(b.getElectricAmount()),
                UIHelper.formatCurrency(b.getWaterAmount()),
                UIHelper.formatCurrency(b.getRentAmount()),
                UIHelper.formatCurrency(b.getTotalAmount()),
                UIHelper.formatDate(b.getDueDate()),
                AppConstants.getStatusDisplay(b.getStatus()));
    }

    private void filter() {
        if (allData == null) return;
        String q = searchField.getText().toLowerCase();
        if (q.isBlank() || q.startsWith("tim")) { renderTable(allData); return; }
        List<Bill> f = new java.util.ArrayList<>();
        for (Bill b : allData)
            if ((b.getRoomNumber() != null && b.getRoomNumber().toLowerCase().contains(q)) || b.getPeriod().contains(q))
                f.add(b);
        renderTable(f);
    }

    private void viewDetail() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);
        Bill b = dao.getById(id); if (b == null) return;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết hóa đơn - Phòng " + b.getRoomNumber(), Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(400, 460); dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(24, 28, 16, 28));

        addDRow(p, "Phòng", b.getRoomNumber());
        addDRow(p, "Kỳ hóa đơn", b.getPeriod());
        p.add(UIHelper.separator()); p.add(Box.createVerticalStrut(8));
        addDRow(p, "Tiêu thụ điện (" + (int)b.getElectricConsumption() + " kWh)", UIHelper.formatCurrency(b.getElectricAmount()));
        addDRow(p, "Tiêu thụ nước (" + (int)b.getWaterConsumption() + " m3)", UIHelper.formatCurrency(b.getWaterAmount()));
        addDRow(p, "Tiền phòng", UIHelper.formatCurrency(b.getRentAmount()));
        addDRow(p, "Phí dịch vụ", UIHelper.formatCurrency(b.getServiceAmount()));
        p.add(UIHelper.separator()); p.add(Box.createVerticalStrut(8));

        JPanel totalRow = new JPanel(new BorderLayout()); totalRow.setOpaque(false);
        JLabel tl = UIHelper.labelBold("TỔNG CỘNG"); tl.setForeground(AppConstants.PRIMARY);
        JLabel tv = new JLabel(UIHelper.formatCurrency(b.getTotalAmount()), SwingConstants.RIGHT);
        tv.setFont(new Font("Segoe UI", Font.BOLD, 18)); tv.setForeground(AppConstants.PRIMARY);
        totalRow.add(tl, BorderLayout.WEST); totalRow.add(tv, BorderLayout.EAST);
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT); totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        p.add(totalRow); p.add(Box.createVerticalStrut(10));

        addDRow(p, "Hạn thanh toán", UIHelper.formatDate(b.getDueDate()));
        addDRow(p, "Ngày thanh toán", UIHelper.formatDate(b.getPaidDate()));
        addDRow(p, "Trạng thái", AppConstants.getStatusDisplay(b.getStatus()));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btnRow.setBackground(Color.WHITE);
        if (Session.canManageFinance() && "UNPAID".equals(b.getStatus())) {
            JButton payBtn2 = UIHelper.successButton("Đã thanh toán");
            payBtn2.addActionListener(e -> { if (dao.markPaid(b.getId(), Session.getUserId())) { UIHelper.showSuccess(dlg, "Đã cập nhật!"); dlg.dispose(); loadData(); } });
            btnRow.add(payBtn2);
        }
        JButton closeBtn = UIHelper.grayButton("Đóng"); closeBtn.addActionListener(e -> dlg.dispose()); btnRow.add(closeBtn);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(UIHelper.smoothScroll(new JScrollPane(p)), BorderLayout.CENTER); wrap.add(btnRow, BorderLayout.SOUTH);
        dlg.setContentPane(wrap); dlg.setVisible(true);
    }

    private void addDRow(JPanel p, String label, String value) {
        JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false);
        row.setBorder(new EmptyBorder(3, 0, 3, 0)); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.add(UIHelper.label(label + ":"), BorderLayout.WEST);
        JLabel v = UIHelper.labelBold(value != null ? value : ""); v.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(v, BorderLayout.EAST); row.setAlignmentX(Component.LEFT_ALIGNMENT); p.add(row);
    }

    private void markPaid() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);
        if (!UIHelper.confirm(this, "Xác nhận đã nhận thanh toán hóa đơn này?")) return;
        if (dao.markPaid(id, Session.getUserId())) { UIHelper.showSuccess(this, "Đã cập nhật!"); loadData(); }
        else UIHelper.showError(this, "Thất bại!");
    }

    private void deleteSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);
        if (!UIHelper.confirm(this, "Xóa hóa đơn này?")) return;
        if (dao.delete(id)) { loadData(); }
        else UIHelper.showError(this, "Xóa thất bại!");
    }

    private void showCreateDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Tạo hóa đơn mới", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480, 480); dlg.setLocationRelativeTo(this);
        JPanel main = new JPanel(new BorderLayout()); main.setBackground(AppConstants.BG_MAIN);
        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(7, 6, 7, 6); g.fill = GridBagConstraints.HORIZONTAL;

        List<Room> rooms = roomDAO.getAll();
        JComboBox<Room> roomCb = new JComboBox<>(); rooms.forEach(roomCb::addItem); roomCb.setFont(AppConstants.FONT_BODY);
        Calendar cal = Calendar.getInstance();
        JSpinner monthSp = new JSpinner(new SpinnerNumberModel(cal.get(Calendar.MONTH) + 1, 1, 12, 1));
        JSpinner yearSp  = new JSpinner(new SpinnerNumberModel(cal.get(Calendar.YEAR), 2020, 2099, 1));
        ((JSpinner.DefaultEditor)monthSp.getEditor()).getTextField().setFont(AppConstants.FONT_BODY);
        ((JSpinner.DefaultEditor)yearSp.getEditor()).getTextField().setFont(AppConstants.FONT_BODY);

        PriceConfig pc = dao.getLatestPriceConfig();
        JTextField ePrevF = UIHelper.styledField(); ePrevF.setText("0");
        JTextField eCurrF = UIHelper.styledField(); eCurrF.setText("0");
        JTextField wPrevF = UIHelper.styledField(); wPrevF.setText("0");
        JTextField wCurrF = UIHelper.styledField(); wCurrF.setText("0");
        JTextField ePrcF  = UIHelper.styledField(); ePrcF.setText(String.valueOf((long)pc.getElectricPrice()));
        JTextField wPrcF  = UIHelper.styledField(); wPrcF.setText(String.valueOf((long)pc.getWaterPrice()));
        JTextField svcF   = UIHelper.styledField(); svcF.setText(String.valueOf((long)pc.getServiceFee()));
        JTextField dueF   = UIHelper.styledField();
        cal.add(Calendar.MONTH, 1); dueF.setText(UIHelper.DATE_FMT.format(cal.getTime()));

        UIHelper.addFormRow(form, g, 0, "Phòng: *", roomCb);
        UIHelper.addFormRow(form, g, 1, "Tháng:", monthSp);
        UIHelper.addFormRow(form, g, 2, "Năm:", yearSp);
        UIHelper.addFormRow(form, g, 3, "Chỉ số điện trước:", ePrevF);
        UIHelper.addFormRow(form, g, 4, "Chỉ số điện sau:", eCurrF);
        UIHelper.addFormRow(form, g, 5, "Đơn giá điện (đ/kWh):", ePrcF);
        UIHelper.addFormRow(form, g, 6, "Chỉ số nước trước:", wPrevF);
        UIHelper.addFormRow(form, g, 7, "Chỉ số nước sau:", wCurrF);
        UIHelper.addFormRow(form, g, 8, "Đơn giá nước (đ/m³):", wPrcF);
        UIHelper.addFormRow(form, g, 9, "Phí dịch vụ (đ):", svcF);
        UIHelper.addFormRow(form, g, 10, "Hạn thanh toán:", dueF);
        main.add(UIHelper.smoothScroll(new JScrollPane(form)), BorderLayout.CENTER);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12)); btnP.setBackground(AppConstants.BG_MAIN);
        JButton cancelBtn = UIHelper.grayButton("Hủy"); cancelBtn.addActionListener(e -> dlg.dispose());
        JButton createBtn = UIHelper.primaryButton("Tạo hóa đơn");
        createBtn.addActionListener(e -> {
            Room room = (Room) roomCb.getSelectedItem(); if (room == null) { UIHelper.showWarning(dlg, "Chọn phòng!"); return; }
            try {
                double ep=Double.parseDouble(ePrevF.getText().trim()), ec=Double.parseDouble(eCurrF.getText().trim());
                double epr=Double.parseDouble(ePrcF.getText().trim());
                double wp=Double.parseDouble(wPrevF.getText().trim()), wc=Double.parseDouble(wCurrF.getText().trim());
                double wpr=Double.parseDouble(wPrcF.getText().trim()), svc=Double.parseDouble(svcF.getText().trim());
                if (ec < ep || wc < wp) { UIHelper.showWarning(dlg, "Chỉ số sau phải lớn hơn chỉ số trước!"); return; }
                Bill b = new Bill();
                b.setRoomId(room.getId()); b.setBillMonth((int)monthSp.getValue()); b.setBillYear((int)yearSp.getValue());
                b.setElectricConsumption(ec-ep); b.setElectricAmount((ec-ep)*epr);
                b.setWaterConsumption(wc-wp); b.setWaterAmount((wc-wp)*wpr);
                b.setRentAmount(room.getRentPrice()); b.setServiceAmount(svc);
                b.setTotalAmount(b.getElectricAmount()+b.getWaterAmount()+b.getRentAmount()+svc);
                b.setStatus("UNPAID"); b.setCreatedBy(Session.getUserId());
                try { b.setDueDate(UIHelper.DATE_FMT.parse(dueF.getText().trim())); } catch (Exception ignored) {}
                if (dao.insert(b)) {
                    UIHelper.showSuccess(dlg, "Tạo thành công!\nTổng: " + UIHelper.formatCurrency(b.getTotalAmount()));
                    dlg.dispose(); loadData();
                } else UIHelper.showError(dlg, "Thất bại (có thể đã tồn tại hóa đơn tháng này)!");
            } catch (NumberFormatException ex) { UIHelper.showError(dlg, "Dữ liệu số không hợp lệ!"); }
        });
        btnP.add(cancelBtn); btnP.add(createBtn);
        main.add(btnP, BorderLayout.SOUTH);
        dlg.setContentPane(main); dlg.setVisible(true);
    }

    private javax.swing.event.DocumentListener simpleListener() {
        return new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){filter();}
            public void removeUpdate(javax.swing.event.DocumentEvent e){filter();}
            public void changedUpdate(javax.swing.event.DocumentEvent e){filter();}
        };
    }
}
