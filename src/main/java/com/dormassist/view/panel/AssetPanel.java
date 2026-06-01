package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.AssetDAO;
import com.dormassist.dao.RoomDAO;
import com.dormassist.model.Asset;
import com.dormassist.model.Room;
import com.dormassist.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AssetPanel extends JPanel {
    private final AssetDAO dao = new AssetDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private JTable table;
    private JTextField searchField;
    private List<Asset> allData;
    private JButton editBtn, deleteBtn;

    public AssetPanel() {
        setLayout(new BorderLayout()); setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20)); buildUI(); loadData();
    }

    private void buildUI() {
        JPanel tb = new JPanel(new BorderLayout(10, 0)); tb.setOpaque(false); tb.setBorder(new EmptyBorder(0, 0, 14, 0));
        searchField = UIHelper.searchField("Tìm tên, mã tài sản, phòng...");
        searchField.getDocument().addDocumentListener(simpleListener());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); left.setOpaque(false); left.add(searchField);
        tb.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); right.setOpaque(false);
        JButton refreshBtn = UIHelper.grayButton("Làm mới"); refreshBtn.addActionListener(e -> loadData());
        editBtn   = UIHelper.infoButton("Chỉnh sửa");
        deleteBtn = UIHelper.dangerButton("Xóa");
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        editBtn.setEnabled(false); deleteBtn.setEnabled(false);
        right.add(refreshBtn);
        if (Session.canManageAssets()) {
            JButton addBtn = UIHelper.primaryButton("+ Thêm tài sản"); addBtn.addActionListener(e -> showDialog(null));
            right.add(addBtn); right.add(editBtn); right.add(deleteBtn);
        }
        tb.add(right, BorderLayout.EAST);
        add(tb, BorderLayout.NORTH);

        String[] cols = {"ID", "Tên tài sản", "Mã TS", "Loại", "Phòng", "Số lượng", "Đơn giá (đ)", "Tình trạng"};
        table = UIHelper.styledTable(cols);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = table.getSelectedRow() >= 0;
            editBtn.setEnabled(sel); deleteBtn.setEnabled(sel);
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) editSelected(); }
        });
        add(UIHelper.tableScroll(table), BorderLayout.CENTER);
    }

    private void loadData() { UIHelper.runAsync(dao::getAll, data -> { allData = data; renderTable(allData); }); }

    private void renderTable(List<Asset> list) {
        UIHelper.clearTable(table);
        for (Asset a : list)
            UIHelper.addRow(table, a.getId(), a.getAssetName(),
                a.getAssetCode() != null ? a.getAssetCode() : "",
                AppConstants.getStatusDisplay(a.getCategory() != null ? a.getCategory() : "OTHER"),
                a.getRoomNumber() != null ? a.getRoomNumber() : "Kho chung",
                a.getQuantity(),
                UIHelper.formatCurrency(a.getPurchasePrice()),
                AppConstants.getStatusDisplay(a.getConditionStatus()));
    }

    private void filter() {
        if (allData == null) return;
        String q = searchField.getText().toLowerCase();
        if (q.isBlank() || q.startsWith("tim")) { renderTable(allData); return; }
        List<Asset> f = new java.util.ArrayList<>();
        for (Asset a : allData)
            if (a.getAssetName().toLowerCase().contains(q) ||
                (a.getAssetCode() != null && a.getAssetCode().toLowerCase().contains(q)) ||
                (a.getRoomNumber() != null && a.getRoomNumber().toLowerCase().contains(q)))
                f.add(a);
        renderTable(f);
    }

    private void editSelected() {
        int row = table.getSelectedRow(); if (row < 0) { UIHelper.showWarning(this, "Chọn tài sản trước!"); return; }
        int id = (int) table.getValueAt(row, 0);
        Asset found = allData.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
        showDialog(found);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);
        String name = (String) table.getValueAt(row, 1);
        if (!UIHelper.confirm(this, "Xóa tài sản: " + name + "?")) return;
        if (dao.delete(id)) { loadData(); }
        else UIHelper.showError(this, "Xóa thất bại!");
    }

    private void showDialog(Asset asset) {
        boolean isNew = asset == null;
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), isNew ? "Thêm tài sản" : "Chỉnh sửa tài sản", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(460, 440); dlg.setLocationRelativeTo(this);
        JPanel main = new JPanel(new BorderLayout()); main.setBackground(AppConstants.BG_MAIN);
        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(7, 6, 7, 6); g.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameF  = UIHelper.styledField();
        JTextField codeF  = UIHelper.styledField();
        JTextField qtyF   = UIHelper.styledField(); qtyF.setText("1");
        JTextField priceF = UIHelper.styledField(); priceF.setText("0");

        String[] catVN = {"Giường", "Tủ", "Điều hòa", "Quạt", "Bàn học", "Ghe", "Khác"};
        String[] catCode = {"BED", "LOCKER", "AC", "FAN", "DESK", "CHAIR", "OTHER"};
        JComboBox<String> catCb = UIHelper.styledCombo(catVN);

        String[] condVN = {"Tốt", "Bình thường", "Hỏng", "Mất"};
        String[] condCode = {"GOOD", "FAIR", "BROKEN", "MISSING"};
        JComboBox<String> condCb = UIHelper.styledCombo(condVN);

        List<Room> rooms = roomDAO.getAll();
        JComboBox<Object> roomCb = new JComboBox<>();
        roomCb.addItem("-- Kho / Chung --"); rooms.forEach(roomCb::addItem); roomCb.setFont(AppConstants.FONT_BODY);
        JTextArea notesArea = UIHelper.styledTextArea(3, 20);

        if (asset != null) {
            nameF.setText(asset.getAssetName()); codeF.setText(asset.getAssetCode() != null ? asset.getAssetCode() : "");
            qtyF.setText(String.valueOf(asset.getQuantity())); priceF.setText(String.valueOf((long) asset.getPurchasePrice()));
            for (int i = 0; i < catCode.length; i++) if (catCode[i].equals(asset.getCategory())) { catCb.setSelectedIndex(i); break; }
            for (int i = 0; i < condCode.length; i++) if (condCode[i].equals(asset.getConditionStatus())) { condCb.setSelectedIndex(i); break; }
            notesArea.setText(asset.getNotes() != null ? asset.getNotes() : "");
            if (asset.getRoomId() > 0) for (int i = 0; i < roomCb.getItemCount(); i++) {
                Object it = roomCb.getItemAt(i); if (it instanceof Room && ((Room)it).getId() == asset.getRoomId()) { roomCb.setSelectedIndex(i); break; }
            }
        }

        UIHelper.addFormRow(form, g, 0, "Tên tài sản: *", nameF);
        UIHelper.addFormRow(form, g, 1, "Mã tài sản:", codeF);
        UIHelper.addFormRow(form, g, 2, "Loại:", catCb);
        UIHelper.addFormRow(form, g, 3, "Phòng:", roomCb);
        UIHelper.addFormRow(form, g, 4, "Số lượng:", qtyF);
        UIHelper.addFormRow(form, g, 5, "Đơn giá (đ):", priceF);
        UIHelper.addFormRow(form, g, 6, "Tình trạng:", condCb);
        UIHelper.addFormRow(form, g, 7, "Ghi chú:", UIHelper.smoothScroll(new JScrollPane(notesArea)));
        main.add(UIHelper.smoothScroll(new JScrollPane(form)), BorderLayout.CENTER);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12)); btnP.setBackground(AppConstants.BG_MAIN);
        JButton cancelBtn = UIHelper.grayButton("Hủy"); cancelBtn.addActionListener(e -> dlg.dispose()); btnP.add(cancelBtn);
        if (Session.canManageAssets()) {
            JButton saveBtn = UIHelper.primaryButton(isNew ? "Thêm" : "Lưu");
            saveBtn.addActionListener(e -> {
                if (nameF.getText().trim().isEmpty()) { UIHelper.showWarning(dlg, "Tên tài sản không được trống!"); return; }
                Asset a = isNew ? new Asset() : asset;
                a.setAssetName(nameF.getText().trim()); a.setAssetCode(codeF.getText().trim());
                a.setCategory(catCode[catCb.getSelectedIndex()]); a.setConditionStatus(condCode[condCb.getSelectedIndex()]);
                a.setNotes(notesArea.getText().trim());
                try { a.setQuantity(Integer.parseInt(qtyF.getText().trim())); } catch (Exception ignored) { a.setQuantity(1); }
                try { a.setPurchasePrice(Double.parseDouble(priceF.getText().trim())); } catch (Exception ignored) {}
                Object rs = roomCb.getSelectedItem(); a.setRoomId(rs instanceof Room ? ((Room)rs).getId() : 0);
                boolean ok = isNew ? dao.insert(a) : dao.update(a);
                if (ok) { UIHelper.showSuccess(dlg, "Đã lưu!"); dlg.dispose(); loadData(); }
                else UIHelper.showError(dlg, "Lưu thất bại!");
            }); btnP.add(saveBtn);
        }
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
