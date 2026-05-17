import CoreImage.CIFilterBuiltins
import SwiftUI
import UIKit

/// Render `text` as a square QR code `UIImage`. Uses Core Image's
/// built-in `CIQRCodeGenerator` filter — no third-party dependency.
///
/// Returns nil on encoder failure (shouldn't happen for reasonable
/// payloads).
func renderQrImage(text: String, sizePx: CGFloat) -> UIImage? {
    let filter = CIFilter.qrCodeGenerator()
    guard let data = text.data(using: .utf8) else { return nil }
    filter.setValue(data, forKey: "inputMessage")
    filter.setValue("M", forKey: "inputCorrectionLevel")
    guard let ciImage = filter.outputImage else { return nil }

    let scale = sizePx / ciImage.extent.width
    let scaled = ciImage.transformed(by: CGAffineTransform(scaleX: scale, y: scale))
    let context = CIContext()
    guard let cg = context.createCGImage(scaled, from: scaled.extent) else { return nil }
    return UIImage(cgImage: cg)
}

/// SwiftUI convenience wrapping the function above. Re-renders only
/// when `text` or `size` changes.
struct QrCodeView: View {
    let text: String
    let size: CGFloat

    var body: some View {
        if let image = renderQrImage(text: text, sizePx: size) {
            Image(uiImage: image)
                .interpolation(.none)
                .resizable()
                .frame(width: size, height: size)
        } else {
            Color.gray.frame(width: size, height: size)
        }
    }
}
