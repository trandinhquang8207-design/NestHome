package com.dormassist.util;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

public class EmailService {
    private static final String CONFIG_FILE = "db.properties";

    public boolean isConfigured() {
        Properties p = load();
        return !blank(p.getProperty("smtp.host")) && !blank(p.getProperty("smtp.port"))
                && !blank(p.getProperty("smtp.username")) && !blank(p.getProperty("smtp.password"));
    }

    public void sendResetCode(String to, String code, String fullName) throws IOException {
        String name = blank(fullName) ? "bạn" : fullName;
        String body = "Xin chào " + name + ",\n\n"
                + "Mã xác nhận đổi mật khẩu của bạn là: " + code + "\n"
                + "Mã này có hiệu lực trong 10 phút. Không chia sẻ mã này cho người khác.\n\n"
                + "NestHome - Ký túc xá";
        send(to, "Mã xác nhận đặt lại mật khẩu NestHome", body);
    }

    public void send(String to, String subject, String body) throws IOException {
        Properties p = load();
        String host = p.getProperty("smtp.host", "smtp.gmail.com").trim();
        int port = Integer.parseInt(p.getProperty("smtp.port", "465").trim());
        String username = p.getProperty("smtp.username", "").trim();
        String password = p.getProperty("smtp.password", "").trim();
        String from = p.getProperty("smtp.from", username).trim();
        if (blank(username) || blank(password)) throw new IOException("Chưa cấu hình SMTP trong db.properties.");
        try (SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port)) {
            socket.setSoTimeout(15000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            expect(in, 220); cmd(out, "EHLO localhost"); expectAny(in, 250);
            cmd(out, "AUTH LOGIN"); expect(in, 334);
            cmd(out, Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8))); expect(in, 334);
            cmd(out, Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8))); expect(in, 235);
            cmd(out, "MAIL FROM:<" + from + ">"); expect(in, 250);
            cmd(out, "RCPT TO:<" + to + ">"); expectAny(in, 250, 251);
            cmd(out, "DATA"); expect(in, 354);
            out.write(buildMessage(from, to, subject, body)); out.write("\r\n.\r\n"); out.flush(); expect(in, 250);
            cmd(out, "QUIT");
        }
    }

    private String buildMessage(String from, String to, String subject, String body) {
        String encodedSubject = "=?UTF-8?B?" + Base64.getEncoder().encodeToString(subject.getBytes(StandardCharsets.UTF_8)) + "?=";
        return "From: " + from + "\r\nTo: " + to + "\r\nSubject: " + encodedSubject
                + "\r\nMIME-Version: 1.0\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Transfer-Encoding: 8bit\r\n\r\n"
                + body.replace("\n", "\r\n");
    }
    private Properties load() { Properties p = new Properties(); File f = new File(CONFIG_FILE); if (f.exists()) try (FileInputStream fis = new FileInputStream(f)) { p.load(fis); } catch (IOException ignored) {} return p; }
    private boolean blank(String s) { return s == null || s.trim().isEmpty(); }
    private void cmd(BufferedWriter out, String s) throws IOException { out.write(s + "\r\n"); out.flush(); }
    private void expect(BufferedReader in, int code) throws IOException { String line = readResponse(in); if (line == null || !line.startsWith(String.valueOf(code))) throw new IOException("SMTP trả về không hợp lệ: " + line); }
    private void expectAny(BufferedReader in, int... codes) throws IOException { String line = readResponse(in); if (line == null) throw new IOException("Không nhận được phản hồi SMTP."); for (int code : codes) if (line.startsWith(String.valueOf(code))) return; throw new IOException("SMTP trả về không hợp lệ: " + line); }
    private String readResponse(BufferedReader in) throws IOException { String first = in.readLine(); if (first == null) return null; String line = first; while (line.length() >= 4 && line.charAt(3) == '-') { line = in.readLine(); if (line == null) break; } return first; }
}
