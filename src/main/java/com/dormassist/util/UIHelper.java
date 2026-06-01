package com.dormassist.util;

import com.dormassist.config.AppConstants;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UIHelper {

    public static final SimpleDateFormat DATE_FMT     = new SimpleDateFormat("dd/MM/yyyy");
    public static final SimpleDateFormat DATETIME_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static final NumberFormat CURRENCY_FMT     = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    static { CURRENCY_FMT.setMinimumFractionDigits(0); }

    public static String formatCurrency(double a) { return CURRENCY_FMT.format(a) + " đ"; }
    public static String formatDate(Date d)        { return d != null ? DATE_FMT.format(d) : ""; }
    public static String formatDateTime(Date d)    { return d != null ? DATETIME_FMT.format(d) : ""; }

    public static void applyGlobalUI() {
        Font appFont = AppConstants.FONT_BODY;
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key != null && key.toString().toLowerCase().endsWith("font")) UIManager.put(key, appFont);
        }
        UIManager.put("Button.arc", 18);
        UIManager.put("Button.disabledText", new Color(120, 136, 128));
        UIManager.put("Component.arc", 18);
        UIManager.put("TextComponent.arc", 14);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollPane.smoothScrolling", true);
        UIManager.put("Table.rowHeight", AppConstants.ROW_H);
        UIManager.put("Table.showHorizontalLines", false);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("control", AppConstants.BG_MAIN);
    }

    // ===== NÚT BẤM =====
    public static JButton primaryButton(String text) { return styledBtn(text, AppConstants.PRIMARY, Color.WHITE); }
    public static JButton successButton(String text) { return styledBtn(text, AppConstants.SUCCESS, Color.WHITE); }
    public static JButton dangerButton(String text)  { return styledBtn(text, AppConstants.DANGER, Color.WHITE); }
    public static JButton warningButton(String text) { return styledBtn(text, AppConstants.WARNING, Color.WHITE); }
    public static JButton infoButton(String text)    { return styledBtn(text, AppConstants.INFO, Color.WHITE); }
    public static JButton grayButton(String text)    { return styledBtn(text, new Color(236, 242, 235), AppConstants.TEXT_MAIN); }
    public static JButton outlineButton(String text) { JButton b = styledBtn(text, AppConstants.BG_CARD, AppConstants.PRIMARY_DARK); b.putClientProperty("outline", true); return b; }

    private static JButton styledBtn(String text, Color bg, Color fg) {
        final Color disabledBg = new Color(232, 242, 235);   // #E8F2EB
        final Color disabledFg = new Color(120, 136, 128);   // #788880

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color paintBg;

                if (!isEnabled()) {
                    paintBg = disabledBg;
                    setForeground(disabledFg);
                } else {
                    paintBg = getModel().isPressed()
                            ? bg.darker()
                            : (getModel().isRollover() ? brighten(bg, 1.06f) : bg);
                    setForeground(fg);
                }

                g2.setColor(paintBg);
                g2.fill(new RoundRectangle2D.Float(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        AppConstants.RADIUS_BTN * 2,
                        AppConstants.RADIUS_BTN * 2
                ));

                if (Boolean.TRUE.equals(getClientProperty("outline"))) {
                    Color borderColor = isEnabled()
                            ? AppConstants.PRIMARY_LIGHT.darker()
                            : new Color(210, 224, 216);

                    g2.setColor(borderColor);
                    g2.draw(new RoundRectangle2D.Float(
                            0.5f,
                            0.5f,
                            getWidth() - 1,
                            getHeight() - 1,
                            AppConstants.RADIUS_BTN * 2,
                            AppConstants.RADIUS_BTN * 2
                    ));
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setForeground(fg);
        btn.setFont(AppConstants.FONT_BOLD);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 18, AppConstants.BTN_H));

        btn.addPropertyChangeListener("enabled", e -> {
            btn.setCursor(btn.isEnabled()
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
            btn.repaint();
        });

        return btn;
    }

    private static Color brighten(Color c, float factor) {
        return new Color(Math.min(255, (int)(c.getRed()*factor)), Math.min(255, (int)(c.getGreen()*factor)), Math.min(255, (int)(c.getBlue()*factor)), c.getAlpha());
    }

    // ===== TRƯỜNG NHẬP =====
    public static JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(AppConstants.FONT_BODY);
        f.setPreferredSize(new Dimension(200, AppConstants.FIELD_H));
        f.setBackground(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        return f;
    }

    public static JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(AppConstants.FONT_BODY);
        f.setPreferredSize(new Dimension(200, AppConstants.FIELD_H));
        f.setBackground(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER, 1, true),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        return f;
    }

    public static JTextArea styledTextArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setFont(AppConstants.FONT_BODY); ta.setLineWrap(true); ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        ta.setBackground(Color.WHITE);
        return ta;
    }

    public static JComboBox<String> styledCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(AppConstants.FONT_BODY);
        cb.setPreferredSize(new Dimension(200, AppConstants.FIELD_H));
        cb.setBackground(Color.WHITE);
        return cb;
    }

    // ===== NHÃN =====
    public static JLabel label(String text)      { JLabel l=new JLabel(text); l.setFont(AppConstants.FONT_BODY); l.setForeground(AppConstants.TEXT_MAIN); return l; }
    public static JLabel labelBold(String text)  { JLabel l=new JLabel(text); l.setFont(AppConstants.FONT_BOLD); l.setForeground(AppConstants.TEXT_MAIN); return l; }
    public static JLabel labelMuted(String text) { JLabel l=new JLabel(text); l.setFont(AppConstants.FONT_SMALL); l.setForeground(AppConstants.TEXT_MUTED); return l; }
    public static JLabel labelTitle(String text) { JLabel l=new JLabel(text); l.setFont(AppConstants.FONT_TITLE); l.setForeground(AppConstants.TEXT_MAIN); return l; }
    public static JLabel sectionTitle(String text){ JLabel l=new JLabel(text); l.setFont(AppConstants.FONT_SUB); l.setForeground(AppConstants.PRIMARY_DARK); return l; }

    // ===== CARD =====
    public static JPanel card() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,12));
                g2.fill(new RoundRectangle2D.Float(2,3,getWidth()-5,getHeight()-5,AppConstants.RADIUS_CARD*2,AppConstants.RADIUS_CARD*2));
                g2.setColor(AppConstants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-4,getHeight()-4,AppConstants.RADIUS_CARD*2,AppConstants.RADIUS_CARD*2));
                g2.setColor(AppConstants.BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-5,getHeight()-5,AppConstants.RADIUS_CARD*2,AppConstants.RADIUS_CARD*2));
                g2.dispose();
            }
        };
        p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(16,16,18,18));
        return p;
    }

    public static JPanel statCard(String title, String value, Color color) {
        JPanel c = card(); c.setLayout(new BorderLayout(10,4)); c.setPreferredSize(new Dimension(180, 104));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); top.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 38)); g2.fillRoundRect(0,0,34,26,13,13);
                g2.setColor(color); g2.fillOval(12,8,10,10); g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(34,26)); dot.setOpaque(false);
        JLabel tl=new JLabel("  "+title); tl.setFont(AppConstants.FONT_SMALL); tl.setForeground(AppConstants.TEXT_SEC);
        top.add(dot); top.add(tl);
        JLabel vl=new JLabel(value,SwingConstants.LEFT); vl.setFont(new Font("Segoe UI",Font.BOLD,27)); vl.setForeground(color);
        c.add(top,BorderLayout.NORTH); c.add(vl,BorderLayout.CENTER);
        return c;
    }

    // ===== BẢNG =====
    public static JTable styledTable(String[] cols) {
        DefaultTableModel m = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r,int c) { return false; }
        };
        JTable t = new JTable(m); styleTable(t); return t;
    }

    public static void styleTable(JTable t) {
        t.setFont(AppConstants.FONT_BODY); t.setRowHeight(AppConstants.ROW_H);
        t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0,0));
        t.setSelectionBackground(AppConstants.PRIMARY_LIGHT); t.setSelectionForeground(AppConstants.TEXT_MAIN);
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableHeader h = t.getTableHeader();
        h.setFont(AppConstants.FONT_BOLD); h.setBackground(new Color(241,247,240));
        h.setForeground(AppConstants.TEXT_SEC); h.setPreferredSize(new Dimension(h.getWidth(),42));
        h.setBorder(BorderFactory.createMatteBorder(0,0,1,0,AppConstants.BORDER));
        h.setReorderingAllowed(false);
        t.setDefaultRenderer(Object.class, new NestHomeCellRenderer());
        configureColumns(t);
    }

    private static class NestHomeCellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable tb,Object v,boolean sel,boolean foc,int r,int c) {
            super.getTableCellRendererComponent(tb,v,sel,foc,r,c);
            setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
            if(!sel) setBackground(r%2==0?Color.WHITE:new Color(250,252,248));
            setForeground(sel ? AppConstants.TEXT_MAIN : AppConstants.TEXT_MAIN);
            String header = tb.getColumnName(c);
            if ("ID".equals(header) || "Điểm".equals(header) || "Tầng".equals(header) || "Số lượng".equals(header)) setHorizontalAlignment(SwingConstants.CENTER);
            else if ("Giá thuê".equals(header) || "Tổng tiền".equals(header) || header.contains("Tiền")) setHorizontalAlignment(SwingConstants.RIGHT);
            else setHorizontalAlignment(SwingConstants.LEFT);
            return this;
        }
    }

    public static void configureColumns(JTable table) {
        if (table == null || table.getColumnModel() == null) return;
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            String name = table.getColumnName(i);
            int w = preferredWidthFor(name);
            col.setMinWidth(Math.max(46, Math.min(w, 120)));
            col.setPreferredWidth(w);
            if ("ID".equals(name)) col.setMaxWidth(70);
        }
    }

    private static int preferredWidthFor(String name) {
        if (name == null) return 120;
        switch (name) {
            case "ID": return 56;
            case "Mã token": return 200;
            case "Người sử dụng": return 170;
            case "Ngày tạo":
            case "Ngày sử dụng":
            case "Thời gian":
            case "Hạn thanh toán":
            case "Ngày vào":
            case "Ngày ra": return 170;
            case "Trạng thái": return 150;
            case "Vai trò": return 150;
            case "Tiêu đề": return 250;
            case "Nội dung tóm tắt": return 330;
            case "Nội dung": return 360;
            case "Người đăng":
            case "Người ghi":
            case "Người báo": return 170;
            case "Họ tên":
            case "Sinh viên":
            case "Tên đăng nhập": return 190;
            case "MSSV":
            case "Mã SV":
            case "Mã tài sản": return 130;
            case "Số phòng":
            case "Phòng":
            case "Phòng ở": return 115;
            case "Tòa nhà": return 135;
            case "Loại phòng":
            case "Loại tài sản": return 150;
            case "Giá thuê":
            case "Tổng tiền": return 145;
            case "Lý do":
            case "Ghi chú": return 280;
            default: return Math.max(110, Math.min(240, name.length() * 12 + 64));
        }
    }

    public static JScrollPane tableScroll(JTable t) {
        configureColumns(t);
        JScrollPane sp=new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(AppConstants.BORDER,1,true));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        return smoothScroll(sp);
    }

    public static JScrollPane smoothScroll(JScrollPane sp) {
        if (sp == null) return null;
        sp.getVerticalScrollBar().setUnitIncrement(20);
        sp.getVerticalScrollBar().setBlockIncrement(120);
        sp.getHorizontalScrollBar().setUnitIncrement(20);
        sp.getHorizontalScrollBar().setBlockIncrement(120);
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        sp.setWheelScrollingEnabled(true);
        sp.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        sp.setBorder(sp.getBorder() != null ? sp.getBorder() : BorderFactory.createEmptyBorder());
        return sp;
    }

    public static void applySmoothScrolling(Container c) {
        if (c == null) return;
        if (c instanceof JScrollPane) smoothScroll((JScrollPane)c);
        for (Component child : c.getComponents()) if (child instanceof Container) applySmoothScrolling((Container) child);
    }

    static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { thumbColor = new Color(176, 198, 188); trackColor = new Color(244, 248, 244); }
        @Override protected JButton createDecreaseButton(int orientation) { return invisibleButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return invisibleButton(); }
        private JButton invisibleButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); b.setMinimumSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0)); return b; }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isDragging ? AppConstants.PRIMARY : thumbColor);
            g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 10, 10); g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2=(Graphics2D)g.create(); g2.setColor(trackColor); g2.fillRect(r.x, r.y, r.width, r.height); g2.dispose();
        }
    }

    public static Icon menuIcon(String key, int size) { return new SimpleLineIcon(key, size); }

    private static class SimpleLineIcon implements Icon {
        private final String key; private final int size;
        SimpleLineIcon(String key, int size) { this.key = key == null ? "" : key; this.size = size; }
        public int getIconWidth() { return size; }
        public int getIconHeight() { return size; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            Color col = c != null && c.getForeground()!=null ? c.getForeground() : AppConstants.PRIMARY;
            g2.setColor(col); g2.setStroke(new BasicStroke(Math.max(1.4f, size/12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int s=size, cx=x+s/2, cy=y+s/2;
            if (key.contains("dashboard")) { g2.drawLine(x+3,y+s-5,cx,y+3); g2.drawLine(cx,y+3,x+s-3,y+s-5); g2.drawRect(x+6,y+s/2,x+s-12-x, s/2-4); }
            else if (key.contains("room") || key.contains("rooms")) { g2.drawRect(x+4,y+4,s-8,s-8); g2.drawLine(x+4,cy,x+s-4,cy); g2.drawLine(cx,y+4,cx,y+s-4); }
            else if (key.contains("student") || key.contains("user") || key.contains("profile")) { g2.drawOval(cx-4,y+4,8,8); g2.drawArc(x+4,y+12,s-8,s-7,20,140); }
            else if (key.contains("bill") || key.contains("price")) { g2.drawRoundRect(x+5,y+3,s-10,s-6,4,4); g2.drawLine(x+8,y+8,x+s-8,y+8); g2.drawLine(x+8,y+13,x+s-8,y+13); }
            else if (key.contains("incident")) { g2.drawLine(x+5,y+s-4,x+s-4,y+5); g2.drawLine(x+s-8,y+5,x+s-4,y+5); g2.drawLine(x+s-4,y+5,x+s-4,y+9); }
            else if (key.contains("asset")) { g2.drawRoundRect(x+4,y+6,s-8,s-10,5,5); g2.drawLine(x+7,y+10,x+s-7,y+10); }
            else if (key.contains("notice")) { g2.drawRoundRect(x+4,y+5,s-8,s-10,4,4); g2.drawLine(x+4,y+7,cx,cy); g2.drawLine(x+s-4,y+7,cx,cy); }
            else if (key.contains("discipline")) { int[] xs={cx,x+6,x+s-5}; int[] ys={y+4,y+s-5,y+s-5}; g2.drawPolygon(xs,ys,3); }
            else if (key.contains("transfer")) { g2.drawLine(x+4,y+7,x+s-5,y+7); g2.drawLine(x+s-8,y+4,x+s-5,y+7); g2.drawLine(x+s-8,y+10,x+s-5,y+7); g2.drawLine(x+s-4,y+s-7,x+5,y+s-7); g2.drawLine(x+8,y+s-10,x+5,y+s-7); g2.drawLine(x+8,y+s-4,x+5,y+s-7); }
            else if (key.contains("token")) { g2.drawOval(x+4,cy-4,8,8); g2.drawLine(x+12,cy,x+s-3,cy); g2.drawLine(x+s-7,cy,x+s-7,cy+5); }
            else { g2.drawOval(x+4,y+4,s-8,s-8); g2.drawLine(cx,y+8,cx,cy); g2.drawLine(cx,cy,x+s-7,y+s-7); }
            g2.dispose();
        }
    }

    public static JLabel badge(String text, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 32));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.dispose(); super.paintComponent(g);
            }
        };
        l.setForeground(color.darker()); l.setFont(AppConstants.FONT_BOLD); l.setOpaque(false);
        l.setBorder(BorderFactory.createEmptyBorder(3,10,3,10));
        return l;
    }

    public static JTextField searchField(String hint) {
        JTextField f = new JTextField() {
            { addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { if(getText().equals(hint)){setText("");setForeground(AppConstants.TEXT_MAIN);} }
                public void focusLost(FocusEvent e)  { if(getText().isEmpty()){setText(hint);setForeground(AppConstants.TEXT_MUTED);} }
            }); setText(hint); setForeground(AppConstants.TEXT_MUTED); }
        };
        f.setFont(AppConstants.FONT_BODY); f.setPreferredSize(new Dimension(260, AppConstants.FIELD_H));
        f.setBackground(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppConstants.BORDER,1,true),
            BorderFactory.createEmptyBorder(5,12,5,12)));
        return f;
    }

    // ===== HỘP THOẠI =====
    public static void showSuccess(Component p, String msg) { JOptionPane.showMessageDialog(p, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE); }
    public static void showError(Component p, String msg)   { JOptionPane.showMessageDialog(p, msg, "Lỗi", JOptionPane.ERROR_MESSAGE); }
    public static void showWarning(Component p, String msg) { JOptionPane.showMessageDialog(p, msg, "Cảnh báo", JOptionPane.WARNING_MESSAGE); }
    public static boolean confirm(Component p, String msg)  { return JOptionPane.showConfirmDialog(p,msg,"Xác nhận",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION; }
    public static void setStatus(JLabel status, String msg) { if (status != null) status.setText(msg == null ? "" : msg); }

    // ===== TIỆN ÍCH BẢNG =====
    public static void clearTable(JTable t) { ((DefaultTableModel)t.getModel()).setRowCount(0); }
    public static void addRow(JTable t, Object... row) { ((DefaultTableModel)t.getModel()).addRow(row); }

    public static void addFormRow(JPanel p, GridBagConstraints g, int row, String lbl, JComponent field) {
        g.gridx=0; g.gridy=row; g.weightx=0.35; g.anchor=GridBagConstraints.WEST;
        p.add(labelBold(lbl), g);
        g.gridx=1; g.weightx=0.65; g.fill=GridBagConstraints.HORIZONTAL;
        if (field instanceof JScrollPane) smoothScroll((JScrollPane) field);
        p.add(field, g);
    }

    public static JSeparator separator() { JSeparator s=new JSeparator(); s.setForeground(AppConstants.BORDER); return s; }

    public static JPanel toolbar(String title, JButton... buttons) {
        JPanel tb = new JPanel(new BorderLayout(10, 0));
        tb.setOpaque(false); tb.setBorder(BorderFactory.createEmptyBorder(0,0,14,0));
        if (title != null) tb.add(sectionTitle(title), BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); right.setOpaque(false);
        for (JButton b : buttons) right.add(b);
        tb.add(right, BorderLayout.EAST); return tb;
    }

    public static <T> void runAsync(Supplier<T> loader, Consumer<T> onDone) {
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() { return loader.get(); }
            @Override protected void done() { try { onDone.accept(get()); } catch (Exception ex) { ex.printStackTrace(); } }
        }.execute();
    }
}
