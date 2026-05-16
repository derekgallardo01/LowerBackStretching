import SwiftUI

/// Big screen-level title used inside a content scroll view (not the
/// navigation bar title). Most iOS screens rely on `.navigationTitle`,
/// but a couple show their own large title inline.
struct ScreenHeader: View {
    let text: String
    init(_ text: String) { self.text = text }
    var body: some View {
        Text(text).font(.largeTitle.weight(.semibold))
    }
}

/// Section divider inside a screen ("Recent sessions", "My routines").
struct SectionHeader: View {
    let text: String
    init(_ text: String) { self.text = text }
    var body: some View {
        Text(text).font(.headline)
    }
}
