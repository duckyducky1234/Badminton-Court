<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">

    <title>Trang chủ | Badminton Booking</title>
</head>

<body>

    <header>
        <%-- contextPath lấy đường dẫn gốc của ứng dụng --%>
        <nav aria-label="Điều hướng chính">

            <a href="${pageContext.request.contextPath}/home"
               aria-current="page">
                Trang chủ
            </a>

            <a href="${pageContext.request.contextPath}/schedule">
                Lịch sân
            </a>

            <a href="${pageContext.request.contextPath}/booking/history">
                Lịch sử đặt vé
            </a>

            <a href="${pageContext.request.contextPath}/auth/login">
                Đăng nhập
            </a>

        </nav>
    </header>

    <main>

        <section class="booking">
            <h1>Đặt sân cầu lông</h1>

            <p>
                Xem lịch sân, chọn khung giờ phù hợp
                và theo dõi lượt đặt của bạn.
            </p>

            <a href="${pageContext.request.contextPath}/schedule">
                Xem lịch sân
            </a>
        </section>

        <section class="booking-guide">
            <h2>Cách đặt sân</h2>

            <ol>
                <li>
                    Đăng nhập, mở lịch sân và chọn khung giờ còn trống.
                </li>

                <li>
                    Kiểm tra thông tin, xác nhận đặt sân và chuyển khoản
                    theo mã QR trong thời gian giữ chỗ 5 phút.
                </li>

                <li>
                    Báo đã chuyển khoản và chờ quản lý xác nhận.
                    Vé QR sẽ xuất hiện trong lịch sử đặt vé khi được duyệt.
                </li>
            </ol>
        </section>

    </main>

    <footer>
        <p>Badminton Booking</p>
    </footer>

</body>
</html>