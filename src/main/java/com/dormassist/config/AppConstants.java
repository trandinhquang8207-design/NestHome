package com.dormassist.config;

import java.awt.*;

public class AppConstants {

    public static final String APP_NAME     = "NestHome";
    public static final String APP_VERSION  = "1.0.0";
    public static final String APP_SUBTITLE = "Ký túc xá • Quản lý • Chăm sóc • Kết nối";

    // ===== VAI TRÒ =====
    public static final String ROLE_ADMIN_SUPER = "ADMIN_SUPER";
    public static final String ROLE_ADMIN_BASE  = "ADMIN_BASE";
    public static final String ROLE_BASE1 = "BASE1";
    public static final String ROLE_BASE2 = "BASE2";
    public static final String ROLE_BASE3 = "BASE3";

    public static String getRoleDisplay(String role) {
        if (role == null) return "";
        switch (role) {
            case ROLE_ADMIN_SUPER: return "Quản trị viên";
            case ROLE_ADMIN_BASE:  return "Quản trị viên";
            case ROLE_BASE1:       return "Trưởng tầng";
            case ROLE_BASE2:       return "Trưởng phòng";
            case ROLE_BASE3:       return "Sinh viên";
            default: return role;
        }
    }

    // ===== MÀU SẮC =====
    public static final Color PRIMARY       = new Color(37, 112, 98);
    public static final Color PRIMARY_DARK  = new Color(23, 77, 69);
    public static final Color PRIMARY_LIGHT = new Color(221, 238, 230);
    public static final Color SUCCESS       = new Color(76, 156, 136);
    public static final Color WARNING       = new Color(222, 164, 78);
    public static final Color DANGER        = new Color(226, 105, 100);
    public static final Color INFO          = new Color(69, 163, 151);
    public static final Color ORANGE        = new Color(221, 137, 82);
    public static final Color PURPLE        = new Color(118, 147, 126);

    public static final Color SIDEBAR_BG           = new Color(246, 250, 245);
    public static final Color SIDEBAR_HOVER        = new Color(232, 242, 235);
    public static final Color SIDEBAR_ACTIVE       = new Color(221, 238, 230);
    public static final Color SIDEBAR_TEXT         = new Color(74, 91, 86);
    public static final Color SIDEBAR_TEXT_ACTIVE  = new Color(23, 77, 69);
    public static final Color SIDEBAR_SECTION_TEXT = new Color(120, 136, 128);

    public static final Color BG_MAIN   = new Color(250, 248, 243);
    public static final Color BG_CARD   = new Color(255, 255, 252);
    public static final Color BORDER    = new Color(223, 231, 220);
    public static final Color TEXT_MAIN = new Color(32, 48, 43);
    public static final Color TEXT_SEC  = new Color(85, 101, 96);
    public static final Color TEXT_MUTED= new Color(135, 148, 142);

    // ===== FONT =====
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUB     = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SIDEBAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 13);

    // ===== KÍCH THƯỚC =====
    public static final int SIDEBAR_W    = 255;
    public static final int HEADER_H     = 64;
    public static final int ROW_H        = 40;
    public static final int BTN_H        = 34;
    public static final int FIELD_H      = 34;
    public static final int RADIUS_CARD  = 14;
    public static final int RADIUS_BTN   = 9;

    // ===== DỊCH TRẠNG THÁI (100% TIẾNG VIỆT) =====
    public static String getStatusDisplay(String s) {
        if (s == null) return "";
        switch (s) {
            // Chung
            case "PENDING":      return "Chờ duyệt";
            case "APPROVED":     return "Đã duyệt";
            case "REJECTED":     return "Từ chối";
            case "IN_PROGRESS":  return "Đang xử lý";
            case "RESOLVED":     return "Hoàn thành";
            case "CONFIRMED":    return "Đã xác nhận";
            // Hóa đơn
            case "PAID":         return "Đã thanh toán";
            case "UNPAID":       return "Chưa thanh toán";
            case "OVERDUE":      return "Quá hạn";
            case "WAIVED":       return "Miễn giảm";
            case "BILLED":       return "Đã lập hóa đơn";
            // Sinh viên
            case "ACTIVE":           return "Đang cư trú";
            case "TEMPORARY_ABSENT": return "Tạm vắng";
            case "MOVED_OUT":        return "Đã chuyển đi";
            // Phòng
            case "AVAILABLE":    return "Còn trống";
            case "FULL":         return "Đầy";
            case "MAINTENANCE":  return "Đang bảo trì";
            case "CLOSED":       return "Đóng cửa";
            // Loại phòng
            case "STANDARD":         return "Tiêu chuẩn";
            case "VIP":              return "Cao cấp";
            case "DISABLED_ACCESS":  return "Đặc biệt";
            // Tài sản
            case "GOOD":    return "Tốt";
            case "FAIR":    return "Bình thường";
            case "BROKEN":  return "Hỏng";
            case "MISSING": return "Mất";
            // Sự cố ưu tiên
            case "LOW":    return "Thấp";
            case "MEDIUM": return "Trung bình";
            case "HIGH":   return "Cao";
            case "URGENT": return "Khẩn cấp";
            // Khách / Phòng
            case "CHECKED_IN":  return "Đã vào";
            case "CHECKED_OUT": return "Đã ra";
            case "SUBMITTED":   return "Đã nộp";
            case "REVIEWED":    return "Đã xem xét";
            case "ACTIONED":    return "Đã xử lý";
            // Điểm
            case "BONUS":   return "Cộng điểm";
            case "PENALTY": return "Trừ điểm";
            // Thu chi
            case "INCOME":  return "Thu";
            case "EXPENSE": return "Chi";
            // Bàn giao
            case "MOVE_IN":  return "Nhận phòng";
            case "MOVE_OUT": return "Trả phòng";
            // Giới tính
            case "MALE":   return "Nam";
            case "FEMALE": return "Nữ";
            case "OTHER":  return "Khác";
            // Loại tài sản
            case "BED":    return "Giường";
            case "LOCKER": return "Tủ";
            case "AC":     return "Điều hòa";
            case "FAN":    return "Quạt";
            case "DESK":   return "Bàn học";
            case "CHAIR":  return "Ghế";
            default: return s;
        }
    }

    public static Color getStatusColor(String s) {
        if (s == null) return new Color(100, 116, 139);
        switch (s) {
            case "PENDING": case "UNPAID":    return WARNING;
            case "APPROVED": case "PAID":
            case "RESOLVED": case "GOOD":
            case "ACTIVE": case "AVAILABLE":  return SUCCESS;
            case "REJECTED": case "OVERDUE":
            case "BROKEN": case "MISSING":    return DANGER;
            case "IN_PROGRESS": case "MAINTENANCE":
            case "SUBMITTED": case "CONFIRMED": return INFO;
            case "FULL": case "MOVE_IN":      return PRIMARY;
            case "FAIR": case "MEDIUM":       return ORANGE;
            case "URGENT": case "HIGH":       return DANGER;
            case "BONUS":                     return SUCCESS;
            case "PENALTY":                   return DANGER;
            default: return new Color(100, 116, 139);
        }
    }
}
