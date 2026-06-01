package com.dormassist.view.panel;

import com.dormassist.config.AppConstants;
import com.dormassist.config.Session;
import com.dormassist.dao.BillDAO;
import com.dormassist.model.PriceConfig;
import com.dormassist.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PricePanel extends JPanel {
    private final BillDAO billDAO = new BillDAO();
    private JTextField electricField, waterField, serviceField;
    private JLabel lastUpdatedLabel;

    public PricePanel() {
        setLayout(new BorderLayout());
        setBackground(AppConstants.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false); header.setBorder(new EmptyBorder(0, 0, 20, 0));
        header.add(UIHelper.sectionTitle("Cấu hình đơn giá dịch vụ"), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel centerRow = new JPanel(new GridLayout(1, 2, 20, 0));
        centerRow.setOpaque(false);

        // Card trái: form nhập giá
        JPanel leftCard = UIHelper.card();
        leftCard.setLayout(new BoxLayout(leftCard, BoxLayout.Y_AXIS));
        leftCard.setBorder(new EmptyBorder(28, 36, 28, 36));

        JLabel cardTitle = new JLabel("Thiết lập đơn giá hiện hành");
        cardTitle.setFont(AppConstants.FONT_HEADER); cardTitle.setForeground(AppConstants.TEXT_MAIN);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lastUpdatedLabel = UIHelper.labelMuted("Lần cập nhật: đang tải...");
        lastUpdatedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftCard.add(cardTitle);
        leftCard.add(Box.createVerticalStrut(4));
        leftCard.add(lastUpdatedLabel);
        leftCard.add(Box.createVerticalStrut(20));

        JSeparator sep = UIHelper.separator();
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        leftCard.add(sep);
        leftCard.add(Box.createVerticalStrut(20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false); form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 6, 10, 6); g.fill = GridBagConstraints.HORIZONTAL;

        electricField = UIHelper.styledField();
        waterField    = UIHelper.styledField();
        serviceField  = UIHelper.styledField();

        UIHelper.addFormRow(form, g, 0, "Đơn giá điện (đ/kWh):", electricField);
        UIHelper.addFormRow(form, g, 1, "Đơn giá nước (đ/m³):", waterField);
        UIHelper.addFormRow(form, g, 2, "Phí dịch vụ / phòng (đ):", serviceField);

        g.gridx = 1; g.gridy = 3;
        form.add(UIHelper.labelMuted("Áp dụng cho hóa đơn tạo từ tháng tiếp theo"), g);

        leftCard.add(form);
        leftCard.add(Box.createVerticalStrut(20));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false); btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn  = UIHelper.primaryButton("Lưu cấu hình giá");
        JButton resetBtn = UIHelper.grayButton("Tải lại");
        saveBtn.addActionListener(e -> saveConfig());
        resetBtn.addActionListener(e -> loadData());
        btnRow.add(saveBtn); btnRow.add(Box.createHorizontalStrut(10)); btnRow.add(resetBtn);
        leftCard.add(btnRow);

        // Card phải: hướng dẫn
        JPanel rightCard = UIHelper.card();
        rightCard.setLayout(new BoxLayout(rightCard, BoxLayout.Y_AXIS));
        rightCard.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel guideTitle = UIHelper.labelBold("Cách tính hóa đơn hằng tháng");
        guideTitle.setForeground(AppConstants.PRIMARY); guideTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightCard.add(guideTitle);
        rightCard.add(Box.createVerticalStrut(14));

        String[] guides = {
            "Tiền điện = (CS sau - CS trước) x Đơn giá điện",
            "Tiền nước = (CS sau - CS trước) x Đơn giá nước",
            "Phí dịch vụ = Phí cố định mỗi tháng / phòng",
            "Tiền phòng = Giá niêm yết từng phòng",
            "Tổng = Điện + Nước + DV + Tiền phòng"
        };
        for (String s : guides) {
            JLabel l = UIHelper.labelMuted("  " + s);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            l.setBorder(new EmptyBorder(0, 0, 6, 0));
            rightCard.add(l);
        }
        rightCard.add(Box.createVerticalStrut(16));

        JSeparator sep2 = UIHelper.separator();
        sep2.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        rightCard.add(sep2);
        rightCard.add(Box.createVerticalStrut(14));

        JLabel warnTitle = UIHelper.labelBold("Lưu ý quan trọng");
        warnTitle.setForeground(AppConstants.WARNING); warnTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightCard.add(warnTitle);
        rightCard.add(Box.createVerticalStrut(8));

        JLabel warn = new JLabel("<html><div style='width:200px'>"
            + "Thay đổi đơn giá chỉ ảnh hưởng đến hóa đơn <b>tạo sau</b> khi lưu. "
            + "Hóa đơn cũ không bị thay đổi."
            + "</div></html>");
        warn.setFont(AppConstants.FONT_SMALL); warn.setForeground(AppConstants.TEXT_SEC);
        warn.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightCard.add(warn);
        rightCard.add(Box.createVerticalGlue());

        centerRow.add(leftCard);
        centerRow.add(rightCard);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(AppConstants.BG_MAIN);
        GridBagConstraints wc = new GridBagConstraints();
        wc.fill = GridBagConstraints.BOTH; wc.weightx = 1; wc.weighty = 1;
        wrapper.add(centerRow, wc);
        add(wrapper, BorderLayout.CENTER);
    }

    private void loadData() {
        PriceConfig pc = billDAO.getLatestPriceConfig();
        electricField.setText(String.valueOf((long) pc.getElectricPrice()));
        waterField.setText(String.valueOf((long) pc.getWaterPrice()));
        serviceField.setText(String.valueOf((long) pc.getServiceFee()));
        lastUpdatedLabel.setText(pc.getEffectiveDate() != null
            ? "Lần cập nhật: " + UIHelper.DATE_FMT.format(pc.getEffectiveDate())
            : "Đang dùng giá mặc định");
    }

    private void saveConfig() {
        try {
            double elec  = Double.parseDouble(electricField.getText().trim());
            double water = Double.parseDouble(waterField.getText().trim());
            double svc   = Double.parseDouble(serviceField.getText().trim());
            if (elec <= 0 || water <= 0 || svc < 0) {
                UIHelper.showWarning(this, "Đơn giá phải là số dương!"); return;
            }
            if (!UIHelper.confirm(this,
                "Xác nhận lưu cấu hình giá mới?\n\n" +
                "  Đơn giá điện: " + UIHelper.formatCurrency(elec) + "/kWh\n" +
                "  Đơn giá nước: " + UIHelper.formatCurrency(water) + "/m3\n" +
                "  Phí dịch vụ: " + UIHelper.formatCurrency(svc) + "/phòng/tháng")) return;

            PriceConfig pc = new PriceConfig();
            pc.setElectricPrice(elec); pc.setWaterPrice(water); pc.setServiceFee(svc);
            if (billDAO.savePriceConfig(pc, Session.getUserId())) {
                UIHelper.showSuccess(this, "Đã lưu cấu hình giá thành công!");
                loadData();
            } else UIHelper.showError(this, "Lưu thất bại!");
        } catch (NumberFormatException e) {
            UIHelper.showError(this, "Dữ liệu không hợp lệ! Vui lòng nhập số nguyên.");
        }
    }
}
