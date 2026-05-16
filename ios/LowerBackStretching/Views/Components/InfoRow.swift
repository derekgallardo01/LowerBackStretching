import SwiftUI

/// A standard list-card with a title, a subtitle (typically meta info like
/// `"5 stretches · 3 min"`), an optional body paragraph, and an optional
/// trailing icon. Used by Programs, Stretches, Routines, Sessions, Days.
struct InfoRow<Trailing: View>: View {
    let title: String
    let subtitle: String
    var body: String?
    var trailing: Trailing

    init(
        title: String,
        subtitle: String,
        body: String? = nil,
        @ViewBuilder trailing: () -> Trailing = { EmptyView() },
    ) {
        self.title = title
        self.subtitle = subtitle
        self.body = body
        self.trailing = trailing()
    }

    var body: some View {
        HStack(alignment: .center) {
            VStack(alignment: .leading, spacing: 4) {
                Text(title).font(.headline)
                Text(subtitle)
                    .font(.caption.weight(.medium))
                    .foregroundStyle(.tint)
                if let body {
                    Text(body)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .padding(.top, 2)
                }
            }
            Spacer()
            trailing
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(RoundedRectangle(cornerRadius: 14).fill(Color(.secondarySystemBackground)))
    }
}
