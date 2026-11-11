<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activePage" value="login" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập | Badminton Booking</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
    <%@ include file="/WEB-INF/interface/common/header.jspf" %>
    <main>
        <section class="panel narrow">
            <h1>Đăng nhập</h1>
            <%@ include file="/WEB-INF/interface/common/messages.jspf" %>
            <form class="field-list" method="post" action="${pageContext.request.contextPath}/auth/login">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <label>Email
                    <input name="email" type="email" maxlength="254" autocomplete="username" required
                           value="<c:out value='${email}' />">
                </label>
                <label>Mật khẩu
                    <input name="password" type="password" maxlength="128" autocomplete="current-password" required>
                </label>
                <button type="submit">Đăng nhập</button>
            </form>
            <p>Chưa có tài khoản? <a href="${pageContext.request.contextPath}/auth/register">Đăng ký</a></p>
        </section>
    </main>
    <%@ include file="/WEB-INF/interface/common/footer.jspf" %>
</body>
</html>
