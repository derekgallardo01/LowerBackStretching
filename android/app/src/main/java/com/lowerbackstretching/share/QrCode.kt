package com.lowerbackstretching.share

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Render [text] as a square QR code [Bitmap] of [sizePx] pixels per side.
 * Uses ZXing's pure-Kotlin core (no Android-specific dependency) and
 * converts the BitMatrix to ARGB pixels manually.
 *
 * Returns null on encoder failure (shouldn't happen for reasonable
 * payloads).
 */
fun renderQrBitmap(text: String, sizePx: Int): Bitmap? {
    val hints = mapOf(
        EncodeHintType.MARGIN to 1,
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
    )
    val matrix = try {
        QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    } catch (t: Throwable) {
        return null
    }
    val w = matrix.width
    val h = matrix.height
    val pixels = IntArray(w * h)
    for (y in 0 until h) {
        val row = y * w
        for (x in 0 until w) {
            pixels[row + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
        }
    }
    return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
}
