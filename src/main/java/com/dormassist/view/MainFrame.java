package com.dormassist.view;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.util.LogoHelper;
import com.dormassist.util.UIHelper;
import com.dormassist.view.panel.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private JPanel contentArea;
    private CardLayout cardLayout;
    private JLabel pageTitleLabel;
    private JLabel pageSubLabel;
    private final Map<String, JButton> navBtns = new LinkedHashMap<>();
    private String activePanel = "";

    public MainFrame() {
        setTitle(AppConstants.APP_NAME + " - Quản lý Ký túc xá");
        Image icon = LogoHelper.appImage(64,64);
        if (icon != null) setIconImage(icon);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 720));
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppConstants.BG_MAIN);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMain(), BorderLayout.CENTER);
        setContentPane(root);
        UIHelper.applySmoothScrolling(root);
    }

    private JPanel buildSidebar() {
        JPanel sb = new JPanel(new BorderLayout());
        sb.setPreferredSize(new Dimension(AppConstants.SIDEBAR_W, 0));
        sb.setBackground(AppConstants.SIDEBAR_BG);
        sb.setBorder(BorderFactory.createMatteBorder(0,0,0,1, AppConstants.BORDER));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(18,18,12,18));
        JLabel logo = new JLabel();
        ImageIcon logoIcon = LogoHelper.logoIcon(168, 58);
        if (logoIcon != null) logo.setIcon(logoIcon);
        else logo.setText("NestHome");
        top.add(logo, BorderLayout.CENTER);
        sb.add(top, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setBackground(AppConstants.SIDEBAR_BG);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(2,12,8,12));

        addItem(nav,"dashboard",  "Tổng quan",                true);
        addItem(nav,"rooms",      Session.isBase3() ? "Phòng ở" : "Quản lý phòng", Session.canViewAllRooms());
        addItem(nav,"students",   "Sinh viên",                Session.canViewAllRooms());
        addItem(nav,"bills",      "Hóa đơn & Thanh toán",     Session.canManageFinance());
        addItem(nav,"incidents",  "Sự cố & Bảo trì",          true);
        addItem(nav,"assets",     "Tài sản",                  true);
        addItem(nav,"notices",    "Thông báo",                true);
        addItem(nav,"discipline", "Điểm rèn luyện",           true);
        addItem(nav,"transfer",   "Chuyển phòng",             true);
        if (Session.isAdmin()) {
            addItem(nav,"tokens", "Mã Token",                 true);
            addItem(nav,"users",  "Tài khoản / Phân quyền",   Session.isAdminSuper());
            addItem(nav,"prices", "Cấu hình giá",             true);
        }
        addItem(nav,"profile",    "Cá nhân / Cài đặt",        true);

        JScrollPane navSp = UIHelper.smoothScroll(new JScrollPane(nav));
        navSp.setBorder(null);
        navSp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navSp.setBackground(AppConstants.SIDEBAR_BG);
        navSp.getViewport().setBackground(AppConstants.SIDEBAR_BG);
        sb.add(navSp, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(8,14,14,14));
        bottom.add(buildDormIllustration());
        bottom.add(Box.createVerticalStrut(8));
        JLabel ver = new JLabel("v" + AppConstants.APP_VERSION, SwingConstants.CENTER);
        ver.setAlignmentX(Component.CENTER_ALIGNMENT); ver.setFont(AppConstants.FONT_SMALL); ver.setForeground(AppConstants.TEXT_MUTED);
        JLabel copy = new JLabel("© 2026 NestHome", SwingConstants.CENTER);
        copy.setAlignmentX(Component.CENTER_ALIGNMENT); copy.setFont(new Font("Segoe UI", Font.PLAIN, 10)); copy.setForeground(AppConstants.TEXT_MUTED);
        bottom.add(ver); bottom.add(Box.createVerticalStrut(4)); bottom.add(copy);
        sb.add(bottom, BorderLayout.SOUTH);
        return sb;
    }

    private JComponent buildDormIllustration() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(), h=getHeight();
                g2.setColor(new Color(229, 242, 229));
                g2.fillRoundRect(0,0,w,h,18,18);
                g2.setColor(new Color(209, 226, 209));
                for (int i=0;i<5;i++) g2.fillOval(-20+i*48,h-32-(i%2)*8,70,45);
                g2.setColor(new Color(161, 191, 166));
                g2.fillRoundRect(w/2-46, h-78, 92, 58, 6, 6);
                g2.setColor(new Color(246,250,245));
                for(int r=0;r<2;r++) for(int c=0;c<4;c++) g2.fillRoundRect(w/2-34+c*18,h-68+r*18,10,10,3,3);
                g2.setColor(AppConstants.PRIMARY);
                g2.drawLine(w/2-55,h-78,w/2,h-108); g2.drawLine(w/2,h-108,w/2+55,h-78);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(AppConstants.SIDEBAR_W-34, 126));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 126));
        return p;
    }

    private void addItem(JPanel nav, String key, String label, boolean visible) {
        if (!visible) return;
        JButton btn = new JButton(label, UIHelper.menuIcon(key, 18)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if (key.equals(activePanel)) {
                    g2.setColor(AppConstants.SIDEBAR_ACTIVE);
                    g2.fill(new RoundRectangle2D.Float(0,2,getWidth(),getHeight()-4,14,14));
                } else if (getModel().isRollover()) {
                    g2.setColor(AppConstants.SIDEBAR_HOVER);
                    g2.fill(new RoundRectangle2D.Float(0,2,getWidth(),getHeight()-4,14,14));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppConstants.FONT_SIDEBAR);
        btn.setForeground(AppConstants.SIDEBAR_TEXT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(12);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(AppConstants.SIDEBAR_W - 24, 40));
        btn.setPreferredSize(new Dimension(AppConstants.SIDEBAR_W - 24, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(new EmptyBorder(0,14,0,8));
        btn.addActionListener(e -> navigateTo(key));
        navBtns.put(key, btn);
        nav.add(btn);
        nav.add(Box.createVerticalStrut(3));
    }

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(AppConstants.BG_MAIN);
        main.add(buildHeader(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(AppConstants.BG_MAIN);
        contentArea.add(new DashboardPanel(this), "dashboard");
        contentArea.add(new ProfilePanel(),       "profile");
        contentArea.add(new RoomPanel(),          "rooms");
        contentArea.add(new StudentPanel(),       "students");
        contentArea.add(new BillPanel(),          "bills");
        contentArea.add(new AssetPanel(),         "assets");
        contentArea.add(new IncidentPanel(),      "incidents");
        contentArea.add(new TransferPanel(),      "transfer");
        contentArea.add(new DisciplinePanel(),    "discipline");
        contentArea.add(new NoticePanel(),        "notices");
        if (Session.isAdminSuper()) contentArea.add(new UserPanel(), "users");
        if (Session.isAdmin()) {
            contentArea.add(new TokenPanel(), "tokens");
            contentArea.add(new PricePanel(), "prices");
        }
        main.add(contentArea, BorderLayout.CENTER);
        navigateTo("dashboard");
        return main;
    }

    private JPanel buildHeader() {
        JPanel topBar = new JPanel(new BorderLayout(12,0));
        topBar.setBackground(AppConstants.BG_CARD);
        topBar.setPreferredSize(new Dimension(0, AppConstants.HEADER_H));
        topBar.setBorder(BorderFactory.createMatteBorder(0,0,1,0, AppConstants.BORDER));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 11));
        left.setOpaque(false);
        JLabel menu = new JLabel(UIHelper.menuIcon("menu", 22));
        menu.setForeground(AppConstants.TEXT_SEC);
        left.add(menu);
        JPanel titleBlock = new JPanel(); titleBlock.setOpaque(false); titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        pageTitleLabel = new JLabel("Tổng quan");
        pageTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17)); pageTitleLabel.setForeground(AppConstants.TEXT_MAIN);
        pageSubLabel = new JLabel("Quản lý ký túc xá xanh, nhẹ nhàng và hiệu quả");
        pageSubLabel.setFont(AppConstants.FONT_SMALL); pageSubLabel.setForeground(AppConstants.TEXT_MUTED);
        titleBlock.add(pageTitleLabel); titleBlock.add(Box.createVerticalStrut(2)); titleBlock.add(pageSubLabel);
        left.add(titleBlock);
        topBar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        right.setOpaque(false);
        JLabel bell = new JLabel(UIHelper.menuIcon("notices", 20));
        bell.setForeground(AppConstants.PRIMARY);
        right.add(bell);
        right.add(buildMiniAvatar());
        JPanel info = new JPanel(); info.setOpaque(false); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(Session.getCurrentUser().getFullName());
        name.setFont(AppConstants.FONT_BOLD); name.setForeground(AppConstants.TEXT_MAIN);
        JLabel role = new JLabel(AppConstants.getRoleDisplay(Session.getRole()));
        role.setFont(AppConstants.FONT_SMALL); role.setForeground(AppConstants.TEXT_MUTED);
        info.add(name); info.add(role); right.add(info);
        JButton logout = UIHelper.outlineButton("Đăng xuất");
        logout.setPreferredSize(new Dimension(92, 32)); logout.addActionListener(e -> doLogout());
        right.add(logout);
        topBar.add(right, BorderLayout.EAST);
        return topBar;
    }

    private JComponent buildMiniAvatar() {
        String full = Session.getCurrentUser().getFullName();
        String initials = "N";
        if (full != null && !full.trim().isEmpty()) initials = full.trim().substring(0,1).toUpperCase();
        final String text = initials;
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(226, 239, 226)); g2.fillOval(0,0,getWidth()-1,getHeight()-1);
                g2.setColor(AppConstants.PRIMARY); g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (getWidth()-fm.stringWidth(text))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        p.setOpaque(false); p.setPreferredSize(new Dimension(34,34));
        return p;
    }

    public void navigateTo(String key) {
        if (!navBtns.containsKey(key) && !"dashboard".equals(key)) return;
        activePanel = key;
        cardLayout.show(contentArea, key);
        navBtns.values().forEach(b -> { b.setForeground(AppConstants.SIDEBAR_TEXT); b.repaint(); });
        JButton act = navBtns.get(key);
        if (act != null) { act.setForeground(AppConstants.SIDEBAR_TEXT_ACTIVE); act.repaint(); }
        String[] keys  = {"dashboard","rooms","students","bills","incidents","assets","notices","discipline","transfer","tokens","users","profile","prices"};
        String[] names = {"Tổng quan","Quản lý phòng","Quản lý sinh viên","Hóa đơn & Thanh toán", "Sự cố & Bảo trì","Tài sản","Thông báo","Điểm rèn luyện", "Chuyển phòng","Mã Token","Tài khoản / Phân quyền","Thông tin cá nhân","Cấu hình giá"};
        String[] descs = {"Tổng hợp nhanh tình hình ký túc xá","Quản lý phòng, tầng, loại phòng và trạng thái","Quản lý hồ sơ sinh viên nội trú","Theo dõi hóa đơn, hạn thanh toán và doanh thu", "Tiếp nhận và xử lý sự cố bảo trì","Quản lý tài sản theo phòng/khu vực","Đăng và theo dõi thông báo","Theo dõi điểm rèn luyện theo phòng", "Duyệt yêu cầu chuyển phòng","Cấp mã token cho người dùng đăng ký theo vai trò","Quản lý tài khoản và phân quyền","Quản lý và cập nhật thông tin tài khoản của bạn","Cấu hình đơn giá điện, nước và dịch vụ"};
        for (int i = 0; i < keys.length; i++) if (keys[i].equals(key)) { pageTitleLabel.setText(names[i]); pageSubLabel.setText(descs[i]); break; }
    }

    private void doLogout() {
        if (!UIHelper.confirm(this,"Bạn có chắc muốn đăng xuất?")) return;
        Session.clear();
        new LoginFrame().setVisible(true);
        dispose();
    }
}
