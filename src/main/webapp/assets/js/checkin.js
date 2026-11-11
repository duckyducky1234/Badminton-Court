"use strict";

const scanTools = document.querySelector("#scan-tools");
const video = document.querySelector("#qr-camera");
const openCamera = document.querySelector("#start-camera");
const closeCamera = document.querySelector("#stop-camera");
const fileInput = document.querySelector("#qr-file");
const statusMessage = document.querySelector("#scan-status");
const tokenInput = document.querySelector("#checkin-token");
const canvas = document.createElement("canvas");
const context = canvas.getContext("2d", { willReadFrequently: true });
const ticketPattern = /^BADMINTON:[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}$/i;
let stream;
let scanTimer;

function stopCamera() {
    clearTimeout(scanTimer);
    if (stream) {
        stream.getTracks().forEach((track) => track.stop());
        stream = null;
    }
    video.srcObject = null;
    video.hidden = true;
    closeCamera.hidden = true;
    openCamera.disabled = false;
}

function decode(source, width, height) {
    const scale = Math.min(1, 900 / Math.max(width, height));
    canvas.width = Math.max(1, Math.round(width * scale));
    canvas.height = Math.max(1, Math.round(height * scale));
    context.drawImage(source, 0, 0, canvas.width, canvas.height);
    const pixels = context.getImageData(0, 0, canvas.width, canvas.height);
    return window.jsQR(pixels.data, pixels.width, pixels.height, { inversionAttempts: "dontInvert" });
}

function applyScan(code) {
    if (!code || !ticketPattern.test(code.data)) {
        return false;
    }
    tokenInput.value = code.data;
    statusMessage.textContent = "Đã đọc mã vé. Bấm Xác nhận check-in để tiếp tục.";
    stopCamera();
    tokenInput.focus();
    return true;
}

function scanFrame() {
    if (stream && video.videoWidth && video.videoHeight) {
        if (applyScan(decode(video, video.videoWidth, video.videoHeight))) {
            return;
        }
    }
    if (stream) {
        scanTimer = setTimeout(scanFrame, 160);
    }
}

if (context && typeof window.jsQR === "function") {
    scanTools.hidden = false;
    openCamera.hidden = !navigator.mediaDevices?.getUserMedia;
    fileInput.hidden = typeof createImageBitmap !== "function";
}

openCamera.addEventListener("click", async () => {
    openCamera.disabled = true;
    statusMessage.textContent = "Đang mở camera...";
    try {
        stream = await navigator.mediaDevices.getUserMedia({
            audio: false,
            video: { facingMode: { ideal: "environment" } }
        });
        video.srcObject = stream;
        video.hidden = false;
        closeCamera.hidden = false;
        await video.play();
        statusMessage.textContent = "Đưa vé QR vào trước camera.";
        scanFrame();
    } catch {
        stopCamera();
        statusMessage.textContent = "Không mở được camera. Bạn có thể chọn ảnh hoặc nhập mã vé.";
    }
});

closeCamera.addEventListener("click", () => {
    stopCamera();
    statusMessage.textContent = "Đã tắt camera.";
});

fileInput.addEventListener("change", async () => {
    const file = fileInput.files[0];
    if (!file) {
        return;
    }
    stopCamera();
    let image;
    try {
        image = await createImageBitmap(file);
        if (!applyScan(decode(image, image.width, image.height))) {
            statusMessage.textContent = "Không tìm thấy mã vé hợp lệ trong ảnh.";
        }
    } catch {
        statusMessage.textContent = "Không đọc được ảnh đã chọn.";
    } finally {
        image?.close();
        fileInput.value = "";
    }
});

window.addEventListener("pagehide", stopCamera);
