# NestHome / DormAssist - Bản sửa UI/UX, thứ tự danh sách và thanh cuộn

## 1. Môi trường

- JDK 11 trở lên.
- SQL Server / SQL Server Express.
- Maven 3.8+ nếu muốn build bằng `mvn clean package`.
- Gmail hệ thống nếu dùng chức năng quên mật khẩu qua email OTP.

Project vẫn giữ package chính `com.dormassist` để tránh lỗi dây chuyền, nhưng phần hiển thị giao diện đã đồng bộ theo thương hiệu **NestHome - Ký túc xá - Quản lý • Chăm sóc • Kết nối**.

## 2. Database thống nhất

App đang dùng database:

```properties
db.name=dormassist
```

File cập nhật database cũ nằm ở:

```text
update_database.sql
```

Script này an toàn khi chạy lại nhiều lần:

- Không `DROP DATABASE`.
- Không xóa dữ liệu cũ.
- Có `IF OBJECT_ID`, `IF COL_LENGTH`, `IF NOT EXISTS`.
- Tự tạo database `dormassist` nếu chưa có.
- Sửa lỗi cũ `cấpacity` thành `capacity` nếu database đang bị sai tên cột.
- Bổ sung bảng/cột/khóa ngoại cho các DAO hiện tại.
- Text tiếng Việt dùng `NVARCHAR` ở các cột nội dung, tên, ghi chú.
- Tiền dùng `DECIMAL(18,2)` trong SQL.
- Token có unique index để tránh trùng mã.

## 3. Cách chạy SQL

Mở SQL Server Management Studio, mở file:

```text
update_database.sql
```

Sau đó chạy toàn bộ script. Database thống nhất là:

```sql
USE dormassist;
```

## 4. Cấu hình SQL Server

Sửa file `db.properties` ở thư mục gốc project:

```properties
db.host=localhost
db.port=1433
db.name=dormassist
db.user=sa
db.password=your_sql_server_password
```

Không đưa file `db.properties` có mật khẩu thật lên GitHub. Có thể dùng `db.properties.example` làm mẫu.

## 5. Cấu hình Gmail SMTP cho quên mật khẩu

Trong `db.properties`:

```properties
smtp.host=smtp.gmail.com
smtp.port=465
smtp.username=your_gmail@gmail.com
smtp.password=your_gmail_app_password
smtp.from=your_gmail@gmail.com
```

Ghi chú:

- `smtp.username` là Gmail hệ thống dùng để gửi OTP.
- `smtp.password` là **Gmail App Password 16 ký tự**, không phải mật khẩu Gmail thường.
- Không hard-code mật khẩu Gmail trong source.

## 6. Tài khoản và token mẫu

Sau khi chạy `update_database.sql`, nếu chưa có tài khoản admin thì script tạo:

```text
Tên đăng nhập: admin
Mật khẩu: admin123
Vai trò: Quản trị viên
```

Token mẫu dễ nhập:

```text
SVVKU-0001      -> Sinh viên
TTVKU-0001     -> Trưởng tầng
TPVKU-0001     -> Trưởng phòng
ADMINVKU-0001  -> Quản trị viên
```

Token tạo mới từ màn hình **Mã Token** sẽ tăng tuần tự theo vai trò:

```text
SVVKU-0002, SVVKU-0003, ...
TTVKU-0002, TTVKU-0003, ...
TPVKU-0002, TPVKU-0003, ...
ADMINVKU-0002, ADMINVKU-0003, ...
```

## 7. Các phần đã sửa trong bản này

### 7.1. Mục Cá nhân / Cài đặt

Đã làm lại màn hình **Thông tin cá nhân** theo ảnh mẫu:

