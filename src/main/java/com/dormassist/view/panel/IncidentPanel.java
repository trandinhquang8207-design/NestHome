package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.IncidentDAO;
import com.dormassist.dao.RoomDAO;
import com.dormassist.model.Incident;
import com.dormassist.model.Room;
import com.dormassist.util.UIHelper;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.concurrent.*;

public class IncidentPanel extends JPanel {
    private final IncidentDAO dao    = new IncidentDAO();
    private final RoomDAO roomDAO    = new RoomDAO();
    private JTable table;
    private JTextField searchField;
    private List<Incident> allData;

    // Nút hành động thường xuyên
    private JButton viewBtn, resolveBtn, deleteBtn;

    // Đa luồng: cập nhật mỗi 10 giây
    private ScheduledExecutorService scheduler;

    public IncidentPanel() {
        setLayout(new BorderLayout()); setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20,20,20,20));
        buildUI(); loadData(); startAutoRefresh();
    }

    private void startAutoRefresh() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t=new Thread(r,"incident-refresh"); t.setDaemon(true); return t;
        });
        scheduler.scheduleAtFixedRate(()->SwingUtilities.invokeLater(this::loadData),
            10, 10, TimeUnit.SECONDS);
    }

    private void buildUI() {
        JPanel tb=new JPanel(new BorderLayout(10,0)); tb.setOpaque(false); tb.setBorder(new EmptyBorder(0,0,14,0));
        searchField=UIHelper.searchField("Tìm tiêu đề, phòng...");
        searchField.getDocument().addDocumentListener(simpleListener());
        JPanel left=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); left.setOpaque(false); left.add(searchField);
        tb.add(left,BorderLayout.WEST);

        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); right.setOpaque(false);
        JButton refreshBtn=UIHelper.grayButton("Làm mới"); refreshBtn.addActionListener(e->loadData());
        JButton addBtn=UIHelper.warningButton("+ Báo sự cố / Yêu cầu"); addBtn.addActionListener(e->showCreateDialog());

        viewBtn   =UIHelper.infoButton("Xem chi tiết");
        resolveBtn=UIHelper.successButton("Xác nhận đã sửa xong");
        deleteBtn =UIHelper.dangerButton("Xóa");

        viewBtn.addActionListener(e->viewSelected());
        resolveBtn.addActionListener(e->resolveSelected());
        deleteBtn.addActionListener(e->deleteSelected());

        viewBtn.setEnabled(false); resolveBtn.setEnabled(false); deleteBtn.setEnabled(false);

        right.add(refreshBtn); right.add(addBtn);
        right.add(viewBtn);
        if(Session.isAdmin()||Session.isBase1()){ right.add(resolveBtn); right.add(deleteBtn); }
        tb.add(right,BorderLayout.EAST); add(tb,BorderLayout.NORTH);

        // Badge live
        JPanel headRow=new JPanel(new BorderLayout()); headRow.setOpaque(false);
        JLabel liveLbl=new JLabel("  Tự động cập nhật mỗi 10 giây");
        liveLbl.setFont(new Font("Segoe UI",Font.ITALIC,11)); liveLbl.setForeground(AppConstants.SUCCESS);
        headRow.add(tb,BorderLayout.CENTER); headRow.add(liveLbl,BorderLayout.SOUTH);
        add(headRow,BorderLayout.NORTH);

        String[] cols={"ID","Tiêu đề","Phòng","Người báo","Ưu tiên","Trạng thái","Thời gian"};
        table=UIHelper.styledTable(cols);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);

        table.getSelectionModel().addListSelectionListener(e->{
            int row=table.getSelectedRow(); boolean sel=row>=0;
            viewBtn.setEnabled(sel);
            if(sel){
                String status=(String)table.getValueAt(row,5);
                resolveBtn.setEnabled(sel&&!"Hoàn thành".equals(status));
                deleteBtn.setEnabled(sel);
            } else { resolveBtn.setEnabled(false); deleteBtn.setEnabled(false); }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseClicked(java.awt.event.MouseEvent e){if(e.getClickCount()==2)viewSelected();}
        });
        add(UIHelper.tableScroll(table),BorderLayout.CENTER);

        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.LEFT)); bottom.setOpaque(false);
        bottom.add(UIHelper.labelMuted("Chọn hàng để sử dụng các nút thao tác phía trên"));
        add(bottom,BorderLayout.SOUTH);
    }

    private void loadData() {
        if (Session.isAdmin() || Session.isBase1()) {
            allData = dao.getAll();
        } else {
            allData = dao.getByReporter(Session.getUserId());
        }

        if (allData != null) {
            allData.sort(Comparator.comparingInt(Incident::getId));
        }

        renderTable(allData);
    }

    private void renderTable(List<Incident> list) {
        UIHelper.clearTable(table);
        for(Incident i:list)
            UIHelper.addRow(table,i.getId(),i.getTitle(),
                i.getRoomNumber()!=null?i.getRoomNumber():"",
                i.getReporterName()!=null?i.getReporterName():"",
                AppConstants.getStatusDisplay(i.getPriority()!=null?i.getPriority():"MEDIUM"),
                AppConstants.getStatusDisplay(i.getStatus()),
                UIHelper.formatDateTime(i.getCreatedAt()));
    }

    private void filter() {
        if(allData==null)return;
        String q=searchField.getText().toLowerCase();
        if(q.isBlank()||q.startsWith("tim")){renderTable(allData);return;}
        List<Incident> f=new java.util.ArrayList<>();
        for(Incident i:allData)
            if(i.getTitle().toLowerCase().contains(q)||
               (i.getRoomNumber()!=null&&i.getRoomNumber().toLowerCase().contains(q)))
                f.add(i);
        renderTable(f);
    }

    private void viewSelected() {
        int row=table.getSelectedRow(); if(row<0)return;
        int id=(int)table.getValueAt(row,0);
        Incident inc=allData.stream().filter(i->i.getId()==id).findFirst().orElse(null);
        if(inc==null)return; showUpdateDialog(inc);
    }

    /** Nút "Xác nhận đã sửa xong" */
    private void resolveSelected() {
        int row=table.getSelectedRow(); if(row<0)return;
        int id=(int)table.getValueAt(row,0);
        String notes=JOptionPane.showInputDialog(this,"Ghi chú kết quả sửa chữa:","");
        if(notes==null)return;
        if(dao.markResolved(id,Session.getUserId(),notes)){
            UIHelper.showSuccess(this,"Đã xác nhận hoàn thành sửa chữa!"); loadData();
        } else UIHelper.showError(this,"Thất bại!");
    }

    private void deleteSelected() {
        int row=table.getSelectedRow(); if(row<0)return;
        int id=(int)table.getValueAt(row,0);
        if(!UIHelper.confirm(this,"Xóa sự cố này?"))return;
        if(dao.delete(id)){loadData();}
        else UIHelper.showError(this,"Xóa thất bại!");
    }

    private void showCreateDialog() {
        JDialog dlg=new JDialog(SwingUtilities.getWindowAncestor(this),"Báo sự cố / Yêu cầu sửa chữa",Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(460,380); dlg.setLocationRelativeTo(this);
        JPanel main=new JPanel(new BorderLayout()); main.setBackground(AppConstants.BG_MAIN);
        JPanel form=new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(20,24,20,24));
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(7,6,7,6); g.fill=GridBagConstraints.HORIZONTAL;

        JTextField titleF=UIHelper.styledField();
        JTextArea descArea=UIHelper.styledTextArea(4,20);
        String[] priVN={"Thấp","Trung bình","Cao","Khẩn cấp"}; String[] priCode={"LOW","MEDIUM","HIGH","URGENT"};
        JComboBox<String> priCb=UIHelper.styledCombo(priVN); priCb.setSelectedIndex(1);
        List<Room> rooms=roomDAO.getAll();
        JComboBox<Object> roomCb=new JComboBox<>(); roomCb.addItem("-- Chọn phòng --"); rooms.forEach(roomCb::addItem); roomCb.setFont(AppConstants.FONT_BODY);

        UIHelper.addFormRow(form,g,0,"Tiêu đề: *",titleF);
        UIHelper.addFormRow(form,g,1,"Phòng:",roomCb);
        UIHelper.addFormRow(form,g,2,"Ưu tiên:",priCb);
        UIHelper.addFormRow(form,g,3,"Mô tả:",UIHelper.smoothScroll(new JScrollPane(descArea)));
        main.add(form,BorderLayout.CENTER);

        JPanel btnP=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,12)); btnP.setBackground(AppConstants.BG_MAIN);
        JButton cancelBtn=UIHelper.grayButton("Hủy"); cancelBtn.addActionListener(e->dlg.dispose());
        JButton sendBtn=UIHelper.warningButton("Gửi báo cáo");
        sendBtn.addActionListener(e->{
            if(titleF.getText().trim().isEmpty()){UIHelper.showWarning(dlg,"Nhập tiêu đề!"); return;}
            Incident inc=new Incident();
            inc.setTitle(titleF.getText().trim()); inc.setDescription(descArea.getText().trim());
            inc.setPriority(priCode[priCb.getSelectedIndex()]); inc.setReporterId(Session.getUserId());
            Object rs=roomCb.getSelectedItem(); inc.setRoomId(rs instanceof Room?((Room)rs).getId():0);
            if(dao.insert(inc)){UIHelper.showSuccess(dlg,"Đã gửi báo cáo!"); dlg.dispose(); loadData();}
            else UIHelper.showError(dlg,"Gửi thất bại!");
        });
        btnP.add(cancelBtn); btnP.add(sendBtn);
        main.add(btnP,BorderLayout.SOUTH);
        dlg.setContentPane(main); dlg.setVisible(true);
    }

    private void showUpdateDialog(Incident inc) {
        JDialog dlg=new JDialog(SwingUtilities.getWindowAncestor(this),"Cập nhật sự cố",Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480,420); dlg.setLocationRelativeTo(this);
        JPanel main=new JPanel(new BorderLayout()); main.setBackground(AppConstants.BG_MAIN);
        JPanel form=new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(20,24,20,24));
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(7,6,7,6); g.fill=GridBagConstraints.HORIZONTAL;

        JLabel titleLbl=UIHelper.labelBold(inc.getTitle());
        JTextArea descArea=UIHelper.styledTextArea(3,20); descArea.setText(inc.getDescription()!=null?inc.getDescription():""); descArea.setEditable(false); descArea.setBackground(new Color(248,250,252));
        String[] statVN={"Chờ duyệt","Đang xử lý","Hoàn thành","Từ chối"}; String[] statCode={"PENDING","IN_PROGRESS","RESOLVED","REJECTED"};
        JComboBox<String> statCb=UIHelper.styledCombo(statVN);
        for(int i=0;i<statCode.length;i++) if(statCode[i].equals(inc.getStatus())){statCb.setSelectedIndex(i);break;}
        JTextArea resArea=UIHelper.styledTextArea(4,20); resArea.setText(inc.getResolutionNotes()!=null?inc.getResolutionNotes():"");

        UIHelper.addFormRow(form,g,0,"Tiêu đề:",titleLbl);
        UIHelper.addFormRow(form,g,1,"Mô tả:",UIHelper.smoothScroll(new JScrollPane(descArea)));
        UIHelper.addFormRow(form,g,2,"Trạng thái:",statCb);
        UIHelper.addFormRow(form,g,3,"Ghi chú xử lý:",UIHelper.smoothScroll(new JScrollPane(resArea)));
        main.add(form,BorderLayout.CENTER);

        JPanel btnP=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,12)); btnP.setBackground(AppConstants.BG_MAIN);
        JButton closeBtn=UIHelper.grayButton("Đóng"); closeBtn.addActionListener(e->dlg.dispose()); btnP.add(closeBtn);

        if(Session.isAdmin()||Session.isBase1()){
            JButton resolveBtn=UIHelper.successButton("Xác nhận hoàn thành");
            resolveBtn.addActionListener(e->{
                if(dao.markResolved(inc.getId(),Session.getUserId(),resArea.getText().trim())){
                    UIHelper.showSuccess(dlg,"Đã xác nhận!"); dlg.dispose(); loadData();
                } else UIHelper.showError(dlg,"Thất bại!");
            });
            JButton saveBtn=UIHelper.primaryButton("Cập nhật trạng thái");
            saveBtn.addActionListener(e->{
                if(dao.updateStatus(inc.getId(),statCode[statCb.getSelectedIndex()],Session.getUserId(),resArea.getText().trim())){
                    UIHelper.showSuccess(dlg,"Đã cập nhật!"); dlg.dispose(); loadData();
                } else UIHelper.showError(dlg,"Thất bại!");
            });
            btnP.add(resolveBtn); btnP.add(saveBtn);
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
}
