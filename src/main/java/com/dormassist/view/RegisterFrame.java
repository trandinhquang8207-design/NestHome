package com.dormassist.view;

import com.dormassist.config.AppConstants;
import com.dormassist.dao.UserDAO;
import com.dormassist.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterFrame extends JDialog {
    private JTextField usernameField, fullNameField, studentCodeField, emailField, phoneField, tokenField;
    private JPasswordField passwordField, confirmPassField;
    private JComboBox<String> roleCombo;
    private final UserDAO userDAO = new UserDAO();

    // Map hiển thị -> role code
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

    public RegisterFrame(Frame parent) {
        super(parent, "Đăng ký tài khoản mới", true);
        setSize(500, 625);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppConstants.BG_MAIN);

        // Header
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        hdr.setBackground(AppConstants.PRIMARY);
        JPanel htxt = new JPanel(); htxt.setOpaque(false); htxt.setLayout(new BoxLayout(htxt, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Tạo tài khoản mới");
        t.setFont(AppConstants.FONT_HEADER); t.setForeground(Color.WHITE);
        JLabel s = new JLabel("Cần mã token hợp lệ do quản trị viên cấp");
        s.setFont(AppConstants.FONT_SMALL); s.setForeground(new Color(200,220,255));
        htxt.add(t); htxt.add(s); hdr.add(htxt);
        root.add(hdr, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20,30,10,30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7,6,7,6); g.fill = GridBagConstraints.HORIZONTAL;

        fullNameField   = UIHelper.styledField();
        studentCodeField = UIHelper.styledField();
        usernameField   = UIHelper.styledField();
        passwordField   = UIHelper.styledPasswordField();
        confirmPassField= UIHelper.styledPasswordField();
        emailField      = UIHelper.styledField();
        phoneField      = UIHelper.styledField();
        tokenField      = UIHelper.styledField();
        tokenField.setFont(new Font("Consolas", Font.BOLD, 13));

        // Combo chỉ hiển thị tên tiếng Việt, KHÔNG có prefix BASE1/BASE2...
        roleCombo = UIHelper.styledCombo(ROLE_DISPLAY);

        UIHelper.addFormRow(form, g, 0, "Họ và tên: *",         fullNameField);
        UIHelper.addFormRow(form, g, 1, "MSSV:",                studentCodeField);
        UIHelper.addFormRow(form, g, 2, "Tên đăng nhập: *",     usernameField);
        UIHelper.addFormRow(form, g, 3, "Mật khẩu: *",          passwordField);
        UIHelper.addFormRow(form, g, 4, "Xác nhận mật khẩu: *", confirmPassField);
        UIHelper.addFormRow(form, g, 5, "Email:",               emailField);
        UIHelper.addFormRow(form, g, 6, "Số điện thoại:",       phoneField);
        UIHelper.addFormRow(form, g, 7, "Vai trò: *",           roleCombo);
        UIHelper.addFormRow(form, g, 8, "Mã token: *",          tokenField);

        g.gridx = 0;
        g.gridy = 9;
        g.gridwidth = 2;
        form.add(UIHelper.labelMuted("(*) Bắt buộc. Mã token do quản trị viên cấp theo vai trò."), g);

        JScrollPane sp = UIHelper.smoothScroll(new JScrollPane(form));
        sp.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER));
        root.add(sp, BorderLayout.CENTER);

        // Buttons
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btn.setBackground(AppConstants.BG_MAIN);
        btn.setBorder(BorderFactory.createMatteBorder(1,0,0,0, AppConstants.BORDER));
        JButton cancelBtn = UIHelper.grayButton("Hủy");
        cancelBtn.addActionListener(e -> dispose());
        JButton regBtn = UIHelper.primaryButton("Đăng ký");
        regBtn.addActionListener(e -> doRegister());
        btn.add(cancelBtn); btn.add(regBtn);
        root.add(btn, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void doRegister() {
        String name  = fullNameField.getText().trim();
        String studentCode = studentCodeField.getText().trim();
        String uname = usernameField.getText().trim();
        String pass  = new String(passwordField.getPassword());
        String conf  = new String(confirmPassField.getPassword());
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String token = tokenField.getText().trim();
        int idx      = roleCombo.getSelectedIndex();
        String role  = idx >= 0 ? ROLE_CODE[idx] : "";

        if (AppConstants.ROLE_BASE3.equals(role) && studentCode.isEmpty()) {
            UIHelper.showWarning(this, "Sinh viên cần nhập MSSV để hệ thống đồng bộ hồ sơ chính xác.");
            return;
        }

        if (name.isEmpty()||uname.isEmpty()||pass.isEmpty()||token.isEmpty()) {
            UIHelper.showWarning(this,"Vui lòng điền đầy đủ các trường bắt buộc (*)!"); return;
        }
        if (!pass.equals(conf)) { UIHelper.showError(this,"Mật khẩu xác nhận không khớp!"); return; }
        if (pass.length() < 6)  { UIHelper.showWarning(this,"Mật khẩu phải có ít nhất 6 ký tự!"); return; }
        if (userDAO.usernameExists(uname)) { UIHelper.showError(this,"Tên đăng nhập đã tồn tại!"); return; }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        boolean ok = userDAO.register(uname, pass, name, email, phone, role, token, studentCode);
        setCursor(Cursor.getDefaultCursor());

        if (ok) { UIHelper.showSuccess(this,"Đăng ký thành công!\nVui lòng đăng nhập."); dispose(); }
        else     UIHelper.showError(this,"Đăng ký thất bại!\nKiểm tra lại Mã token và vai trò.");
    }
}
