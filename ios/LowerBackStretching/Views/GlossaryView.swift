import SwiftUI

struct GlossaryView: View {
    @EnvironmentObject private var content: ContentStore
    @State private var query: String = ""

    private var filtered: [GlossaryEntry] {
        if query.isEmpty { return content.glossary }
        return content.glossary.filter {
            $0.term.localizedCaseInsensitiveContains(query)
                || $0.definition.localizedCaseInsensitiveContains(query)
        }
    }

    private var groupedKeys: [String] {
        Array(Set(filtered.map(\.category))).sorted()
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 10) {
                if filtered.isEmpty {
                    Text("No matches.")
                        .font(.body)
                        .foregroundStyle(.secondary)
                        .padding(.top, 12)
                } else {
                    ForEach(groupedKeys, id: \.self) { category in
                        SectionHeader(category.capitalized)
                        ForEach(filtered.filter { $0.category == category }) { entry in
                            EntryCard(entry: entry)
                        }
                    }
                }
            }
            .padding(16)
        }
        .navigationTitle("Glossary")
        .navigationBarTitleDisplayMode(.inline)
        .searchable(text: $query, prompt: "Search terms")
    }
}

private struct EntryCard: View {
    let entry: GlossaryEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(entry.term).font(.headline)
            Text(entry.definition).font(.body)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 14))
    }
}
