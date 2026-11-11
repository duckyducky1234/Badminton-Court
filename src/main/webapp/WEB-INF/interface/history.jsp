<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activePage" value="history" />
<fmt:setLocale value="vi_VN" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sử đặt vé | Badminton Booking</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/booking.css">
</head>
<body>
    <%@ include file="/WEB-INF/interface/common/header.jspf" %>
    <main>
        <h1>Lịch sử đặt vé</h1>
        <%@ include file="/WEB-INF/interface/common/messages.jspf" %>
        <c:if test="${empty bookings}">
            <p>Bạn chưa có lượt đặt. <a href="${pageContext.request.contextPath}/schedule">Xem lịch sân</a></p>
        </c:if>
        <div class="booking-list">
            <c:forEach items="${bookings}" var="booking">
                <section class="panel">
                    <h2>#${booking.id} · <c:out value="${booking.courtName}" /></h2>
                    <p>${booking.date} · ${booking.startTime}–${booking.endTime}</p>
                    <p><fmt:formatNumber value="${booking.amount}" /> ₫ · <c:out value="${booking.status.label}" /></p>
                    <c:if test="${booking.status.name() eq 'HELD'}">
                        <a href="${pageContext.request.contextPath}/booking/payment?id=${booking.id}">Thanh toán hoặc hủy giữ chỗ</a>
                    </c:if>
                    <%@ include file="/WEB-INF/interface/components/ticket-panel.jspf" %>
                </section>
            </c:forEach>
        </div>
    </main>
    <%@ include file="/WEB-INF/interface/common/footer.jspf" %>
</body>
</html>
