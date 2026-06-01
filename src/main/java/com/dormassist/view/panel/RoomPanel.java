package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.RoomDAO;
import com.dormassist.model.Building;
import com.dormassist.model.Room;
import com.dormassist.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class RoomPanel extends JPanel {
    private final RoomDAO dao = new RoomDAO();
    private JTable table;
    private JTextField searchField;
    private List<Room> allData;

    // Nút hành động hiển thị thường xuyên (không cần chuột phải)
    private JButton editBtn, deleteBtn;

    public RoomPanel() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20,20,20,20));
        buildUI(); loadData();
    }

    private void buildUI() {
        // ===== TOOLBAR =====
        JPanel toolbar = new JPanel(new BorderLayout(10,0));
        toolbar.setOpaque(false); toolbar.setBorder(new EmptyBorder(0,0,14,0));

        searchField = UIHelper.searchField("Tìm số phòng, tòa nhà...");
        searchField.getDocument().addDocumentListener(simpleListener());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); left.setOpaque(false);
        left.add(searchField);
        toolbar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); right.setOpaque(false);
        JButton refreshBtn = UIHelper.grayButton("Làm mới"); refreshBtn.addActionListener(e -> loadData());
        editBtn   = UIHelper.infoButton("Chỉnh sửa");
        deleteBtn = UIHelper.dangerButton("Xóa phòng");
        editBtn.addActionListener(e   -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        editBtn.setEnabled(false); deleteBtn.setEnabled(false);
        right.add(refreshBtn);
        if (Session.isAdmin()) {
            JButton addBtn = UIHelper.primaryButton("+ Thêm phòng");
            addBtn.addActionListener(e -> showDialog(null));
            right.add(addBtn);
            right.add(editBtn); right.add(deleteBtn);
        }
        toolbar.add(right, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        // ===== BẢNG =====
        String[] cols = {"ID","Số phòng","Tầng","Tòa nhà","Sức chứa","Đang ở","Giá thuê","Loại phòng","Trạng thái"};
        table = UIHelper.styledTable(cols);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(8).setPreferredWidth(120);

        // Kích hoạt nút khi chọn hàng
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = table.getSelectedRow() >= 0;
            editBtn.setEnabled(sel); deleteBtn.setEnabled(sel);
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { if(e.getClickCount()==2) editSelected(); }
        });

        add(UIHelper.tableScroll(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT)); bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(6,0,0,0));
        bottom.add(UIHelper.labelMuted("Chọn hàng rồi dùng các nút phía trên để thao tác | Nhấn đúp để xem chi tiết"));
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData() { UIHelper.runAsync(dao::getAll, data -> { allData=data; renderTable(allData); }); }

    private void renderTable(List<Room> list) {
        UIHelper.clearTable(table);
        for (Room r : list)
            UIHelper.addRow(table, r.getId(), r.getRoomNumber(), r.getFloor(),
                r.getBuildingName()!=null?r.getBuildingName():"",
                r.getCapacity(), r.getCurrentOccupants(),
                UIHelper.formatCurrency(r.getRentPrice()),
                AppConstants.getStatusDisplay(r.getRoomType()),
                AppConstants.getStatusDisplay(r.getStatus()));
    }

    private void filter() {
        if(allData==null) return;
        String q=searchField.getText().toLowerCase();
        if(q.isBlank()||q.startsWith("tim")||q.startsWith("tìm")) { renderTable(allData); return; }
        List<Room> f=new java.util.ArrayList<>();
        for(Room r:allData)
            if(r.getRoomNumber().toLowerCase().contains(q)||
               (r.getBuildingName()!=null&&r.getBuildingName().toLowerCase().contains(q))||
               AppConstants.getStatusDisplay(r.getStatus()).toLowerCase().contains(q))
                f.add(r);
        renderTable(f);
    }

    private void editSelected() {
        int row=table.getSelectedRow(); if(row<0){UIHelper.showWarning(this,"Chọn một phòng trước!"); return;}
        int id=(int)table.getValueAt(row,0);
        showDialog(dao.getById(id));
    }

    private void deleteSelected() {
        int row=table.getSelectedRow(); if(row<0) return;
        int id=(int)table.getValueAt(row,0);
        String rn=(String)table.getValueAt(row,1);
        if(!UIHelper.confirm(this,"Xóa phòng "+rn+"?\nThao tác này không thể hoàn tác!")) return;
        if(dao.delete(id)){loadData();}
        else UIHelper.showError(this,"Không thể xóa phòng này (có dữ liệu liên quan)!");
    }

    private void showDialog(Room room) {
        boolean isNew = room==null;
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            isNew?"Thêm phòng mới":"Chỉnh sửa phòng - "+room.getRoomNumber(), Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480,430); dlg.setLocationRelativeTo(this);

        JPanel main=new JPanel(new BorderLayout()); main.setBackground(AppConstants.BG_MAIN);
        JPanel form=new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(20,24,20,24));
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(7,6,7,6); g.fill=GridBagConstraints.HORIZONTAL;

        JTextField noField  = UIHelper.styledField();
        JTextField flField  = UIHelper.styledField();
        JTextField capField = UIHelper.styledField();
        JTextField priceField=UIHelper.styledField();

        List<Building> blds = dao.getAllBuildings();
        JComboBox<Building> bldCombo = new JComboBox<>(); blds.forEach(bldCombo::addItem); bldCombo.setFont(AppConstants.FONT_BODY);

        String[] statuses = {"AVAILABLE","FULL","MAINTENANCE","CLOSED"};
        String[] statusVN = {"Còn trống","Đầy","Đang bảo trì","Đóng cửa"};
        JComboBox<String> statCombo = UIHelper.styledCombo(statusVN);

        String[] types   = {"STANDARD","VIP","DISABLED_ACCESS"};
        String[] typeVN  = {"Tiêu chuẩn","Cao cấp","Đặc biệt"};
        JComboBox<String> typeCombo = UIHelper.styledCombo(typeVN);

        JTextArea notesArea = UIHelper.styledTextArea(3,20);

        if(room!=null){
            noField.setText(room.getRoomNumber()); flField.setText(String.valueOf(room.getFloor()));
            capField.setText(String.valueOf(room.getCapacity())); priceField.setText(String.valueOf((long)room.getRentPrice()));
            notesArea.setText(room.getNotes()!=null?room.getNotes():"");
            for(int i=0;i<statuses.length;i++) if(statuses[i].equals(room.getStatus())){statCombo.setSelectedIndex(i);break;}
            for(int i=0;i<types.length;i++)    if(types[i].equals(room.getRoomType())){typeCombo.setSelectedIndex(i);break;}
            for(int i=0;i<bldCombo.getItemCount();i++) if(bldCombo.getItemAt(i).getId()==room.getBuildingId()){bldCombo.setSelectedIndex(i);break;}
        }

        UIHelper.addFormRow(form,g,0,"Số phòng: *",noField);
        UIHelper.addFormRow(form,g,1,"Tầng: *",flField);
        UIHelper.addFormRow(form,g,2,"Tòa nhà:",bldCombo);
        UIHelper.addFormRow(form,g,3,"Sức chứa: *",capField);
        UIHelper.addFormRow(form,g,4,"Giá thuê (đ):",priceField);
        UIHelper.addFormRow(form,g,5,"Trạng thái:",statCombo);
        UIHelper.addFormRow(form,g,6,"Loại phòng:",typeCombo);
        UIHelper.addFormRow(form,g,7,"Ghi chú:",UIHelper.smoothScroll(new JScrollPane(notesArea)));
        main.add(UIHelper.smoothScroll(new JScrollPane(form)),BorderLayout.CENTER);

        JPanel btnP=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,12)); btnP.setBackground(AppConstants.BG_MAIN);
        JButton cancelBtn=UIHelper.grayButton("Hủy"); cancelBtn.addActionListener(e->dlg.dispose()); btnP.add(cancelBtn);
        if(Session.isAdmin()){
            JButton saveBtn=UIHelper.primaryButton(isNew?"Thêm phòng":"Lưu thay đổi");
            saveBtn.addActionListener(e->{
                if(noField.getText().trim().isEmpty()||flField.getText().trim().isEmpty()||capField.getText().trim().isEmpty()){
                    UIHelper.showWarning(dlg,"Vui lòng điền đầy đủ thông tin bắt buộc!"); return;}
                Room r2=isNew?new Room():room;
                r2.setRoomNumber(noField.getText().trim()); r2.setFloor(Integer.parseInt(flField.getText().trim()));
                r2.setCapacity(Integer.parseInt(capField.getText().trim()));
                r2.setStatus(statuses[statCombo.getSelectedIndex()]);
                r2.setRoomType(types[typeCombo.getSelectedIndex()]);
                r2.setNotes(notesArea.getText().trim());
                try{r2.setRentPrice(Double.parseDouble(priceField.getText().replaceAll("[^\\d.]","")));}catch(Exception ignored){}
                Building b=(Building)bldCombo.getSelectedItem(); if(b!=null)r2.setBuildingId(b.getId());
                boolean ok=isNew?dao.insert(r2):dao.update(r2);
                if(ok){UIHelper.showSuccess(dlg,isNew?"Đã thêm phòng!":"Đã cập nhật!"); dlg.dispose(); loadData();}
                else UIHelper.showError(dlg,"Lưu thất bại! Kiểm tra số phòng có bị trùng không.");
            }); btnP.add(saveBtn);
        }
        main.add(btnP,BorderLayout.SOUTH);
        dlg.setContentPane(main); dlg.setVisible(true);
    }

    private javax.swing.event.DocumentListener simpleListener() {
        return new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        };
    }
}
