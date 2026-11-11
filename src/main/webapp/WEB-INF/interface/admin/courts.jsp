<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activePage" value="admin-courts" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý sân | Badminton Booking</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
</head>
<body>
    <%@ include file="/WEB-INF/interface/common/header.jspf" %>
    <main>
        <h1>Quản lý sân</h1>
        <%@ include file="/WEB-INF/interface/common/messages.jspf" %>
        <p>Đặt giờ mở và đóng cửa theo giờ tròn, cùng ngày.</p>

        <div class="court-list">
            <section class="panel">
                <h2>Thêm sân</h2>
                <form class="field-list" method="post" action="${pageContext.request.contextPath}/admin/courts">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <label>Tên sân <input name="name" required maxlength="100"></label>
                    <label>Giá mỗi giờ (₫) <input name="hourlyRate" type="number" min="1" max="499999999" required></label>
                    <label>Mở cửa <input name="openTime" type="time" step="3600" value="06:00" required></label>
                    <label>Đóng cửa <input name="closeTime" type="time" step="3600" value="22:00" required></label>
                    <label class="checkbox-field"><input name="active" type="checkbox" value="true" checked> Cho phép đặt sân</label>
                    <button type="submit">Thêm sân</button>
                </form>
            </section>
            <c:forEach items="${courts}" var="court">
                <section class="panel">
                    <h2><c:out value="${court.name}" /></h2>
                    <form class="field-list" method="post" action="${pageContext.request.contextPath}/admin/courts">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="id" value="${court.id}">
                        <label>Tên sân
                            <input name="name" required maxlength="100" value="<c:out value='${court.name}' />">
                        </label>
                        <label>Giá mỗi giờ (₫)
                            <input name="hourlyRate" type="number" min="1" max="499999999" required value="${court.hourlyRate}">
                        </label>
                        <label>Mở cửa <input name="openTime" type="time" step="3600" value="${court.openTime}" required></label>
                        <label>Đóng cửa <input name="closeTime" type="time" step="3600" value="${court.closeTime}" required></label>
                        <label class="checkbox-field">
                            <input name="active" type="checkbox" value="true" ${court.active ? 'checked' : ''}>
                            Cho phép đặt sân
                        </label>
                        <button type="submit">Lưu thay đổi</button>
                    </form>
                </section>
            </c:forEach>
        </div>
    </main>
    <%@ include file="/WEB-INF/interface/common/footer.jspf" %>
</body>
</html>
