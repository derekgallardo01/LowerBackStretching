import SwiftUI

/// A big number with a small label underneath. Used on Home and Calendar.
struct Stat: View {
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 4) {
            Text(value).font(.title.weight(.semibold))
            Text(label)
                .font(.caption.weight(.medium))
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
    }
}