- Tiêu đề có icon tròn, tiêu đề và mô tả rõ ràng.
- Card **Thông tin hồ sơ** có avatar lớn, nút cập nhật ảnh, họ tên, vai trò, email, số điện thoại, phòng ở, mã sinh viên và badge trạng thái.
- Card **Tổng quan tài khoản** có vai trò, ngày tham gia, trạng thái tài khoản, lần đăng nhập gần nhất và mã token cần nhập mật khẩu để xem.
- Card **Cập nhật thông tin** có họ tên, email, số điện thoại, địa chỉ, ghi chú và nút lưu thay đổi.
- Card **Đổi mật khẩu** kiểm tra mật khẩu cũ, mật khẩu mới và xác nhận mật khẩu mới.
- Form dùng layout co giãn, không dùng absolute positioning cứng cho toàn màn hình.

### 7.2. Màn hình Mã Token

Đã sửa lại màn hình **Mã Token**:

- Tiêu đề: `Mã Token`.
- Mô tả: `Cấp mã token cho người dùng đăng ký theo vai trò`.
- Có ô tìm kiếm nhanh.
- Có bộ lọc vai trò.
- Có nút `Làm mới`, dropdown `Tạo token cho`, `+ Tạo token`, `Sao chép mã`, `Xóa token`.
- Bảng gồm đủ cột:
  - ID
  - Mã token
  - Vai trò
  - Trạng thái
  - Ngày tạo
  - Ngày sử dụng
  - Người sử dụng
- Độ rộng cột đã chỉnh để không còn bị cắt kiểu `Còn hiệ...` hoặc `30/05/...`.
- Token còn hiệu lực lên trước, sau đó theo vai trò và ngày tạo mới nhất.
- Sao chép token dùng clipboard và thông báo nhẹ dạng toast.
- Xóa token có xác nhận, chỉ xóa token còn hiệu lực.

### 7.3. Sidebar, header và icon

- Sidebar dùng nền sáng, trắng kem/xanh nhạt.
- Item đang chọn có nền xanh sage nhạt và chữ xanh đậm.
- Đã thay các emoji/icon dễ lỗi font bằng icon vẽ Java2D, nên không còn hiện ô vuông/ký tự lạ ở sidebar.
- Header có icon menu, icon thông báo, avatar chữ cái đầu, tên người dùng và vai trò.
- Sidebar có minh họa ký túc xá, version và copyright phía dưới.

### 7.4. Thanh cuộn toàn app

Đã chuẩn hóa qua `UIHelper.smoothScroll(...)`:

```java
verticalScrollBar.setUnitIncrement(20);
verticalScrollBar.setBlockIncrement(120);
horizontalScrollBar.setUnitIncrement(20);
horizontalScrollBar.setBlockIncrement(120);
viewport.setScrollMode(JViewport.BLIT_SCROLL_MODE);
```

Ngoài ra:

- Scrollbar dùng UI bo góc màu sage/xám nhẹ.
- Các `JTable` và form dài dùng cùng cấu hình cuộn.
- Các bảng lớn load qua `UIHelper.runAsync(...)` / `SwingWorker` ở các màn hình chính để tránh query DB trực tiếp trên EDT.
- Không tạo lại toàn bộ app khi chỉ refresh bảng.

### 7.5. Bảng dữ liệu và độ rộng cột

Đã chuẩn hóa qua `UIHelper.configureColumns(...)`:

- Tất cả `JTable` tạo bằng `UIHelper.styledTable(...)` được bật `setAutoCreateRowSorter(true)`.
- Độ rộng cột được set theo tên cột: ID, Mã token, Trạng thái, Ngày tạo, Ngày sử dụng, Tiêu đề, Nội dung tóm tắt, Người đăng, Thời gian, Họ tên, Phòng, Tổng tiền...
- Header bảng không cho kéo đổi thứ tự cột lung tung.
- Bảng có row height lớn hơn, dễ đọc hơn.

## 8. Thứ tự danh sách đã sửa ở DAO

Không chỉ sửa riêng màn hình Token. Các DAO có `ORDER BY` rõ ràng hơn:

