"use strict";

const bookingDialog = document.querySelector("#booking-dialog");
if (bookingDialog) {
    const scheduleDate = document.querySelector("#schedule-date").textContent.trim();
    const bookingForm = document.querySelector("#booking-form");

    for (const button of document.querySelectorAll(".slot-button")) {
        button.addEventListener("click", () => {
            const slot = button.dataset;
            document.querySelector("#booking-date").textContent = scheduleDate;
            document.querySelector("#booking-court").textContent = slot.court;
            document.querySelector("#booking-time").textContent = slot.start + "–" + slot.end;
            document.querySelector("#booking-amount").textContent = slot.amount;

            if (bookingForm) {
                bookingForm.elements.courtId.value = slot.courtId;
                bookingForm.elements.start.value = slot.start;
            }
            bookingDialog.showModal();
        });
    }
}

const expiry = document.querySelector("#payment-expiry");
if (expiry) {
    const deadline = Date.parse(expiry.dateTime);
    const countdown = document.querySelector("#payment-countdown");
    const report = document.querySelector("#report-payment");
    const instructions = document.querySelector("#payment-instructions");

    function showRemainingTime() {
        const seconds = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
        if (seconds === 0) {
            countdown.textContent = "Đã hết hạn giữ chỗ.";
            if (report) {
                report.disabled = true;
            }
            if (instructions) {
                instructions.hidden = true;
            }
            return;
        }

        const minutes = Math.floor(seconds / 60);
        const remainder = String(seconds % 60).padStart(2, "0");
        countdown.textContent = "Còn " + minutes + ":" + remainder;
        setTimeout(showRemainingTime, 1000);
    }

    if (Number.isFinite(deadline)) {
        showRemainingTime();
    }
}
