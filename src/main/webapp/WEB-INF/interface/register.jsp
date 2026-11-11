<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activePage" value="register" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký | Badminton Booking</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
    <%@ include file="/WEB-INF/interface/common/header.jspf" %>
    <main>
        <section class="panel narrow">
            <h1>Đăng ký</h1>
            <%@ include file="/WEB-INF/interface/common/messages.jspf" %>
            <form class="field-list" method="post" action="${pageContext.request.contextPath}/auth/register">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <label>Họ tên
                    <input name="name" maxlength="100" autocomplete="name" required value="<c:out value='${name}' />">
                </label>
                <label>Email
                    <input name="email" type="email" maxlength="254" autocomplete="email" required
                           value="<c:out value='${email}' />">
                </label>
                <label>Mật khẩu (8–128 ký tự)
                    <input name="password" type="password" minlength="8" maxlength="128" autocomplete="new-password" required>
                </label>
                <button type="submit">Tạo tài khoản</button>
            </form>
            <p>Đã có tài khoản? <a href="${pageContext.request.contextPath}/auth/login">Đăng nhập</a></p>
        </section>
    </main>
    <%@ include file="/WEB-INF/interface/common/footer.jspf" %>
</body>
</html>
