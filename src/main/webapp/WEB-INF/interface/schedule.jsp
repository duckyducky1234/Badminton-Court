<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activePage" value="schedule" />
<fmt:setLocale value="vi_VN" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sân | Badminton Booking</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/booking.css">
    <script defer src="${pageContext.request.contextPath}/assets/js/booking.js"></script>
</head>
<body>
    <%@ include file="/WEB-INF/interface/common/header.jspf" %>
    <main>
        <h1>Lịch sân cầu lông</h1>
        <%@ include file="/WEB-INF/interface/common/messages.jspf" %>

        <form class="inline-form" method="get" action="${pageContext.request.contextPath}/schedule">
            <label for="date">Chọn ngày</label>
            <input id="date" name="date" type="date" required value="${selectedDate}">
            <button type="submit">Xem lịch</button>
        </form>
        <p>Ngày đang xem <strong id="schedule-date">${selectedDate}</strong></p>

        <c:choose>
            <c:when test="${empty slots and empty requestScope.error}">
                <p>Chưa có khung giờ cho ngày này.</p>
            </c:when>
            <c:when test="${not empty slots}">
                <div class="table-scroll">
                    <table>
                        <caption>Khung giờ theo từng sân</caption>
                        <thead>
                            <tr>
                                <th scope="col">Sân</th>
                                <th scope="col">Khung giờ</th>
                                <th scope="col">Giá</th>
                                <th scope="col">Tình trạng</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${slots}" var="slot">
                                <tr>
                                    <th scope="row"><c:out value="${slot.court.name}" /></th>
                                    <td>${slot.startTime}–${slot.endTime}</td>
                                    <td><fmt:formatNumber value="${slot.court.hourlyRate}" /> ₫</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${slot.available}">
                                                <button type="button" class="slot-button" data-court-id="${slot.court.id}"
                                                        data-court="<c:out value='${slot.court.name}' />"
                                                        data-start="${slot.startTime}" data-end="${slot.endTime}"
                                                        data-amount="<fmt:formatNumber value='${slot.court.hourlyRate}' /> ₫">
                                                    Chọn giờ
                                                </button>
                                            </c:when>
                                            <c:otherwise>Không còn trống</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
        </c:choose>

        <%@ include file="/WEB-INF/interface/components/booking-form.jspf" %>
    </main>
    <%@ include file="/WEB-INF/interface/common/footer.jspf" %>
</body>
</html>
