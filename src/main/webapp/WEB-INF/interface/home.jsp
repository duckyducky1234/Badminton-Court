<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activePage" value="home" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    

    <title>Trang chủ | Badminton Booking</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>

<body>

    <%@ include file="/WEB-INF/interface/common/header.jspf" %>

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

    <%@ include file="/WEB-INF/interface/common/footer.jspf" %>

</body>
</html>
