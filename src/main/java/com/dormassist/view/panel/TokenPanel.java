package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.TokenDAO;
import com.dormassist.model.Token;
import com.dormassist.view.ui.Toast;
import com.dormassist.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

public class TokenPanel extends JPanel {
    private final TokenDAO dao = new TokenDAO();

    private JTable table;
    private List<Token> allData = new ArrayList<>();

    private JButton copyBtn, deleteBtn;
    private JTextField searchField;
    private JComboBox<String> filterRoleCb;
    private JComboBox<String> createRoleCb;
    private JLabel statusLabel;

    // Cột hiển thị
    private static final int COL_STT = 0;

    // Cột ID thật trong database, dùng để xóa nhưng sẽ bị ẩn trên giao diện
    private static final int COL_ID_DB = 1;

    private static final int COL_CODE = 2;
    private static final int COL_ROLE = 3;
    private static final int COL_STATUS = 4;
    private static final int COL_CREATED_AT = 5;
    private static final int COL_USED_AT = 6;
    private static final int COL_USED_BY = 7;

    private static final String[] ROLE_DISPLAY = {
            "Sinh viên",
            "Trưởng tầng",
            "Trưởng phòng",
            "Quản trị viên"
    };

    private static final String[] ROLE_CODE = {
            AppConstants.ROLE_BASE3,
            AppConstants.ROLE_BASE1,
            AppConstants.ROLE_BASE2,
            AppConstants.ROLE_ADMIN_SUPER
    };

