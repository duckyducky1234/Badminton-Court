<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activePage" value="schedule" />
<c:set var="selectedDate"
       value="${empty param.date ? '2026-11-10' : param.date}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sân | Badminton Booking</title>
</head>

<body>
    <%@ include file="/WEB-INF/interface/common/header.jspf" %>

    <main>
        <h1>Lịch sân cầu lông</h1>

        <%-- Gửi ngày được chọn qua GET để mở lại lịch của ngày đó --%>
        <form method="get"
              action="${pageContext.request.contextPath}/schedule">
            <label for="date">Chọn ngày</label>
            <input id="date" name="date" type="date" required
                   value="<c:out value='${selectedDate}' />">
            <button type="submit">Xem lịch</button>
        </form>

        <p>
            Ngày đang xem
            <strong id="schedule-date"><c:out value="${selectedDate}" /></strong>
        </p>

        <table>
            <caption>Lịch sân mẫu</caption>

            <thead>
                <tr>
                    <th scope="col">Khung giờ</th>
                    <th scope="col">Sân 1</th>
                    <th scope="col">Sân 2</th>
                </tr>
            </thead>

            <tbody>
                <tr>
                    <th scope="row">17:00–18:00</th>
                    <td>
                        <button type="button" data-court="Sân 1"
                                data-time="17:00–18:00">Trống</button>
                    </td>
                    <td>Đã đặt</td>
                </tr>

                <tr>
                    <th scope="row">18:00–19:00</th>
                    <td>Đã đặt</td>
                    <td>
                        <button type="button" data-court="Sân 2"
                                data-time="18:00–19:00">Trống</button>
                    </td>
                </tr>

                <tr>
                    <th scope="row">19:00–20:00</th>
                    <td>
                        <button type="button" data-court="Sân 1"
                                data-time="19:00–20:00">Trống</button>
                    </td>
                    <td>
                        <button type="button" data-court="Sân 2"
                                data-time="19:00–20:00">Trống</button>
                    </td>
                </tr>
            </tbody>
        </table>

        <%@ include file="/WEB-INF/interface/components/booking-form.jspf" %>
    </main>

    <%@ include file="/WEB-INF/interface/common/footer.jspf" %>

    <script>
        const dialog = document.getElementById("booking-dialog");
        const scheduleDate = document.getElementById("schedule-date");
        const openSlotButtons = document.querySelectorAll("button[data-court]");

        openSlotButtons.forEach((button) => {
            button.addEventListener("click", () => {
                // Điền thông tin từ ô giờ vừa được chọn.
                document.getElementById("booking-date").textContent =
                    scheduleDate.textContent.trim();
                document.getElementById("booking-court").textContent =
                    button.dataset.court;
                document.getElementById("booking-time").textContent =
                    button.dataset.time;

                dialog.showModal();
            });
        });
    </script>
</body>
</html>