- **Tổng quan**: thông báo quan trọng/mới nhất trước, sự cố ưu tiên cao và mới nhất trước.
- **Quản lý phòng**: ưu tiên trạng thái hoạt động/trống, sau đó tòa nhà, tầng, số phòng.
- **Quản lý sinh viên**: sinh viên đang hoạt động trước, sau đó phòng, họ tên A-Z; sinh viên chưa có phòng xuống sau.
- **Hóa đơn & Thanh toán**: quá hạn lên đầu, chưa thanh toán tiếp theo, đã thanh toán phía dưới; trong nhóm sắp theo hạn thanh toán gần nhất.
- **Sự cố & Bảo trì**: khẩn cấp/cao trước, sau đó chưa xử lý, đang xử lý, hoàn tất; trong nhóm sắp mới nhất trước.
- **Tài sản**: sắp theo phòng/khu vực, loại tài sản, tên tài sản và tình trạng.
- **Thông báo**: thông báo quan trọng đứng trước, sau đó mới nhất.
- **Điểm rèn luyện**: sắp theo phòng, điểm thấp/cần nhắc nhở trước, họ tên và thời gian.
- **Chuyển phòng**: chờ duyệt trước, đã duyệt, từ chối; trong nhóm mới nhất trước.
- **Mã Token**: còn hiệu lực trước, đã sử dụng sau; trong nhóm sắp theo vai trò và ngày tạo mới nhất.
- **Tài khoản / Phân quyền**: quản trị viên, trưởng phòng, trưởng tầng, sinh viên; tài khoản đang hoạt động trước, sau đó họ tên/tên đăng nhập.
- **Khách thăm**: đang trong ký túc xá/chờ duyệt trước, lịch mới hơn trước, khách đã rời đi để sau.

Sau khi tìm kiếm, lọc, thêm, sửa, xóa hoặc làm mới, màn hình load lại từ DAO nên vẫn giữ thứ tự mặc định hợp lý.

## 9. Các lỗi/chức năng đã giữ và kiểm tra

- Giữ Java Swing, không chuyển JavaFX/web/framework khác.
- Giữ SQL Server.
- Không đổi tên package chính.
- Không xóa chức năng cũ.
- Không hard-code Gmail thật trong source.
- Sửa lỗi SQL Server còn sót: `FIELD(...)` trong `IncidentDAO` đã đổi sang `CASE WHEN` từ bản trước.
- RoomDAO dùng đúng cột `capacity`, không dùng tên cột có dấu.
- Database config và SQL script thống nhất `dormassist`.
- Vai trò hiển thị tiếng Việt: Sinh viên, Trưởng tầng, Trưởng phòng, Quản trị viên.
- Loại phòng: Tiêu chuẩn, Cao cấp, Đặc biệt.
- Quên mật khẩu dùng OTP email, OTP hết hạn và dùng một lần theo bảng `password_reset_codes`.
- Đổi mật khẩu trong app yêu cầu mật khẩu cũ.
- Mật khẩu người dùng được hash trước khi lưu.

## 10. Build và chạy

Nếu máy có Maven:

```bash
mvn clean package
```

Sau đó chạy JAR trong `target` hoặc chạy class `com.dormassist.Main` từ IDE.

Trong môi trường chỉnh sửa hiện tại không có Maven, nên đã kiểm tra bằng JDK trực tiếp:

```bash
find src/main/java -name "*.java" > sources.txt
javac -encoding UTF-8 -d target/classes @sources.txt
```

Kết quả: source Java compile thành công bằng JDK với encoding UTF-8.

## 11. Giới hạn

Màn hình **Cá nhân / Cài đặt** đã làm sát bố cục ảnh mẫu trong giới hạn Java Swing. Một số chi tiết như avatar minh họa thật, animation, blur vật lý hoặc shadow phức tạp không thể giống 100% app web/JavaFX nếu không đổi nền tảng. Bản này ưu tiên: giao diện đồng bộ NestHome, danh sách có thứ tự, thanh cuộn mượt hơn, compile được, không phá logic và không làm hỏng database cũ.
