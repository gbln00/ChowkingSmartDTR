package com.chowking.smartdtr.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

public class QrUtils {

    /**
     * Encode a string (typically an employeeId) into a QR code Bitmap.
     *
     * @param content   The string to encode — use the employee's employeeId.
     * @param sizePx    Width and height of the output bitmap in pixels (e.g. 800).
     * @param darkColor Pixel color for dark modules (usually Color.BLACK).
     * @param lightColor Pixel color for light modules (usually Color.WHITE).
     * @return Bitmap containing the QR code, or null if encoding failed.
     */
    public static Bitmap generate(
            String content,
            int sizePx,
            int darkColor,
            int lightColor
    ) {
        if (content == null || content.isEmpty()) return null;

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1); // quiet zone (modules, not pixels)
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        try {
            BitMatrix bitMatrix = new QRCodeWriter()
                    .encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints);

            int[] pixels = new int[sizePx * sizePx];
            for (int y = 0; y < sizePx; y++) {
                for (int x = 0; x < sizePx; x++) {
                    pixels[y * sizePx + x] = bitMatrix.get(x, y) ? darkColor : lightColor;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx);
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Convenience overload — black on white QR code. */
    public static Bitmap generate(String content, int sizePx) {
        return generate(content, sizePx, Color.BLACK, Color.WHITE);
    }
}