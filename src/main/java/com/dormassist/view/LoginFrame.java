package com.dormassist.view;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.UserDAO;
import com.dormassist.model.User;
import com.dormassist.util.UIHelper;
import com.dormassist.util.LogoHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberBox;
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle(AppConstants.APP_NAME + " - Đăng nhập");
        Image icon = LogoHelper.appImage(64,64);
        if (icon != null) setIconImage(icon);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1040, 650);
        setMinimumSize(new Dimension(920, 560));
        setLocationRelativeTo(null);
        setResizable(true);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(AppConstants.BG_MAIN);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.gridy = 0;
        g.weighty = 1;
        g.gridx = 0; g.weightx = 0.56;
        root.add(buildBrandPanel(), g);
        g.gridx = 1; g.weightx = 0.44;
        root.add(buildFormPanel(), g);
        setContentPane(root);
        UIHelper.applySmoothScrolling(root);
    }

    private JPanel buildBrandPanel() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0,new Color(238,247,239),getWidth(),getHeight(),new Color(212,232,220));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(255,255,255,120));
                g2.fillOval(-120, -90, 360, 360);
                g2.setColor(new Color(37,112,98,34));
                g2.fillOval(getWidth()-180, getHeight()-210, 300, 300);
                g2.dispose();
            }
        };
        p.setBorder(new EmptyBorder(52,56,44,56));

        JPanel center = new JPanel(); center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(Box.createVerticalGlue());

        JLabel logo = new JLabel();
        ImageIcon full = LogoHelper.fullLogoIcon(520, 230);
        if (full != null) logo.setIcon(full); else logo.setText(AppConstants.APP_NAME);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(logo);
        center.add(Box.createVerticalStrut(16));

        JLabel sub = new JLabel("Ký túc xá • Quản lý • Chăm sóc • Kết nối", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sub.setForeground(AppConstants.TEXT_SEC);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(sub);
        center.add(Box.createVerticalStrut(34));

        JPanel features = new JPanel(new GridLayout(2,2,14,14));
        features.setOpaque(false);
        features.setMaximumSize(new Dimension(560, 132));
        features.add(featureCard("Quản lý cư trú", "Sinh viên, phòng ở, chuyển phòng"));
        features.add(featureCard("Tài chính rõ ràng", "Hóa đơn điện, nước, phí dịch vụ"));
        features.add(featureCard("Chăm sóc nhanh", "Sự cố, bảo trì, thông báo"));
        features.add(featureCard("Kết nối an toàn", "Token, phân quyền, hồ sơ cá nhân"));
        center.add(features);
        center.add(Box.createVerticalGlue());
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel featureCard(String title, String desc) {
        JPanel c = UIHelper.card();
        c.setLayout(new BorderLayout(0,3));
        c.setBorder(new EmptyBorder(10,12,12,12));
        JLabel t = new JLabel(title); t.setFont(AppConstants.FONT_BOLD); t.setForeground(AppConstants.PRIMARY_DARK);
        JLabel d = new JLabel("<html>" + desc + "</html>"); d.setFont(AppConstants.FONT_SMALL); d.setForeground(AppConstants.TEXT_MUTED);
        c.add(t, BorderLayout.NORTH); c.add(d, BorderLayout.CENTER);
        return c;
    }

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(AppConstants.BG_MAIN);
        wrapper.setBorder(new EmptyBorder(38,40,38,48));

        JPanel form = UIHelper.card();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(34,36,34,36));

        JLabel wordmark = new JLabel();
        ImageIcon logo = LogoHelper.logoIcon(240, 68);
        if (logo != null) wordmark.setIcon(logo);
        wordmark.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(wordmark); form.add(Box.createVerticalStrut(18));

        JLabel title = new JLabel("Chào mừng bạn trở lại");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24)); title.setForeground(AppConstants.TEXT_MAIN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Đăng nhập để tiếp tục quản lý ký túc xá");
        sub.setFont(AppConstants.FONT_SMALL); sub.setForeground(AppConstants.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title); form.add(Box.createVerticalStrut(6)); form.add(sub);
        form.add(Box.createVerticalStrut(26));

        JLabel uLbl = UIHelper.labelBold("Tên đăng nhập"); uLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField = UIHelper.styledField();
        usernameField.setToolTipText("Nhập tên đăng nhập");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConstants.FIELD_H + 4));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(uLbl); form.add(Box.createVerticalStrut(7)); form.add(usernameField);
        form.add(Box.createVerticalStrut(16));

        JLabel pLbl = UIHelper.labelBold("Mật khẩu"); pLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField = UIHelper.styledPasswordField();
        passwordField.setToolTipText("Nhập mật khẩu");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, AppConstants.FIELD_H + 4));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(pLbl); form.add(Box.createVerticalStrut(7)); form.add(passwordField);
        form.add(Box.createVerticalStrut(10));

        JPanel options = new JPanel(new BorderLayout()); options.setOpaque(false); options.setAlignmentX(Component.LEFT_ALIGNMENT);
        rememberBox = new JCheckBox("Ghi nhớ đăng nhập");
        rememberBox.setOpaque(false); rememberBox.setFont(AppConstants.FONT_SMALL); rememberBox.setForeground(AppConstants.TEXT_SEC);
        JButton forgotLink = new JButton("Quên mật khẩu?");
        forgotLink.setFont(AppConstants.FONT_SMALL); forgotLink.setForeground(AppConstants.PRIMARY_DARK);
        forgotLink.setContentAreaFilled(false); forgotLink.setBorderPainted(false); forgotLink.setFocusPainted(false);
        forgotLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLink.addActionListener(e -> new ForgotPasswordDialog(this).setVisible(true));
        options.add(rememberBox, BorderLayout.WEST); options.add(forgotLink, BorderLayout.EAST);
        form.add(options); form.add(Box.createVerticalStrut(20));

        JButton loginBtn = buildLoginButton();
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(loginBtn); form.add(Box.createVerticalStrut(14));

        JButton regBtn = UIHelper.outlineButton("Đăng ký tài khoản mới");
        regBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        regBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        regBtn.addActionListener(e -> new RegisterFrame(this).setVisible(true));
        form.add(regBtn);

        KeyAdapter ka = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if(e.getKeyCode()==KeyEvent.VK_ENTER) doLogin(); }
        };
        usernameField.addKeyListener(ka);
        passwordField.addKeyListener(ka);

        wrapper.add(form, new GridBagConstraints());
        return wrapper;
    }

    private JButton buildLoginButton() {
        JButton btn = new JButton("Đăng nhập") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg=getModel().isPressed()?AppConstants.PRIMARY_DARK:(getModel().isRollover()?AppConstants.SUCCESS:AppConstants.PRIMARY);
                g2.setColor(bg); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),16,16));
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE); btn.setFont(AppConstants.FONT_BOLD);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> doLogin());
        return btn;
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            UIHelper.showWarning(this, "Vui lòng nhập tên đăng nhập và mật khẩu!"); return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        UIHelper.runAsync(() -> {
            try { return userDAO.login(user, pass); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        }, u -> {
            setCursor(Cursor.getDefaultCursor());
            if (u == null) { UIHelper.showError(this, "Sai tên đăng nhập hoặc mật khẩu!"); passwordField.setText(""); return; }
            Session.setCurrentUser(u);
            userDAO.log(u.getId(), "LOGIN", "AUTH", "Đăng nhập thành công");
            new MainFrame().setVisible(true);
            dispose();
        });
    }
}