    public TokenPanel() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);

        root.add(titleBlock(), BorderLayout.NORTH);
        root.add(tableCard(), BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);
    }

    private JPanel titleBlock() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 2, 0));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Mã Token");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(AppConstants.TEXT_MAIN);

        JLabel sub = UIHelper.labelMuted("Cấp mã token cho người dùng đăng ký theo vai trò");

        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);

        p.add(left, BorderLayout.WEST);

        statusLabel = UIHelper.labelMuted("Đang tải dữ liệu...");
        p.add(statusLabel, BorderLayout.EAST);

        return p;
    }

    private JPanel tableCard() {
        JPanel card = UIHelper.card();
        card.setLayout(new BorderLayout(0, 12));

        card.add(toolbar(), BorderLayout.NORTH);

        String[] cols = {
                "STT",
                "ID_DB",
                "Mã token",
                "Vai trò",
                "Trạng thái",
                "Ngày tạo",
                "Ngày sử dụng",
                "Người sử dụng"
        };

        table = UIHelper.styledTable(cols);
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(new StatusRenderer());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionState();
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    copySelected();
                }
            }
        });

        JScrollPane scroll = UIHelper.tableScroll(table);
        card.add(scroll, BorderLayout.CENTER);

        // UIHelper.tableScroll(table) có gọi configureColumns(table),
        // nên cần gọi lại sau cùng để cột ID_DB tiếp tục bị ẩn.
        applyTokenTableColumns();

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(
                UIHelper.labelMuted("Token mới tạo theo dạng dễ nhớ: SVVKU-0001, TTVKU-0001, TPVKU-0001, ADMINVKU-0001."),
                BorderLayout.WEST
        );

        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel toolbar() {
        JPanel tb = new JPanel(new BorderLayout(10, 8));
        tb.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        searchField = UIHelper.searchField("Tìm mã token, vai trò, người sử dụng...");
        searchField.setPreferredSize(new Dimension(290, AppConstants.FIELD_H));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        filterRoleCb = UIHelper.styledCombo(
                "Tất cả",
                "Sinh viên",
                "Trưởng tầng",
                "Trưởng phòng",
                "Quản trị viên"
        );
        filterRoleCb.setPreferredSize(new Dimension(150, AppConstants.FIELD_H));
        filterRoleCb.addActionListener(e -> filter());

        left.add(searchField);
        left.add(filterRoleCb);
        tb.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton refreshBtn = UIHelper.grayButton("Làm mới");
        refreshBtn.addActionListener(e -> loadData());

        createRoleCb = UIHelper.styledCombo(ROLE_DISPLAY);
        createRoleCb.setPreferredSize(new Dimension(150, AppConstants.FIELD_H));

        JButton genBtn = UIHelper.primaryButton("+ Tạo token");
        genBtn.addActionListener(e -> createToken());

        copyBtn = UIHelper.infoButton("Sao chép mã");
        copyBtn.addActionListener(e -> copySelected());
        copyBtn.setEnabled(false);

        deleteBtn = UIHelper.dangerButton("Xóa token");
        deleteBtn.addActionListener(e -> deleteSelected());
        deleteBtn.setEnabled(false);

        right.add(refreshBtn);
        right.add(UIHelper.labelMuted("Tạo token cho"));
        right.add(createRoleCb);
        right.add(genBtn);
        right.add(copyBtn);
        right.add(deleteBtn);

        tb.add(right, BorderLayout.EAST);
        return tb;
    }

    private void applyTokenTableColumns() {
        if (table == null || table.getColumnModel() == null) return;

        UIHelper.configureColumns(table);

        // Ẩn ID thật trong database
        TableColumn idCol = table.getColumnModel().getColumn(COL_ID_DB);
        idCol.setMinWidth(0);
        idCol.setPreferredWidth(0);
        idCol.setMaxWidth(0);
        idCol.setResizable(false);

        table.getColumnModel().getColumn(COL_STT).setMinWidth(50);
        table.getColumnModel().getColumn(COL_STT).setPreferredWidth(60);
        table.getColumnModel().getColumn(COL_STT).setMaxWidth(70);

        table.getColumnModel().getColumn(COL_CODE).setPreferredWidth(210);
        table.getColumnModel().getColumn(COL_ROLE).setPreferredWidth(150);
        table.getColumnModel().getColumn(COL_STATUS).setPreferredWidth(140);
        table.getColumnModel().getColumn(COL_CREATED_AT).setPreferredWidth(170);
        table.getColumnModel().getColumn(COL_USED_AT).setPreferredWidth(170);
        table.getColumnModel().getColumn(COL_USED_BY).setPreferredWidth(180);
    }

    private void loadData() {
        statusLabel.setText("Đang tải dữ liệu...");

        UIHelper.runAsync(dao::getAll, data -> {
            allData = data != null ? data : new ArrayList<>();
            filter();
            statusLabel.setText("Tổng số: " + allData.size() + " token");
        });
    }

    private void filter() {
        if (table == null || allData == null) return;

        String q = "";
        if (searchField != null) {
            q = searchField.getText().trim().toLowerCase();
            if (q.startsWith("tìm mã")) q = "";
        }

        String role = filterRoleCb == null ? "Tất cả" : String.valueOf(filterRoleCb.getSelectedItem());

        UIHelper.clearTable(table);

        int stt = 1;

        for (Token token : allData) {
            String tokenCode = token.getTokenCode() != null ? token.getTokenCode() : "";
            String roleVN = AppConstants.getRoleDisplay(token.getRole());
            String status = token.isUsed() ? "Đã sử dụng" : "Còn hiệu lực";
            String usedBy = token.getUsedByName() != null && !token.getUsedByName().trim().isEmpty()
                    ? token.getUsedByName().trim()
                    : "";

            boolean roleOk = role == null || "Tất cả".equals(role) || role.equals(roleVN);

            boolean qOk = q.isEmpty()
                    || tokenCode.toLowerCase().contains(q)
                    || roleVN.toLowerCase().contains(q)
                    || status.toLowerCase().contains(q)
                    || usedBy.toLowerCase().contains(q);

            if (roleOk && qOk) {
                UIHelper.addRow(
                        table,
                        stt++,
                        token.getId(),
                        tokenCode,
                        roleVN,
                        status,
                        UIHelper.formatDateTime(token.getCreatedAt()),
                        UIHelper.formatDateTime(token.getUsedAt()),
                        usedBy.isEmpty() ? "Chưa sử dụng" : usedBy
                );
            }
        }

        applyTokenTableColumns();
        updateActionState();
    }

    private void createToken() {
        int idx = createRoleCb.getSelectedIndex();
        if (idx < 0 || idx >= ROLE_CODE.length) {
            UIHelper.showWarning(this, "Vui lòng chọn vai trò cần tạo token.");
            return;
        }

        String role = ROLE_CODE[idx];
        String roleVN = ROLE_DISPLAY[idx];

        String code = dao.generateToken(role, Session.getUserId());

        if (code != null) {
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(code), null);

            Toast.show(this, "Đã tạo và sao chép token " + code + " cho " + roleVN + ".");
            loadData();
        } else {
            UIHelper.showError(this, "Tạo token thất bại. Vui lòng kiểm tra kết nối SQL Server.");
        }
    }

    private void copySelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String code = String.valueOf(table.getValueAt(row, COL_CODE));

        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(code), null);

        Toast.show(this, "Đã sao chép token: " + code);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        int id = Integer.parseInt(String.valueOf(table.getValueAt(row, COL_ID_DB)));
        String code = String.valueOf(table.getValueAt(row, COL_CODE));
        String status = String.valueOf(table.getValueAt(row, COL_STATUS));

        if (!"Còn hiệu lực".equals(status)) {
            UIHelper.showWarning(this, "Chỉ có thể xóa token còn hiệu lực.");
            return;
        }

        if (!UIHelper.confirm(this, "Xóa token " + code + "?\nToken này sẽ không thể dùng để đăng ký nữa.")) {
            return;
        }

        if (dao.delete(id)) {
            Toast.show(this, "Đã xóa token.");
            loadData();
        } else {
            UIHelper.showError(this, "Không thể xóa token đã sử dụng hoặc đang được tham chiếu.");
        }
    }

    private void updateActionState() {
        if (copyBtn == null || deleteBtn == null || table == null) return;

        int row = table.getSelectedRow();
        boolean selected = row >= 0;

        copyBtn.setEnabled(selected);

        if (!selected) {
            deleteBtn.setEnabled(false);
            return;
        }

        String status = String.valueOf(table.getValueAt(row, COL_STATUS));
        deleteBtn.setEnabled("Còn hiệu lực".equals(status));
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean selected,
                boolean focus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table,
                    value,
                    selected,
                    focus,
                    row,
                    column
            );

            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(AppConstants.FONT_BOLD);

            String status = value == null ? "" : value.toString();

            if (!selected) {
                label.setForeground(
                        "Còn hiệu lực".equals(status)
                                ? AppConstants.SUCCESS.darker()
                                : AppConstants.TEXT_MUTED
                );
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 252, 248));
            }

            return label;
        }
    }
}