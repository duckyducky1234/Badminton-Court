<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lỗi | Badminton Booking</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
    <main>
        <section class="panel">
            <h1>Không thể hoàn tất yêu cầu</h1>
            <c:choose>
                <c:when test="${requestScope['jakarta.servlet.error.status_code'] eq 403}">
                    <p>Bạn không có quyền truy cập hoặc biểu mẫu đã hết hiệu lực.</p>
                </c:when>
                <c:when test="${requestScope['jakarta.servlet.error.status_code'] eq 404}">
                    <p>Không tìm thấy trang hoặc mã QR được yêu cầu.</p>
                </c:when>
                <c:otherwise>
                    <p>Vui lòng tải lại trang hoặc quay lại sau.</p>
                </c:otherwise>
            </c:choose>
            <a href="${pageContext.request.contextPath}/home">Về trang chủ</a>
        </section>
    </main>
</body>
</html>
