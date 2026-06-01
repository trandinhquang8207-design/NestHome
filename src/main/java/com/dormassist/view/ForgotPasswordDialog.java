package com.dormassist.view;

import com.dormassist.dao.PasswordResetDAO;
import com.dormassist.dao.UserDAO;
import com.dormassist.model.User;
import com.dormassist.util.EmailService;
import com.dormassist.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ForgotPasswordDialog extends JDialog {
    private final UserDAO userDAO = new UserDAO();
    private final PasswordResetDAO resetDAO = new PasswordResetDAO();
    private final EmailService emailService = new EmailService();
    private final JTextField emailField = UIHelper.styledField();
    private final JTextField codeField = UIHelper.styledField();
    private final JPasswordField newPassField = UIHelper.styledPasswordField();
    private final JPasswordField confirmPassField = UIHelper.styledPasswordField();
    private User targetUser;

    public ForgotPasswordDialog(Window owner) {
        super(owner, "Quên mật khẩu", ModalityType.APPLICATION_MODAL);
        setSize(480, 420); setLocationRelativeTo(owner); setResizable(false); buildUI();
    }
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 14)); root.setBackground(Color.WHITE); root.setBorder(new EmptyBorder(24, 28, 24, 28));
        JPanel head = new JPanel(new GridLayout(2, 1, 0, 4)); head.setOpaque(false);
        head.add(UIHelper.labelTitle("Đặt lại mật khẩu")); head.add(UIHelper.labelMuted("Nhập email đã đăng ký. Hệ thống sẽ gửi mã xác nhận qua Gmail.")); root.add(head, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(7,0,7,0); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        UIHelper.addFormRow(form, g, 0, "Email đã đăng ký:", emailField); UIHelper.addFormRow(form, g, 1, "Mã xác nhận:", codeField); UIHelper.addFormRow(form, g, 2, "Mật khẩu mới:", newPassField); UIHelper.addFormRow(form, g, 3, "Xác nhận mật khẩu:", confirmPassField); root.add(form, BorderLayout.CENTER);
        JButton sendBtn = UIHelper.infoButton("Gửi mã"); JButton resetBtn = UIHelper.primaryButton("Đổi mật khẩu"); JButton closeBtn = UIHelper.grayButton("Đóng");
        sendBtn.addActionListener(e -> sendCode()); resetBtn.addActionListener(e -> resetPassword()); closeBtn.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); buttons.setOpaque(false); buttons.add(sendBtn); buttons.add(resetBtn); buttons.add(closeBtn); root.add(buttons, BorderLayout.SOUTH); setContentPane(root);
    }
    private void sendCode() {
        String email = emailField.getText().trim(); if (email.isEmpty()) { UIHelper.showWarning(this, "Vui lòng nhập email đã đăng ký."); return; }
        targetUser = userDAO.getByEmail(email); if (targetUser == null) { UIHelper.showError(this, "Không tìm thấy tài khoản đang hoạt động với email này."); return; }
        if (resetDAO.countRecentRequests(targetUser.getId()) >= 5) { UIHelper.showWarning(this, "Bạn đã yêu cầu quá nhiều mã. Vui lòng thử lại sau 15 phút."); return; }
        String code = resetDAO.generateCode(); if (!resetDAO.createCode(targetUser.getId(), email, code)) { UIHelper.showError(this, "Không thể tạo mã xác nhận. Vui lòng kiểm tra database."); return; }
        try { emailService.sendResetCode(email, code, targetUser.getFullName()); UIHelper.showSuccess(this, "Mã xác nhận đã được gửi đến Gmail của bạn."); }
        catch (Exception ex) { UIHelper.showError(this, "Đã tạo mã nhưng gửi Gmail thất bại:\n" + ex.getMessage() + "\n\nHãy cấu hình SMTP trong db.properties bằng mật khẩu ứng dụng Gmail."); }
    }
    private void resetPassword() {
        if (targetUser == null) targetUser = userDAO.getByEmail(emailField.getText().trim());
        if (targetUser == null) { UIHelper.showWarning(this, "Vui lòng nhập email và bấm Gửi mã trước."); return; }
        String code = codeField.getText().trim(); String p1 = new String(newPassField.getPassword()); String p2 = new String(confirmPassField.getPassword());
        if (code.isEmpty() || p1.isEmpty() || p2.isEmpty()) { UIHelper.showWarning(this, "Vui lòng nhập đủ mã xác nhận và mật khẩu mới."); return; }
        if (p1.length() < 6) { UIHelper.showWarning(this, "Mật khẩu mới phải có ít nhất 6 ký tự."); return; }
        if (!p1.equals(p2)) { UIHelper.showError(this, "Mật khẩu xác nhận không khớp."); return; }
        if (resetDAO.resetPassword(targetUser.getId(), code, p1)) { UIHelper.showSuccess(this, "Đổi mật khẩu thành công. Vui lòng đăng nhập lại."); dispose(); }
        else UIHelper.showError(this, "Mã xác nhận không đúng hoặc đã hết hạn.");
    }
}
