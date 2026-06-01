package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.*;
import com.dormassist.model.*;
import com.dormassist.util.UIHelper;
import com.dormassist.view.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.*;

public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final RoomDAO roomDAO         = new RoomDAO();
    private final StudentDAO studentDAO   = new StudentDAO();
    private final BillDAO billDAO         = new BillDAO();
    private final IncidentDAO incDAO      = new IncidentDAO();
    private final NotificationDAO notifDAO= new NotificationDAO();

    // Các label stat cần cập nhật
    private JLabel roomTotal, roomAvail, roomFull, roomMaint, roomOccupants;
    private JTable incidentTable, noticeTable;

    // Đa luồng: cập nhật mỗi 15 giây
    private ScheduledExecutorService scheduler;

    public DashboardPanel(MainFrame mf) {
        this.mainFrame = mf;
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20,20,20,20));
        buildUI();
        startAutoRefresh();
    }

    // ===== ĐA LUỒNG: cập nhật sự cố và thông báo tự động =====
    private void startAutoRefresh() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "dashboard-refresh");
            t.setDaemon(true); return t;
        });
        scheduler.scheduleAtFixedRate(this::refreshLiveTables, 5, 15, TimeUnit.SECONDS);
    }

    /** Gọi khi panel bị ẩn để dừng thread */
    public void stopAutoRefresh() {
        if (scheduler != null && !scheduler.isShutdown()) scheduler.shutdown();
    }

    private void refreshLiveTables() {
        UIHelper.runAsync(() -> {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("incidents", incDAO.getLatest(6));
            data.put("notices", notifDAO.getLatest(5));
            data.put("available", roomDAO.countAvailable());
            data.put("full", roomDAO.countFull());
            data.put("maintenance", roomDAO.countMaintenance());
            data.put("total", roomDAO.countTotal());
            data.put("occupants", roomDAO.countTotalOccupants());
            return data;
        }, data -> {
            renderIncidentTable((java.util.List<Incident>) data.get("incidents"));
            renderNoticeTable((java.util.List<Notification>) data.get("notices"));
            roomAvail.setText(String.valueOf(data.get("available")));
            roomFull.setText(String.valueOf(data.get("full")));
            roomMaint.setText(String.valueOf(data.get("maintenance")));
            roomTotal.setText(String.valueOf(data.get("total")));
            roomOccupants.setText(String.valueOf(data.get("occupants")));
        });
    }

    private void buildUI() {
        JScrollPane sp = UIHelper.smoothScroll(new JScrollPane(buildContent()));
        sp.setBorder(null); sp.getVerticalScrollBar().setUnitIncrement(12);
        add(sp, BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(AppConstants.BG_MAIN);
        p.setBorder(new EmptyBorder(4,4,4,4));

        p.add(buildWelcome());      p.add(Box.createVerticalStrut(14));
        p.add(buildStatCards());    p.add(Box.createVerticalStrut(14));
        p.add(buildMiddleRow());    p.add(Box.createVerticalStrut(14));
        p.add(buildNoticeCard());   p.add(Box.createVerticalStrut(14));
        p.add(buildQuickActions()); p.add(Box.createVerticalStrut(10));
        return p;
    }

    // ===== BANNER CHÀO =====
    private JPanel buildWelcome() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp=new GradientPaint(0,0,AppConstants.PRIMARY_DARK,getWidth(),0,new Color(76,156,136));
                g2.setPaint(gp); g2.fill(new java.awt.geom.RoundRectangle2D.Float(0,0,getWidth(),getHeight(),16,16));
                g2.dispose();
            }
        };
        p.setLayout(new BorderLayout(16,0)); p.setOpaque(false);
        p.setBorder(new EmptyBorder(18,24,18,24));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,96));

        JPanel left = new JPanel(); left.setOpaque(false); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel hello = new JLabel("Xin chào, " + Session.getCurrentUser().getFullName() + " !");
        hello.setFont(new Font("Segoe UI",Font.BOLD,20)); hello.setForeground(Color.WHITE);
        JLabel roleTime = new JLabel(AppConstants.getRoleDisplay(Session.getRole()) + "  •  " +
            new java.text.SimpleDateFormat("EEEE, dd/MM/yyyy", new java.util.Locale("vi")).format(new java.util.Date()));
        roleTime.setFont(AppConstants.FONT_BODY); roleTime.setForeground(new Color(222,242,232));
        left.add(hello); left.add(Box.createVerticalStrut(4)); left.add(roleTime);
        p.add(left, BorderLayout.CENTER);

        // Icon KTX bên phải
        JLabel icon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,30)); g2.fillRoundRect(0,0,56,56,14,14);
                g2.setColor(new Color(255,255,255,180)); g2.setFont(new Font("Segoe UI",Font.BOLD,22));
                g2.drawString("KTX",8,36); g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(60,60));
        p.add(icon, BorderLayout.EAST);
        return p;
    }

    // ===== THẺ THỐNG KÊ =====
    private JPanel buildStatCards() {
        JPanel p = new JPanel(new GridLayout(1,4,16,0));
        p.setOpaque(false); p.setMaximumSize(new Dimension(Integer.MAX_VALUE,110));
        p.add(UIHelper.statCard("Tổng số phòng",   String.valueOf(roomDAO.countTotal()),    AppConstants.PRIMARY));
        p.add(UIHelper.statCard("Sinh viên",        String.valueOf(studentDAO.countTotal()), AppConstants.SUCCESS));
        p.add(UIHelper.statCard("Hóa đơn chưa thanh toán",  String.valueOf(billDAO.countUnpaid()),   AppConstants.WARNING));
        p.add(UIHelper.statCard("Sự cố chờ xử lý",  String.valueOf(incDAO.countPending()),   AppConstants.DANGER));
        return p;
    }

    // ===== HÀNG GIỮA =====
    private JPanel buildMiddleRow() {
        JPanel p = new JPanel(new GridLayout(1,2,16,0));
        p.setOpaque(false); p.setMaximumSize(new Dimension(Integer.MAX_VALUE,300));

        // Card tình trạng phòng
        JPanel roomCard = UIHelper.card();
        roomCard.setLayout(new BorderLayout(0,10));
        roomCard.add(UIHelper.sectionTitle("Tình trạng phòng"), BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(5,1,0,6)); stats.setOpaque(false);
        roomAvail    = makeStat("Còn trống",      roomDAO.countAvailable(), AppConstants.SUCCESS);
        roomFull     = makeStat("Đầy",            roomDAO.countFull(),      AppConstants.PRIMARY);
        roomMaint    = makeStat("Bảo trì",        roomDAO.countMaintenance(),AppConstants.WARNING);
        roomTotal    = makeStat("Tổng số phòng",  roomDAO.countTotal(),     AppConstants.TEXT_SEC);
        // Tổng số người đã chuyển vào (không phải "đầy người")
        roomOccupants= makeStat("Tổng người ở",   roomDAO.countTotalOccupants(), AppConstants.PURPLE);

        // rebuild để giữ tham chiếu
        stats.add(buildStatRow("Còn trống:",    roomAvail,   AppConstants.SUCCESS));
        stats.add(buildStatRow("Đầy:",          roomFull,    AppConstants.PRIMARY));
        stats.add(buildStatRow("Bảo trì:",      roomMaint,   AppConstants.WARNING));
        stats.add(buildStatRow("Tổng phòng:",   roomTotal,   AppConstants.TEXT_SEC));
        stats.add(buildStatRow("Tổng người ở:", roomOccupants,AppConstants.PURPLE));
        roomCard.add(stats, BorderLayout.CENTER);
        p.add(roomCard);

        // Card sự cố - LIVE UPDATE
        JPanel incCard = UIHelper.card();
        incCard.setLayout(new BorderLayout(0,10));
        JPanel incHead = new JPanel(new BorderLayout());
        incHead.setOpaque(false);
        incHead.add(UIHelper.sectionTitle("Sự cố gần đây"), BorderLayout.WEST);
        JLabel liveLbl = new JLabel("  LIVE");
        liveLbl.setFont(new Font("Segoe UI",Font.BOLD,10)); liveLbl.setForeground(AppConstants.DANGER);
        incHead.add(liveLbl, BorderLayout.EAST);
        incCard.add(incHead, BorderLayout.NORTH);

        incidentTable = UIHelper.styledTable(new String[]{"Tiêu đề","Ưu tiên","Trạng thái"});
        incidentTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        loadIncidentTable();
        incCard.add(UIHelper.tableScroll(incidentTable), BorderLayout.CENTER);
        p.add(incCard);
        return p;
    }

    private JLabel makeStat(String label, int val, Color color) {
        JLabel l = new JLabel(String.valueOf(val), SwingConstants.RIGHT);
        l.setFont(AppConstants.FONT_BOLD); l.setForeground(color); return l;
    }

    private JPanel buildStatRow(String label, JLabel valLbl, Color color) {
        JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false);
        JLabel l = UIHelper.label(label); row.add(l, BorderLayout.WEST);
        row.add(valLbl, BorderLayout.EAST); return row;
    }

    // ===== THÔNG BÁO LIVE =====
    private JPanel buildNoticeCard() {
        JPanel c = UIHelper.card(); c.setLayout(new BorderLayout(0,10));
        JPanel head = new JPanel(new BorderLayout()); head.setOpaque(false);
        head.add(UIHelper.sectionTitle("Thông báo mới nhất"), BorderLayout.WEST);
        JLabel live = new JLabel("  LIVE"); live.setFont(new Font("Segoe UI",Font.BOLD,10)); live.setForeground(AppConstants.INFO);
        head.add(live, BorderLayout.EAST);
        c.add(head, BorderLayout.NORTH);
        noticeTable = UIHelper.styledTable(new String[]{"Tiêu đề", "Nội dung tóm tắt", "Người đăng", "Thời gian"});
        noticeTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        noticeTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        noticeTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        noticeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) showNoticeDetail();
            }
        });
        loadNoticeTable();
        c.add(UIHelper.tableScroll(noticeTable), BorderLayout.CENTER);
        return c;
    }

    // ===== THAO TÁC NHANH =====
    private JPanel buildQuickActions() {
        JPanel p = UIHelper.card();
        p.setLayout(new BorderLayout(0,8));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,86));
        p.add(UIHelper.sectionTitle("Thao tác nhanh"), BorderLayout.NORTH);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);

        if (Session.canViewAllRooms()) {
            JButton b1 = UIHelper.primaryButton("+ Thêm sinh viên");
            b1.addActionListener(e -> mainFrame.navigateTo("students")); btns.add(b1);
        }
        if (Session.canManageFinance()) {
            JButton b2 = UIHelper.successButton("+ Tạo hóa đơn");
            b2.addActionListener(e -> mainFrame.navigateTo("bills")); btns.add(b2);
        }
        JButton b3 = UIHelper.warningButton("+ Báo sự cố");
        b3.addActionListener(e -> mainFrame.navigateTo("incidents")); btns.add(b3);

        // "Đăng ký khách" -> đổi thành "Đăng ký đổi phòng"
        JButton b4 = UIHelper.grayButton("+ Đăng ký đổi phòng");
        b4.addActionListener(e -> mainFrame.navigateTo("transfer")); btns.add(b4);

        JButton b5 = UIHelper.infoButton("Làm mới");
        b5.addActionListener(e -> refreshLiveTables()); btns.add(b5);

        p.add(btns, BorderLayout.CENTER);
        return p;
    }

    // ===== TẢI DỮ LIỆU LIVE =====
    private void loadIncidentTable() { renderIncidentTable(incDAO.getLatest(6)); }

    private void renderIncidentTable(List<Incident> list) {
        if (incidentTable == null) return;
        UIHelper.clearTable(incidentTable);
        for (Incident i : list)
            UIHelper.addRow(incidentTable,
                i.getTitle(),
                AppConstants.getStatusDisplay(i.getPriority()!=null?i.getPriority():"MEDIUM"),
                AppConstants.getStatusDisplay(i.getStatus()));
    }

    private void loadNoticeTable() { renderNoticeTable(notifDAO.getLatest(5)); }

    private void renderNoticeTable(List<Notification> list) {
        if (noticeTable == null) return;
        UIHelper.clearTable(noticeTable);
        if (list.isEmpty()) {
            UIHelper.addRow(noticeTable, "Chưa có thông báo nào", "", "", "");
            return;
        }
        for (Notification n : list) {
            String content = n.getContent() != null ? n.getContent() : "";
            String snippet = content.length() > 80 ? content.substring(0, 80) + "..." : content;
            UIHelper.addRow(noticeTable, n.getTitle(), snippet,
                n.getSenderName()!=null?n.getSenderName():"",
                UIHelper.formatDateTime(n.getCreatedAt()));
        }
    }

    private void showNoticeDetail() {
        int row = noticeTable.getSelectedRow();
        if (row < 0) return;
        String title = String.valueOf(noticeTable.getValueAt(row, 0));
        if ("Chưa có thông báo nào".equals(title)) return;
        for (Notification n : notifDAO.getLatest(20)) {
            if (title.equals(n.getTitle())) {
                JTextArea area = UIHelper.styledTextArea(10, 42);
                area.setEditable(false);
                area.setText((n.getContent() != null ? n.getContent() : "") +
                    "\n\nNgười đăng: " + (n.getSenderName()!=null?n.getSenderName():"") +
                    "\nThời gian: " + UIHelper.formatDateTime(n.getCreatedAt()));
                JOptionPane.showMessageDialog(this, UIHelper.smoothScroll(new JScrollPane(area)), n.getTitle(), JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
    }

    private void updateRoomStats() {
        roomAvail.setText(String.valueOf(roomDAO.countAvailable()));
        roomFull.setText(String.valueOf(roomDAO.countFull()));
        roomMaint.setText(String.valueOf(roomDAO.countMaintenance()));
        roomTotal.setText(String.valueOf(roomDAO.countTotal()));
        roomOccupants.setText(String.valueOf(roomDAO.countTotalOccupants()));
    }
}
