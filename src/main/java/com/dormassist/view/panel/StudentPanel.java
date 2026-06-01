package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.*;
import com.dormassist.model.*;
import com.dormassist.util.UIHelper;

import javax.swing.table.TableColumn;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.ParseException;
import java.util.List;

public class StudentPanel extends JPanel {
    private final StudentDAO dao = new StudentDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private JTable table;
    private JTextField searchField;
    private List<Student> allData;
    private JButton editBtn, deleteBtn;

    private static final int COL_STT = 0;
    private static final int COL_ID_DB = 1;
    private static final int COL_NAME = 2;
    private static final int COL_CODE = 3;
    private static final int COL_ROOM = 4;
    private static final int COL_GENDER = 5;
    private static final int COL_PHONE = 6;
    private static final int COL_POINT = 7;
    private static final int COL_STATUS = 8;

    public StudentPanel() {
        setLayout(new BorderLayout()); setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20,20,20,20)); buildUI(); loadData();
    }

    private void buildUI() {
        JPanel tb = new JPanel(new BorderLayout(10,0));
        tb.setOpaque(false); tb.setBorder(new EmptyBorder(0,0,14,0));
        searchField = UIHelper.searchField("Tìm tên, MSSV, số phòng...");
        searchField.getDocument().addDocumentListener(simpleListener());

        JPanel left=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); left.setOpaque(false); left.add(searchField);
        tb.add(left,BorderLayout.WEST);

        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); right.setOpaque(false);
        JButton refreshBtn=UIHelper.grayButton("Làm mới"); refreshBtn.addActionListener(e->loadData());
        editBtn  =UIHelper.infoButton("Xem / Sửa");
        deleteBtn=UIHelper.dangerButton("Chuyển đi");
        editBtn.addActionListener(e->editSelected()); deleteBtn.addActionListener(e->deleteSelected());
        editBtn.setEnabled(false); deleteBtn.setEnabled(false);
        right.add(refreshBtn);
        if(Session.isAdmin()||Session.isBase1()||Session.isBase2()){
            JButton addBtn=UIHelper.primaryButton("+ Thêm sinh viên"); addBtn.addActionListener(e->showDialog(null));
            right.add(addBtn); right.add(editBtn); right.add(deleteBtn);
        } else {
            right.add(editBtn);
        }
        tb.add(right,BorderLayout.EAST); add(tb,BorderLayout.NORTH);

        String[] cols = {
                "STT",
                "ID_DB",
                "Họ tên",
                "MSSV",
                "Phòng",
                "Giới tính",
                "Điện thoại",
                "Điểm RL",
                "Trạng thái"
        };

        table = UIHelper.styledTable(cols);

