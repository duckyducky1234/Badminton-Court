<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activePage" value="admin-checkin" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Check-in | Badminton Booking</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
    <script defer src="${pageContext.request.contextPath}/assets/vendor/jsQR.js"></script>
    <script defer src="${pageContext.request.contextPath}/assets/js/checkin.js"></script>
</head>
<body>
    <%@ include file="/WEB-INF/interface/common/header.jspf" %>
    <main>
        <section class="panel narrow">
            <h1>Check-in vé sân</h1>
            <%@ include file="/WEB-INF/interface/common/messages.jspf" %>
            <p>Quét QR, chọn ảnh chứa QR hoặc nhập mã vé được hiển thị trong lịch sử đặt sân.</p>

            <div id="scan-tools" class="field-list" hidden>
                <div class="actions">
                    <button id="start-camera" class="secondary" type="button">Mở camera</button>
                    <button id="stop-camera" class="secondary" type="button" hidden>Tắt camera</button>
                </div>
                <video id="qr-camera" muted playsinline hidden aria-label="Camera quét mã QR"></video>
                <label>Ảnh vé QR <input id="qr-file" type="file" accept="image/*"></label>
                <p id="scan-status" role="status"></p>
            </div>

            <form class="field-list" method="post" action="${pageContext.request.contextPath}/admin/checkin">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                <label>Mã vé
                    <input id="checkin-token" name="token" maxlength="46" autocomplete="off" spellcheck="false" required>
                </label>
                <button type="submit">Xác nhận check-in</button>
            </form>
        </section>
    </main>
    <%@ include file="/WEB-INF/interface/common/footer.jspf" %>
</body>
</html>
