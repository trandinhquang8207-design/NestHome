package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.StudentDAO;
import com.dormassist.dao.UserDAO;
import com.dormassist.model.Student;
import com.dormassist.model.User;
import com.dormassist.view.ui.Toast;
import com.dormassist.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class ProfilePanel extends JPanel {
    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final User user = Session.getCurrentUser();
    private Student student;

    private JTextField fullNameField, emailField, phoneField, addressField;
    private JTextArea noteArea;
    private JPasswordField oldPassField, newPassField, confirmPassField;
    private JLabel tokenValue;

    public ProfilePanel() {
        student = studentDAO.getByUserId(user.getId());
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(18, 22, 22, 22));
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppConstants.BG_MAIN);

        content.add(pageTitle());
        content.add(Box.createVerticalStrut(14));

        JPanel top = new JPanel(new GridLayout(1, 2, 16, 0));
        top.setOpaque(false);
        top.add(profileInfoCard());
        top.add(accountOverviewCard());
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE, 228));
        content.add(top);
        content.add(Box.createVerticalStrut(16));

        JPanel bottom = new JPanel(new GridLayout(1, 2, 16, 0));
        bottom.setOpaque(false);
        bottom.add(updateInfoCard());
        bottom.add(passwordCard());
        bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 282));
        content.add(bottom);
        content.add(Box.createVerticalGlue());

        JScrollPane sp = UIHelper.smoothScroll(new JScrollPane(content));
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.getViewport().setBackground(AppConstants.BG_MAIN);
        add(sp, BorderLayout.CENTER);
    }

    private JComponent pageTitle() {
        JPanel p = new JPanel(new BorderLayout(12,0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        JLabel icon = new JLabel(UIHelper.menuIcon("profile", 26));
        icon.setForeground(AppConstants.PRIMARY);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setBackground(new Color(232, 242, 235));
        icon.setPreferredSize(new Dimension(44,44));
        JPanel titleBlock = new JPanel(); titleBlock.setOpaque(false); titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Thông tin cá nhân");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22)); title.setForeground(AppConstants.TEXT_MAIN);
        JLabel sub = new JLabel("Quản lý và cập nhật thông tin tài khoản của bạn");
        sub.setFont(AppConstants.FONT_BODY); sub.setForeground(AppConstants.TEXT_MUTED);
        titleBlock.add(title); titleBlock.add(Box.createVerticalStrut(2)); titleBlock.add(sub);
        p.add(icon, BorderLayout.WEST); p.add(titleBlock, BorderLayout.CENTER);
        return p;
    }

    private JPanel profileInfoCard() {
        JPanel card = UIHelper.card();
        card.setLayout(new BorderLayout(18, 8));
        card.add(UIHelper.sectionTitle("Thông tin hồ sơ"), BorderLayout.NORTH);

        JPanel avatarWrap = new JPanel(null);
        avatarWrap.setOpaque(false);
        avatarWrap.setPreferredSize(new Dimension(170, 140));
        AvatarPanel avatar = new AvatarPanel(displayName(), userDAO.getAvatar(user.getId()));
        avatar.setBounds(34, 18, 104, 104);
        avatarWrap.add(avatar);

        JButton camera = UIHelper.outlineButton("Máy ảnh");
        camera.setText("Cập nhật");
        camera.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        camera.setBounds(62, 113, 86, 28);
        camera.addActionListener(e -> chooseAvatarImage());
        avatarWrap.add(camera);
        card.add(avatarWrap, BorderLayout.WEST);

        JPanel info = new JPanel(new GridBagLayout());
        info.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 4, 5, 4); g.fill = GridBagConstraints.HORIZONTAL;
        addInfoRow(info, g, 0, "Họ và tên:", displayName());
        addInfoRow(info, g, 1, "Vai trò:", AppConstants.getRoleDisplay(user.getRole()));
        addInfoRow(info, g, 2, "Email:", value(user.getEmail()));
        addInfoRow(info, g, 3, "Số điện thoại:", value(user.getPhone()));
        addInfoRow(info, g, 4, "Phòng ở:", student != null && student.getRoomNumber()!=null ? student.getRoomNumber() : "Chưa xếp phòng");
        addInfoRow(info, g, 5, "Mã sinh viên:", student != null && student.getStudentCode()!=null ? student.getStudentCode() : "Chưa cập nhật");
        g.gridx=0; g.gridy=6; g.weightx=.35; info.add(iconLabel("Trạng thái:"), g);
        g.gridx=1; g.weightx=.65; info.add(UIHelper.badge(user.isActive()?"Đang hoạt động":"Tạm khóa", user.isActive()?AppConstants.SUCCESS:AppConstants.DANGER), g);
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private JPanel accountOverviewCard() {
        JPanel card = UIHelper.card();
        card.setLayout(new BorderLayout(0, 8));
        card.add(UIHelper.sectionTitle("Tổng quan tài khoản"), BorderLayout.NORTH);
        JPanel rows = new JPanel(new GridBagLayout()); rows.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(10,4,10,4); g.fill = GridBagConstraints.HORIZONTAL;
        addOverviewRow(rows, g, 0, "Vai trò", UIHelper.badge(AppConstants.getRoleDisplay(user.getRole()), AppConstants.PRIMARY));
        addOverviewRow(rows, g, 1, "Ngày tham gia", UIHelper.label(value(UIHelper.formatDate(user.getCreatedAt()))));
        addOverviewRow(rows, g, 2, "Trạng thái tài khoản", UIHelper.badge(user.isActive()?"Đang hoạt động":"Tạm khóa", user.isActive()?AppConstants.SUCCESS:AppConstants.DANGER));
        addOverviewRow(rows, g, 3, "Lần đăng nhập gần nhất", UIHelper.label(value(UIHelper.formatDateTime(user.getLastLogin()))));
        tokenValue = UIHelper.labelMuted("Mã token: " + (user.getTokenUsed()==null || user.getTokenUsed().trim().isEmpty() ? "Không dùng token" : "••••••••••••"));
        JButton reveal = UIHelper.outlineButton("Xem token"); reveal.addActionListener(e -> revealToken());
        JPanel tokenRow = new JPanel(new BorderLayout(8,0)); tokenRow.setOpaque(false); tokenRow.add(tokenValue, BorderLayout.CENTER); tokenRow.add(reveal, BorderLayout.EAST);
        g.gridx=0; g.gridy=4; g.weightx=1; g.gridwidth=2; rows.add(tokenRow, g);
        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel updateInfoCard() {
        JPanel card = UIHelper.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UIHelper.sectionTitle("Cập nhật thông tin"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 4, 6, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        fullNameField = UIHelper.styledField();
        fullNameField.setText(editValue(user.getFullName() != null ? user.getFullName() : user.getUsername()));

        emailField = UIHelper.styledField();
        emailField.setText(editValue(user.getEmail()));

        phoneField = UIHelper.styledField();
        phoneField.setText(editValue(user.getPhone()));

        addressField = UIHelper.styledField();
        addressField.setText(student != null ? editValue(student.getHometown()) : "");

        noteArea = UIHelper.styledTextArea(3, 20);
        noteArea.setText(student != null ? editValue(student.getNotes()) : "");

        // Nếu tài khoản không phải sinh viên / chưa có hồ sơ sinh viên,
        // địa chỉ và ghi chú không có nơi lưu trong bảng students nên tạm khóa để tránh hiểu nhầm.
        boolean hasStudentProfile = student != null;
        addressField.setEnabled(hasStudentProfile);
        noteArea.setEnabled(hasStudentProfile);

        if (!hasStudentProfile) {
            addressField.setToolTipText("Chỉ áp dụng cho tài khoản sinh viên có hồ sơ trong bảng students.");
            noteArea.setToolTipText("Chỉ áp dụng cho tài khoản sinh viên có hồ sơ trong bảng students.");
        }

        addFormLine(form, g, 0, "Họ và tên *", fullNameField, "Email *", emailField);
        addFormLine(form, g, 1, "Số điện thoại *", phoneField, "Địa chỉ", addressField);

        // Không đặt gridy = 2 nữa vì dòng Số điện thoại / Địa chỉ đã dùng gridy 2 và 3.
        g.gridx = 0;
        g.gridy = 4;
        g.gridwidth = 2;
        g.weightx = 1;
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        form.add(UIHelper.labelBold("Ghi chú"), g);

        g.gridx = 0;
        g.gridy = 5;
        g.gridwidth = 2;
        g.weightx = 1;
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;

        JScrollPane noteSp = UIHelper.smoothScroll(new JScrollPane(noteArea));
        noteSp.setPreferredSize(new Dimension(200, 74));
        form.add(noteSp, g);

        card.add(form, BorderLayout.CENTER);

        JButton save = UIHelper.primaryButton("Lưu thay đổi");
        save.addActionListener(e -> saveInfo());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(save);

        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JPanel passwordCard() {
        JPanel card = UIHelper.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UIHelper.sectionTitle("Đổi mật khẩu"), BorderLayout.NORTH);
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(6,4,6,4); g.fill = GridBagConstraints.HORIZONTAL;
        oldPassField = UIHelper.styledPasswordField(); newPassField = UIHelper.styledPasswordField(); confirmPassField = UIHelper.styledPasswordField();
        addPasswordLine(form, g, 0, "Mật khẩu cũ *", oldPassField);
        addPasswordLine(form, g, 1, "Mật khẩu mới *", newPassField);
        addPasswordLine(form, g, 2, "Xác nhận mật khẩu mới *", confirmPassField);
        card.add(form, BorderLayout.CENTER);
        JButton save = UIHelper.primaryButton("Cập nhật mật khẩu"); save.addActionListener(e -> changePassword());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); actions.setOpaque(false); actions.add(save);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private void addInfoRow(JPanel p, GridBagConstraints g, int row, String label, String value) {
        g.gridx=0; g.gridy=row; g.weightx=.32; p.add(iconLabel(label), g);
        g.gridx=1; g.weightx=.68; JLabel v=UIHelper.labelBold(value(value)); p.add(v, g);
    }

    private JLabel iconLabel(String text) {
        JLabel l = UIHelper.label(text); l.setForeground(AppConstants.TEXT_SEC); return l;
    }

    private void addOverviewRow(JPanel p, GridBagConstraints g, int row, String label, JComponent value) {
        g.gridx=0; g.gridy=row; g.weightx=.55; p.add(iconLabel(label), g);
        g.gridx=1; g.weightx=.45; p.add(value, g);
    }

    private void addFormLine(JPanel p, GridBagConstraints g, int row,
                             String l1, JComponent c1,
                             String l2, JComponent c2) {

        int labelY = row * 2;
        int fieldY = row * 2 + 1;

        g.gridwidth = 1;
        g.weightx = 0.5;
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.gridy = labelY;
        p.add(UIHelper.labelBold(l1), g);

        g.gridx = 1;
        g.gridy = labelY;
        p.add(UIHelper.labelBold(l2), g);

        g.gridx = 0;
        g.gridy = fieldY;
        p.add(c1, g);

        g.gridx = 1;
        g.gridy = fieldY;
        p.add(c2, g);
    }

    private void addPasswordLine(JPanel p, GridBagConstraints g, int row, String label, JPasswordField field) {
        g.gridx=0; g.gridy=row*2; g.weightx=1; p.add(UIHelper.labelBold(label), g);
        JPanel wrap = new JPanel(new BorderLayout(4,0)); wrap.setOpaque(false); wrap.add(field, BorderLayout.CENTER);
        JButton eye = UIHelper.outlineButton("Hiện"); eye.setPreferredSize(new Dimension(64, AppConstants.FIELD_H));
        eye.addActionListener(e -> { char echo = field.getEchoChar(); field.setEchoChar(echo == 0 ? '•' : (char)0); eye.setText(echo == 0 ? "Hiện" : "Ẩn"); });
        wrap.add(eye, BorderLayout.EAST);
        g.gridx=0; g.gridy=row*2+1; p.add(wrap, g);
    }

    private void saveInfo() {
        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty()) {
            UIHelper.showWarning(this, "Vui lòng nhập họ và tên.");
            fullNameField.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            UIHelper.showWarning(this, "Vui lòng nhập email.");
            emailField.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            UIHelper.showWarning(this, "Email không hợp lệ.");
            emailField.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            UIHelper.showWarning(this, "Vui lòng nhập số điện thoại.");
            phoneField.requestFocus();
            return;
        }

        // Cập nhật bảng users
        user.setFullName(name);
        user.setEmail(email);
        user.setPhone(phone);

        boolean okUser = userDAO.update(user);

        // Nếu tài khoản có hồ sơ sinh viên thì cập nhật thêm bảng students
        boolean okStudent = true;

        if (student != null) {
            student.setFullName(name);
            student.setEmail(email);
            student.setPhone(phone);
            student.setHometown(cleanOptional(addressField.getText()));
            student.setNotes(cleanOptional(noteArea.getText()));

            okStudent = studentDAO.update(student);
        }

        if (okUser && okStudent) {
            student = studentDAO.getByUserId(user.getId());
            Toast.show(this, "Đã lưu thay đổi.");

            refreshProfilePanel();
        } else {
            UIHelper.showError(this, "Không thể lưu thay đổi.");
        }
    }

    private void changePassword() {
        String oldP = new String(oldPassField.getPassword());
        String p1 = new String(newPassField.getPassword());
        String p2 = new String(confirmPassField.getPassword());
        if(oldP.isEmpty() || p1.isEmpty() || p2.isEmpty()) { UIHelper.showWarning(this, "Vui lòng nhập đầy đủ thông tin mật khẩu."); return; }
        if(p1.length() < 6) { UIHelper.showWarning(this, "Mật khẩu mới phải có ít nhất 6 ký tự."); return; }
        if(!p1.equals(p2)) { UIHelper.showError(this, "Xác nhận mật khẩu mới không khớp."); return; }
        if(userDAO.changePasswordWithOldPassword(user.getId(), oldP, p1)) {
            oldPassField.setText(""); newPassField.setText(""); confirmPassField.setText("");
            Toast.show(this, "Đã cập nhật mật khẩu.");
        } else UIHelper.showError(this, "Mật khẩu cũ không đúng.");
    }

    private void revealToken() {
        JPasswordField pf = UIHelper.styledPasswordField();
        int ok = JOptionPane.showConfirmDialog(this, pf, "Nhập mật khẩu để xem mã token", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(ok != JOptionPane.OK_OPTION) return;
        if(!userDAO.verifyPasswordById(user.getId(), new String(pf.getPassword()))) { UIHelper.showError(this, "Mật khẩu không đúng."); return; }
        String token = user.getTokenUsed();
        tokenValue.setText("Mã token: " + (token==null || token.trim().isEmpty() ? "Tài khoản này không dùng token" : token));
    }

    private String displayName() {return user.getFullName() != null && !user.getFullName().trim().isEmpty() ? user.getFullName() : user.getUsername();}
    private void refreshProfilePanel() {
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private String editValue(String s) {
        return s == null ? "" : s.trim();
    }

    private String cleanOptional(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private String value(String s) { return s == null || s.trim().isEmpty() ? "Chưa cập nhật" : s; }

    private void chooseAvatarImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh đại diện");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Ảnh (*.jpg, *.jpeg, *.png)",
                "jpg", "jpeg", "png"
        ));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        try {
            BufferedImage original = ImageIO.read(file);

            if (original == null) {
                UIHelper.showError(this, "File bạn chọn không phải là ảnh hợp lệ.");
                return;
            }

            byte[] avatarBytes = buildAvatarBytes(original);

            if (userDAO.updateAvatar(user.getId(), avatarBytes)) {
                Toast.show(this, "Đã cập nhật ảnh đại diện.");
                refreshProfilePanel();
            } else {
                UIHelper.showError(this, "Không thể lưu ảnh đại diện vào database.");
            }

        } catch (Exception ex) {
            UIHelper.showError(this, "Cập nhật ảnh thất bại:\n" + ex.getMessage());
        }
    }

    private byte[] buildAvatarBytes(BufferedImage original) throws Exception {
        BufferedImage square = cropToSquare(original);
        BufferedImage resized = resizeImage(square, 256, 256);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(resized, "png", out);

        return out.toByteArray();
    }

    private BufferedImage cropToSquare(BufferedImage src) {
        int size = Math.min(src.getWidth(), src.getHeight());
        int x = (src.getWidth() - size) / 2;
        int y = (src.getHeight() - size) / 2;

        return src.getSubimage(x, y, size, size);
    }

    private BufferedImage resizeImage(BufferedImage src, int width, int height) {
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawImage(src, 0, 0, width, height, null);
        g2.dispose();

        return out;
    }

    private static class AvatarPanel extends JPanel {
        private final String name;
        private final byte[] avatarBytes;
        private Image avatarImage;

        AvatarPanel(String name, byte[] avatarBytes) {
            this.name = name == null ? "N" : name;
            this.avatarBytes = avatarBytes;
            this.avatarImage = loadAvatarImage(avatarBytes);
            setOpaque(false);
        }

        private Image loadAvatarImage(byte[] data) {
            if (data == null || data.length == 0) return null;

            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                return img;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            int w = getWidth();
            int h = getHeight();

            if (avatarImage != null) {
                Shape oldClip = g2.getClip();

                Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, w - 1, h - 1);
                g2.setClip(circle);
                g2.drawImage(avatarImage, 0, 0, w, h, null);

                g2.setClip(oldClip);
                g2.setColor(new Color(210, 226, 216));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(circle);

                g2.dispose();
                return;
            }

            // Avatar mặc định như giao diện cũ
            g2.setColor(new Color(224, 238, 224));
            g2.fillOval(0, 0, w - 1, h - 1);

            g2.setColor(new Color(118, 156, 126));
            g2.fillOval(w / 2 - 18, 24, 36, 36);

            g2.setColor(new Color(78, 136, 121));
            g2.fillRoundRect(w / 2 - 32, 62, 64, 42, 34, 34);

            g2.setColor(new Color(249, 210, 172));
            g2.fillOval(w / 2 - 22, 20, 44, 44);

            g2.setColor(new Color(44, 59, 54));
            g2.fillArc(w / 2 - 24, 14, 48, 30, 0, 180);

            String initials = name.trim().isEmpty()
                    ? "N"
                    : name.trim().substring(0, 1).toUpperCase();

            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.setColor(Color.WHITE);

            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initials, (w - fm.stringWidth(initials)) / 2, 89);

            g2.dispose();
        }
    }
}
