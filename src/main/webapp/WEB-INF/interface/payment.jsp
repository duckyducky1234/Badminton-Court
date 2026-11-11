<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh toán | Badminton Booking</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/booking.css">
    <script defer src="${pageContext.request.contextPath}/assets/js/booking.js"></script>
</head>
<body>
    <%@ include file="/WEB-INF/interface/common/header.jspf" %>
    <main>
        <section class="panel">
            <h1>Lượt đặt #${booking.id}</h1>
            <%@ include file="/WEB-INF/interface/common/messages.jspf" %>
            <dl>
                <dt>Sân</dt>
                <dd><c:out value="${booking.courtName}" /></dd>
                <dt>Khung giờ</dt>
                <dd>${booking.date} · ${booking.startTime}–${booking.endTime}</dd>
                <dt>Số tiền</dt>
                <dd><fmt:formatNumber value="${booking.amount}" /> ₫</dd>
                <dt>Trạng thái</dt>
                <dd><c:out value="${booking.status.label}" /></dd>
            </dl>
            <%@ include file="/WEB-INF/interface/components/payment-panel.jspf" %>
            <p><a href="${pageContext.request.contextPath}/booking/history">Về lịch sử đặt vé</a></p>
        </section>
    </main>
    <%@ include file="/WEB-INF/interface/common/footer.jspf" %>
</body>
</html>
