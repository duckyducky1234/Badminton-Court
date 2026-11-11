# Đặt sân cầu lông

Ứng dụng Java 21 dùng Jakarta Servlet, JSP/JSTL và PostgreSQL. Khách xem lịch và đặt sân theo từng khung một giờ. Hệ thống giữ chỗ trong 5 phút để khách chuyển khoản và gửi thông báo. Sau khi đối chiếu giao dịch, quản trị viên xác nhận để khách nhận vé QR. Quản trị viên cũng có thể chỉnh giờ mở cửa, giá và trạng thái của từng sân.

## Cách tổ chức mã

- **View** gồm các JSP trong `WEB-INF/interface`, nhận dữ liệu từ controller để hiển thị.
- **Controller** gồm các servlet đọc biểu mẫu, gọi service và chuyển kết quả sang JSP. Filter kiểm tra phiên đăng nhập, vai trò và mã CSRF.
- **Model** đóng gói dữ liệu trong `Account`, `Court` và `Booking`. `User` và `Admin` kế thừa `Account` và triển khai `getRole()` theo vai trò. `CourtHours` quản lý quy tắc giờ hoạt động.
- **Service và DAO** tách nghiệp vụ khỏi JDBC. `BookingService` quản lý chuyển trạng thái, các DAO chạy SQL tham số hóa qua `DatabaseConnection`. PostgreSQL dùng chỉ mục duy nhất có điều kiện để bảo vệ khung giờ ngay cả khi hai yêu cầu đến đồng thời.

## Cài đặt trên máy khác

Cần Java 21, Maven, Tomcat 10.1 và PostgreSQL. Các lệnh sau dùng PowerShell.

1. Tạo một database riêng và tài khoản PostgreSQL cho ứng dụng. Chạy [database/schema.sql](database/schema.sql) bằng tài khoản có quyền tạo bảng trong database đó. Schema thêm hai sân mẫu.

   ```powershell
   $env:PGCLIENTENCODING = 'UTF8'
   psql -h 127.0.0.1 -p 5432 -U badminton_app -d badminton_booking -W -v ON_ERROR_STOP=1 -f database/schema.sql
   ```

2. Cấu hình kết nối và build.

   ```powershell
   $env:DB_URL = 'jdbc:postgresql://127.0.0.1:5432/badminton_booking'
   $env:DB_USER = 'badminton_app'
   $env:DB_PASSWORD = [Net.NetworkCredential]::new('', (Read-Host 'Mật khẩu PostgreSQL' -AsSecureString)).Password
   mvn clean verify
   ```

3. Tạo tài khoản quản trị một lần trong phiên PowerShell đã đặt các biến DB.

   ```powershell
   $env:BOOKING_ADMIN_NAME = 'Quản trị sân'
   $env:BOOKING_ADMIN_EMAIL = Read-Host 'Email quản trị'
   $env:BOOKING_ADMIN_PASSWORD = [Net.NetworkCredential]::new('', (Read-Host 'Mật khẩu quản trị' -AsSecureString)).Password
   java -cp 'target/classes;target/badminton-booking/WEB-INF/lib/*' com.ducky.booking.setup.BootstrapAdmin
   Remove-Item Env:BOOKING_ADMIN_PASSWORD
   ```

4. Để bật thanh toán, đặt `BOOKING_PAYMENT_BANK_BIN` (BIN VietQR 6 chữ số), `BOOKING_PAYMENT_ACCOUNT_NUMBER` và `BOOKING_PAYMENT_ACCOUNT_NAME` trong môi trường của Tomcat. Quản trị viên đối chiếu chuyển khoản và xác nhận bằng tay. Nếu chưa có thông tin tài khoản nhận, trang thanh toán sẽ báo chưa được cấu hình.

5. Từ phiên đã đặt các biến môi trường, chép WAR vào `webapps` của Tomcat. Ví dụ với Tomcat dùng cấu hình mặc định và đã đặt `CATALINA_HOME`:

   ```powershell
   Copy-Item target/badminton-booking.war "$env:CATALINA_HOME/webapps/"
   & "$env:CATALINA_HOME/bin/catalina.bat" run
   ```

   Mở [Badminton Booking](http://127.0.0.1:8080/badminton-booking/). Nếu Tomcat đã chạy trước khi đặt biến, khởi động lại để nhận cấu hình mới.

## Kiểm thử

`mvn verify` chạy kiểm thử Java và đóng gói `target/badminton-booking.war`. Để chạy cả kiểm thử tích hợp, nạp schema vào database thử riêng, đặt `BOOKING_TEST_DB_URL`, `BOOKING_TEST_DB_USER` và `BOOKING_TEST_DB_PASSWORD` rồi chạy lại lệnh. Các bài kiểm thử kiểm tra luồng giữ chỗ, xác nhận, check-in và tranh chấp cùng khung giờ.

## Cấu hình cục bộ trên máy đã triển khai

Cụm PostgreSQL riêng chạy trên `127.0.0.1:55432` và Tomcat chạy trên `127.0.0.1:8080`. Windows lưu dữ liệu và cấu hình được tạo từ Codex trong `LocalCache` của ứng dụng. Khi chạy từ Windows PowerShell hoặc VS Code, dùng đường dẫn thực tế dưới đây.

Sau khi khởi động lại máy, chạy:

```powershell
$bookingLocal = Join-Path $env:LOCALAPPDATA 'Packages\OpenAI.Codex_2p2nqsd0c76g0\LocalCache\Local\BadmintonBooking'
& 'C:/Program Files/PostgreSQL/18/bin/pg_ctl.exe' -D "$bookingLocal/postgres" -l "$bookingLocal/postgres.log" -o '-h 127.0.0.1 -p 55432' -w start
$env:CATALINA_BASE = Join-Path $bookingLocal 'tomcat'
& "$env:CATALINA_HOME/bin/catalina.bat" run
```

Để triển khai lại sau khi sửa code, mở PowerShell trong thư mục dự án:

```powershell
mvn clean package
$bookingLocal = Join-Path $env:LOCALAPPDATA 'Packages\OpenAI.Codex_2p2nqsd0c76g0\LocalCache\Local\BadmintonBooking'
Copy-Item -LiteralPath '.\target\badminton-booking.war' -Destination "$bookingLocal\tomcat\webapps\badminton-booking.war" -Force
```

Cấu hình riêng của Tomcat nằm ở `tomcat\bin\setenv.bat` trong thư mục trên. Tài khoản quản trị cục bộ được lưu trong `admin-login.txt` cùng thư mục. Hãy giữ tệp này riêng tư.
