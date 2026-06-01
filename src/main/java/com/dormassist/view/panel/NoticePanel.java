package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.NotificationDAO;
import com.dormassist.model.Notification;
import com.dormassist.util.UIHelper;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.concurrent.*;

public class NoticePanel extends JPanel {
    private final NotificationDAO dao = new NotificationDAO();
    private JTable table;
    private List<Notification> allData;
    private JButton viewBtn, deleteBtn;
    private ScheduledExecutorService scheduler;

    public NoticePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        buildUI();
        loadData();
        startAutoRefresh();
    }

    // ===== ĐA LUỒNG: tự cập nhật mỗi 15 giây =====
    private void startAutoRefresh() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "notice-refresh");
            t.setDaemon(true); return t;
        });
        scheduler.scheduleAtFixedRate(this::loadData, 15, 15, TimeUnit.SECONDS);
    }

    private void buildUI() {
        // Header + toolbar
        JPanel tb = new JPanel(new BorderLayout(10, 0));
        tb.setOpaque(false); tb.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.add(UIHelper.sectionTitle("Thông báo & Tin tức"));
        JLabel liveLbl = new JLabel("   Tự động làm mới mỗi 15 giây");
        liveLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        liveLbl.setForeground(AppConstants.SUCCESS);
        titleRow.add(liveLbl);
        tb.add(titleRow, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton refreshBtn = UIHelper.grayButton("Làm mới");
        refreshBtn.addActionListener(e -> loadData());
        viewBtn   = UIHelper.infoButton("Xem nội dung");
        deleteBtn = UIHelper.dangerButton("Xóa");
        viewBtn.addActionListener(e -> viewDetail());
        deleteBtn.addActionListener(e -> deleteSelected());
        viewBtn.setEnabled(false); deleteBtn.setEnabled(false);
        right.add(refreshBtn);
        right.add(viewBtn);
        if (Session.isAdmin()) right.add(deleteBtn);
        tb.add(right, BorderLayout.EAST);
        add(tb, BorderLayout.NORTH);

        // Bảng danh sách
        String[] cols = {"ID", "Tiêu đề", "Nội dung tóm tắt", "Đối tượng", "Quan trọng", "Người đăng", "Thời gian"};
        table = UIHelper.styledTable(cols);
        table.getColumnModel().getColumn(0).setMinWidth(45);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setMinWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setMinWidth(260);
        table.getColumnModel().getColumn(2).setPreferredWidth(360);
        table.getColumnModel().getColumn(3).setMinWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setMinWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setMinWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);
        table.getColumnModel().getColumn(6).setMinWidth(140);
        table.getColumnModel().getColumn(6).setPreferredWidth(170);
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = table.getSelectedRow() >= 0;
            viewBtn.setEnabled(sel); deleteBtn.setEnabled(sel);
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) viewDetail();
            }
        });

        // Panel dưới: bảng + form đăng thông báo (admin)
        if (Session.isAdmin()) {
            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                UIHelper.tableScroll(table), buildPostForm());
            split.setResizeWeight(0.55);
            split.setDividerSize(6);
            split.setBorder(null);
            add(split, BorderLayout.CENTER);
        } else {
            add(UIHelper.tableScroll(table), BorderLayout.CENTER);
        }
    }

    /** Form đăng thông báo MỞ RỘNG (chỉ admin) */
    private JPanel buildPostForm() {
        JPanel outer = UIHelper.card();
        outer.setLayout(new BorderLayout(0, 10));

        JLabel formTitle = UIHelper.sectionTitle("Đăng thông báo mới");
        outer.add(formTitle, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(12, 0));
        body.setOpaque(false);

        // Cột trái: metadata
        JPanel meta = new JPanel(new GridBagLayout());
        meta.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6); g.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = UIHelper.styledField();
        String[] targetVN = {"Tất cả", "Quản trị viên", "Trưởng tầng", "Trưởng phòng", "Sinh viên"};
        String[] targetCode = {"ALL", "ADMIN_SUPER", "BASE1", "BASE2", "BASE3"};
        JComboBox<String> targetCb = UIHelper.styledCombo(targetVN);
        JCheckBox importantCb = new JCheckBox("Đánh dấu quan trọng");
        importantCb.setFont(AppConstants.FONT_BODY); importantCb.setOpaque(false);

        UIHelper.addFormRow(meta, g, 0, "Tiêu đề: *", titleField);
        UIHelper.addFormRow(meta, g, 1, "Gửi đến:", targetCb);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2;
        meta.add(importantCb, g);
        meta.setPreferredSize(new Dimension(300, 0));
        body.add(meta, BorderLayout.WEST);

        // Cột phải: nội dung (khung rộng, cao)
        JPanel contentCol = new JPanel(new BorderLayout(0, 4));
        contentCol.setOpaque(false);
        contentCol.add(UIHelper.labelBold("Nội dung: *"), BorderLayout.NORTH);
        JTextArea contentArea = UIHelper.styledTextArea(6, 40);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane csp = UIHelper.smoothScroll(new JScrollPane(contentArea));
        csp.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER, 1, true));
        csp.setPreferredSize(new Dimension(0, 150));
        contentCol.add(csp, BorderLayout.CENTER);
        body.add(contentCol, BorderLayout.CENTER);

        outer.add(body, BorderLayout.CENTER);

        // Nút gửi
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        JButton clearBtn = UIHelper.grayButton("Xóa trắng");
        clearBtn.addActionListener(e -> { titleField.setText(""); contentArea.setText(""); targetCb.setSelectedIndex(0); importantCb.setSelected(false); });
        JButton sendBtn = UIHelper.primaryButton("Đăng thông báo");
        sendBtn.addActionListener(e -> {
            String title   = titleField.getText().trim();
            String content = contentArea.getText().trim();
            if (title.isEmpty() || content.isEmpty()) { UIHelper.showWarning(this, "Điền đầy đủ tiêu đề và nội dung!"); return; }
            Notification n = new Notification();
            n.setSenderId(Session.getUserId()); n.setTitle(title); n.setContent(content);
            n.setTargetRole(targetCode[targetCb.getSelectedIndex()]);
            n.setImportant(importantCb.isSelected());
            if (dao.insert(n)) {
                UIHelper.showSuccess(this, "Đã đăng thông báo!");
                titleField.setText(""); contentArea.setText("");
                loadData();
            } else UIHelper.showError(this, "Thất bại!");
        });
        btnRow.add(clearBtn); btnRow.add(sendBtn);
        outer.add(btnRow, BorderLayout.SOUTH);
        return outer;
    }

    private void loadData() {
        UIHelper.runAsync(dao::getAll, data -> {
            allData = data;

            if (allData != null) {
                allData.sort(Comparator.comparingInt(Notification::getId));
            }

            UIHelper.clearTable(table);
            if (allData == null || allData.isEmpty()) {
                UIHelper.addRow(table, "", "Chưa có thông báo nào", "", "", "", "", "");
                return;
            }
            for (Notification n : allData) {
                String snippet = n.getContent() != null && n.getContent().length() > 70
                    ? n.getContent().substring(0, 70) + "..." : n.getContent();
                UIHelper.addRow(table, n.getId(), n.getTitle(), snippet,
                    getTargetDisplay(n.getTargetRole()),
                    n.isImportant() ? "Quan trọng" : "",
                    n.getSenderName() != null ? n.getSenderName() : "",
                    UIHelper.formatDateTime(n.getCreatedAt()));
            }
        });
    }

    private String getTargetDisplay(String role) {
        if (role == null) return "Tất cả";
        switch (role) {
            case "ALL": return "Tất cả";
            case "ADMIN_SUPER": return "Quản trị viên";
            case "ADMIN_BASE":  return "Quản trị viên";
            case "BASE1": return "Trưởng tầng";
            case "BASE2": return "Trưởng phòng";
            case "BASE3": return "Sinh viên";
            default: return role;
        }
    }

    private void viewDetail() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);
        Notification n = allData.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
        if (n == null) return;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), n.getTitle(), Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(560, 400); dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(24, 28, 20, 28));

        if (n.isImportant()) {
            JLabel imp = new JLabel("  QUAN TRỌNG");
            imp.setFont(new Font("Segoe UI", Font.BOLD, 11));
            imp.setForeground(AppConstants.DANGER);
            imp.setOpaque(true); imp.setBackground(new Color(254, 226, 226));
            imp.setBorder(new EmptyBorder(4, 10, 4, 10));
            JPanel impRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); impRow.setOpaque(false); impRow.add(imp);
            p.add(impRow, BorderLayout.NORTH);
        }

        JLabel titleLbl = new JLabel(n.getTitle());
        titleLbl.setFont(AppConstants.FONT_HEADER); titleLbl.setForeground(AppConstants.PRIMARY);

        JLabel meta = UIHelper.labelMuted("Người đăng: " + (n.getSenderName() != null ? n.getSenderName() : "N/A") +
            "  |  " + UIHelper.formatDateTime(n.getCreatedAt()) +
            "  |  Gửi đến: " + getTargetDisplay(n.getTargetRole()));

        JTextArea content = UIHelper.styledTextArea(8, 40);
        content.setText(n.getContent()); content.setEditable(false);
        content.setBackground(new Color(248, 250, 252));
        JScrollPane sp = UIHelper.smoothScroll(new JScrollPane(content));
        sp.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));

        JPanel center = new JPanel(new BorderLayout(0, 8)); center.setOpaque(false);
        center.add(titleLbl, BorderLayout.NORTH);
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2)); metaRow.setOpaque(false); metaRow.add(meta);
        center.add(metaRow, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);
        p.add(sp, BorderLayout.SOUTH);

        JPanel btn = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btn.setBackground(Color.WHITE);
        JButton closeBtn = UIHelper.grayButton("Đóng"); closeBtn.addActionListener(e -> dlg.dispose()); btn.add(closeBtn);

        JPanel wrap = new JPanel(new BorderLayout()); wrap.add(p, BorderLayout.CENTER); wrap.add(btn, BorderLayout.SOUTH);
        dlg.setContentPane(wrap); dlg.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);
        if (!UIHelper.confirm(this, "Xóa thông báo này?")) return;
        if (dao.delete(id)) { loadData(); }
        else UIHelper.showError(this, "Xóa thất bại!");
    }
}
