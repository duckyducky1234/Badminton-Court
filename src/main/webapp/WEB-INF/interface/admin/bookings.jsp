<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activePage" value="admin-bookings" />
<fmt:setLocale value="vi_VN" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý đặt sân | Badminton Booking</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
</head>
<body>
    <%@ include file="/WEB-INF/interface/common/header.jspf" %>
    <main>
        <h1>Quản lý đặt sân</h1>
        <%@ include file="/WEB-INF/interface/common/messages.jspf" %>
        <p>Đối chiếu nội dung và số tiền chuyển khoản trước khi xác nhận.</p>

        <c:choose>
            <c:when test="${empty bookings}"><p>Chưa có lượt đặt sân.</p></c:when>
            <c:otherwise>
                <div class="table-scroll">
                    <table>
                        <caption>Các lượt đặt sân</caption>
                        <thead>
                            <tr>
                                <th scope="col">Mã đặt</th>
                                <th scope="col">Tài khoản</th>
                                <th scope="col">Sân và khung giờ</th>
                                <th scope="col">Số tiền</th>
                                <th scope="col">Trạng thái</th>
                                <th scope="col">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${bookings}" var="booking">
                                <tr>
                                    <th scope="row">#${booking.id}</th>
                                    <td>#${booking.accountId}</td>
                                    <td>
                                        <c:out value="${booking.courtName}" /><br>
                                        ${booking.date} · ${booking.startTime}–${booking.endTime}
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${booking.amount}" /> ₫<br>
                                        <code><c:out value="${references[booking.id]}" /></code>
                                    </td>
                                    <td><c:out value="${booking.status.label}" /></td>
                                    <td>
                                        <c:if test="${booking.status.name() eq 'PENDING_CONFIRMATION'}">
                                            <form class="actions" method="post" action="${pageContext.request.contextPath}/admin/bookings">
                                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                <input type="hidden" name="id" value="${booking.id}">
                                                <button type="submit" name="action" value="confirm">Xác nhận</button>
                                                <button class="secondary" type="submit" name="action" value="reject">Từ chối</button>
                                            </form>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </main>
    <%@ include file="/WEB-INF/interface/common/footer.jspf" %>
</body>
</html>