// Giữ thứ tự hiển thị cố định theo thứ tự tạo, không cho JTable tự sort lại khi bấm tiêu đề cột
        table.setAutoCreateRowSorter(false);
        table.setRowSorter(null);

        table.getColumnModel().getColumn(COL_STT).setMaxWidth(60);
        table.getColumnModel().getColumn(COL_NAME).setPreferredWidth(160);
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            boolean selected = row >= 0;

            editBtn.setEnabled(selected);

            if (deleteBtn != null) {
                if (!selected) {
                    deleteBtn.setEnabled(false);
                } else {
                    String status = String.valueOf(table.getValueAt(row, 7));
                    deleteBtn.setEnabled(!"Đã chuyển đi".equals(status));
                }
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseClicked(java.awt.event.MouseEvent e){if(e.getClickCount()==2)editSelected();}
        });
        add(UIHelper.tableScroll(table),BorderLayout.CENTER);
        hideIdDbColumn();
    }

    private void loadData() { UIHelper.runAsync(dao::getAll, data -> { allData=data; renderTable(allData); }); }

    private void renderTable(List<Student> list) {
        UIHelper.clearTable(table);

        List<Student> data = list == null ? new ArrayList<>() : new ArrayList<>(list);

        // ID thật tăng dần gần nhất với thứ tự tạo trong database
        data.sort(Comparator.comparingInt(Student::getId));

        int stt = 1;

        for (Student s : data) {
            UIHelper.addRow(
                    table,
                    stt++,
                    s.getId(),
                    s.getFullName(),
                    s.getStudentCode() != null ? s.getStudentCode() : "",
                    s.getRoomNumber() != null ? s.getRoomNumber() : "Chưa xếp phòng",
                    AppConstants.getStatusDisplay(s.getGender() != null ? s.getGender() : "OTHER"),
                    s.getPhone() != null ? s.getPhone() : "",
                    s.getDisciplinePoints(),
                    AppConstants.getStatusDisplay(s.getStatus())
            );
        }

        hideIdDbColumn();
    }

    private void filter() {
        if(allData==null) return;
        String q=searchField.getText().toLowerCase();
        if(q.isBlank()||q.startsWith("tim")){renderTable(allData);return;}
        List<Student> f=new java.util.ArrayList<>();
        for(Student s:allData)
            if(s.getFullName().toLowerCase().contains(q)||
               (s.getStudentCode()!=null&&s.getStudentCode().toLowerCase().contains(q))||
               (s.getRoomNumber()!=null&&s.getRoomNumber().toLowerCase().contains(q))||
               (s.getPhone()!=null&&s.getPhone().contains(q)))
                f.add(s);
        renderTable(f);
    }

    private void editSelected() {
        int id = getSelectedStudentId();

        if (id <= 0) {
            UIHelper.showWarning(this, "Chọn sinh viên trước!");
            return;
        }

        showDialog(dao.getById(id));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        int id = (int) table.getValueAt(row, 0);
        String name = String.valueOf(table.getValueAt(row, 1));
        String status = String.valueOf(table.getValueAt(row, 7));

        if ("Đã chuyển đi".equals(status)) {
            UIHelper.showWarning(this, "Sinh viên này đã ở trạng thái Đã chuyển đi.");
            return;
        }

        String message =
                "Cho sinh viên \"" + name + "\" chuyển đi?\n\n"
                        + "Hệ thống sẽ thực hiện:\n"
                        + "- Chuyển trạng thái hồ sơ sang Đã chuyển đi\n"
                        + "- Gỡ sinh viên khỏi phòng hiện tại\n"
                        + "- Khóa tài khoản đăng nhập nếu sinh viên đã có tài khoản\n"
                        + "- Giữ lại toàn bộ lịch sử liên quan";

        if (!UIHelper.confirm(this, message)) return;

        if (dao.moveOutAndLockAccount(id, Session.getUserId())) {
            UIHelper.showSuccess(this, "Đã chuyển sinh viên sang trạng thái Đã chuyển đi và khóa tài khoản liên kết nếu có.");
            loadData();
        } else {
            UIHelper.showError(this, "Thao tác thất bại!");
        }
    }

    private void showDialog(Student s) {
        boolean isNew=s==null;
        boolean canEdit=Session.isAdmin()||Session.isBase1()||Session.isBase2();
        JDialog dlg=new JDialog(SwingUtilities.getWindowAncestor(this),
            isNew?"Thêm sinh viên mới":"Thông tin sinh viên",Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(520,540); dlg.setLocationRelativeTo(this);

        JPanel main=new JPanel(new BorderLayout()); main.setBackground(AppConstants.BG_MAIN);
        JTabbedPane tabs=new JTabbedPane(); tabs.setFont(AppConstants.FONT_BODY);

        // Tab 1: Thông tin cá nhân
        JPanel p1=new JPanel(new GridBagLayout()); p1.setBackground(Color.WHITE); p1.setBorder(new EmptyBorder(16,20,16,20));
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(6,6,6,6); g.fill=GridBagConstraints.HORIZONTAL;
        JTextField nameF=UIHelper.styledField(), codeF=UIHelper.styledField(), idcardF=UIHelper.styledField();
        JTextField phoneF=UIHelper.styledField(), emailF=UIHelper.styledField(), hometownF=UIHelper.styledField(), dobF=UIHelper.styledField();
        dobF.setToolTipText("dd/MM/yyyy");
        String[] genVN={"Nam","Nữ","Khác"}; String[] genCode={"MALE","FEMALE","OTHER"};
        JComboBox<String> genderCb=UIHelper.styledCombo(genVN);
        String[] statVN={"Đang cư trú","Tạm vắng","Đã chuyển đi"}; String[] statCode={"ACTIVE","TEMPORARY_ABSENT","MOVED_OUT"};
        JComboBox<String> statusCb=UIHelper.styledCombo(statVN);
        JTextArea notesArea=UIHelper.styledTextArea(3,20);

        if(s!=null){
            nameF.setText(s.getFullName()); codeF.setText(s.getStudentCode()!=null?s.getStudentCode():"");
            idcardF.setText(s.getIdCard()!=null?s.getIdCard():"");
            phoneF.setText(s.getPhone()!=null?s.getPhone():""); emailF.setText(s.getEmail()!=null?s.getEmail():"");
            hometownF.setText(s.getHometown()!=null?s.getHometown():""); notesArea.setText(s.getNotes()!=null?s.getNotes():"");
            if(s.getDob()!=null)dobF.setText(UIHelper.DATE_FMT.format(s.getDob()));
            for(int i=0;i<genCode.length;i++) if(genCode[i].equals(s.getGender())){genderCb.setSelectedIndex(i);break;}
            for(int i=0;i<statCode.length;i++) if(statCode[i].equals(s.getStatus())){statusCb.setSelectedIndex(i);break;}
        }

        UIHelper.addFormRow(p1,g,0,"Họ và tên: *",nameF);
        UIHelper.addFormRow(p1,g,1,"MSSV:",codeF);
        UIHelper.addFormRow(p1,g,2,"CMND/CCCD:",idcardF);
        UIHelper.addFormRow(p1,g,3,"Ngày sinh:",dobF);
        UIHelper.addFormRow(p1,g,4,"Giới tính:",genderCb);
        UIHelper.addFormRow(p1,g,5,"Điện thoại:",phoneF);
        UIHelper.addFormRow(p1,g,6,"Email:",emailF);
        UIHelper.addFormRow(p1,g,7,"Quê quán:",hometownF);
        UIHelper.addFormRow(p1,g,8,"Trạng thái:",statusCb);
        UIHelper.addFormRow(p1,g,9,"Ghi chú:",UIHelper.smoothScroll(new JScrollPane(notesArea)));
        tabs.addTab("Thông tin cá nhân",UIHelper.smoothScroll(new JScrollPane(p1)));

        // Tab 2: Phòng ở
        JPanel p2=new JPanel(new GridBagLayout()); p2.setBackground(Color.WHITE); p2.setBorder(new EmptyBorder(16,20,16,20));
        GridBagConstraints g2=new GridBagConstraints(); g2.insets=new Insets(6,6,6,6); g2.fill=GridBagConstraints.HORIZONTAL;
        List<Room> rooms=roomDAO.getAll();
        JComboBox<Object> roomCb=new JComboBox<>(); roomCb.addItem("-- Chưa xếp phòng --"); rooms.forEach(roomCb::addItem); roomCb.setFont(AppConstants.FONT_BODY);
        JTextField joinF=UIHelper.styledField(), leaveF=UIHelper.styledField();
        joinF.setToolTipText("dd/MM/yyyy"); leaveF.setToolTipText("dd/MM/yyyy");
        if(s!=null){
            if(s.getJoinDate()!=null)joinF.setText(UIHelper.DATE_FMT.format(s.getJoinDate()));
            if(s.getExpectedLeaveDate()!=null)leaveF.setText(UIHelper.DATE_FMT.format(s.getExpectedLeaveDate()));
            if(s.getRoomId()>0)for(int i=0;i<roomCb.getItemCount();i++){Object it=roomCb.getItemAt(i);if(it instanceof Room&&((Room)it).getId()==s.getRoomId()){roomCb.setSelectedIndex(i);break;}}
        }
        UIHelper.addFormRow(p2,g2,0,"Phòng ở:",roomCb);
        UIHelper.addFormRow(p2,g2,1,"Ngày vao:",joinF);
        UIHelper.addFormRow(p2,g2,2,"Ngày dự kiến ra:",leaveF);
        tabs.addTab("Phòng ở",p2);

        main.add(tabs,BorderLayout.CENTER);
        JPanel btnP=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,12)); btnP.setBackground(AppConstants.BG_MAIN);
        JButton closeBtn=UIHelper.grayButton("Đóng"); closeBtn.addActionListener(e->dlg.dispose()); btnP.add(closeBtn);
        if(canEdit){
            JButton saveBtn=UIHelper.primaryButton(isNew?"Thêm sinh viên":"Lưu thay đổi");
            saveBtn.addActionListener(e->{
                if(nameF.getText().trim().isEmpty()){UIHelper.showWarning(dlg,"Họ tên không được để trống!"); return;}
                Student st=isNew?new Student():s;
                st.setFullName(nameF.getText().trim()); st.setStudentCode(codeF.getText().trim());
                st.setIdCard(idcardF.getText().trim());
                st.setGender(genCode[genderCb.getSelectedIndex()]);
                st.setPhone(phoneF.getText().trim()); st.setEmail(emailF.getText().trim());
                st.setHometown(hometownF.getText().trim()); st.setNotes(notesArea.getText().trim());
                String selectedStatus = statCode[statusCb.getSelectedIndex()];

                if (!isNew && "MOVED_OUT".equals(selectedStatus)
                        && s != null
                        && !"MOVED_OUT".equals(s.getStatus())) {
                    UIHelper.showWarning(
                            dlg,
                            "Để chuyển sinh viên đi và khóa tài khoản đúng quy trình,\n"
                                    + "vui lòng dùng nút \"Chuyển đi\" ở danh sách sinh viên."
                    );
                    return;
                }

                st.setStatus(selectedStatus);
                try{if(!dobF.getText().trim().isEmpty())st.setDob(UIHelper.DATE_FMT.parse(dobF.getText().trim()));}catch(ParseException ignored){}
                try{if(!joinF.getText().trim().isEmpty())st.setJoinDate(UIHelper.DATE_FMT.parse(joinF.getText().trim()));}catch(ParseException ignored){}
                try{if(!leaveF.getText().trim().isEmpty())st.setExpectedLeaveDate(UIHelper.DATE_FMT.parse(leaveF.getText().trim()));}catch(ParseException ignored){}
                Object rs=roomCb.getSelectedItem(); st.setRoomId(rs instanceof Room?((Room)rs).getId():0);
                boolean ok=isNew?dao.insert(st):dao.update(st);
                if(ok){UIHelper.showSuccess(dlg,isNew?"Đã thêm!":"Đã cập nhật!"); dlg.dispose(); loadData();}
                else UIHelper.showError(dlg,"Lưu thất bại!");
            }); btnP.add(saveBtn);
        }
        main.add(btnP,BorderLayout.SOUTH);
        dlg.setContentPane(main); dlg.setVisible(true);
    }

    private javax.swing.event.DocumentListener simpleListener() {
        return new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){filter();}
            public void removeUpdate(javax.swing.event.DocumentEvent e){filter();}
            public void changedUpdate(javax.swing.event.DocumentEvent e){filter();}
        };
    }

    private void hideIdDbColumn() {
        TableColumn idCol = table.getColumnModel().getColumn(COL_ID_DB);
        idCol.setMinWidth(0);
        idCol.setPreferredWidth(0);
        idCol.setMaxWidth(0);
        idCol.setResizable(false);
    }

    private int getSelectedStudentId() {
        int row = table.getSelectedRow();
        if (row < 0) return -1;

        Object value = table.getValueAt(row, COL_ID_DB);
        return Integer.parseInt(String.valueOf(value));
    }
}
