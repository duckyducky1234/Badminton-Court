package com.ducky.booking.model.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

final class QrImage {
    private static final int SIZE = 320;

    private QrImage() {}

    static byte[] png(String payload) {
        var output = new ByteArrayOutputStream();
        try {
            var matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, SIZE, SIZE);
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("Unable to render QR image", ex);
        }
    }
}
