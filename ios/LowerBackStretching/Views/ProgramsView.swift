import SwiftUI

struct ProgramsView: View {
    @EnvironmentObject private var content: ContentStore
    @State private var selectedCategory: String = "all"

    private var categories: [String] {
        ["all"] + Array(Set(content.programs.map(\.category))).sorted()
    }
    private var visible: [Program] {
        selectedCategory == "all" ? content.programs : content.programs.filter { $0.category == selectedCategory }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(categories, id: \.self) { c in
                        Button { selectedCategory = c } label: {
                            Text(c.replacingOccurrences(of: "-", with: " "))
                                .padding(.horizontal, 12).padding(.vertical, 6)
                                .background(
                                    Capsule().fill(
                                        selectedCategory == c
                                        ? Color.accentColor.opacity(0.2)
                                        : Color(.secondarySystemBackground)
                                    )
                                )
                                .foregroundStyle(selectedCategory == c ? Color.accentColor : Color.primary)
                        }
                    }
                }
                .padding(.horizontal, 16)
            }

            ScrollView {
                VStack(spacing: 12) {
                    ForEach(visible) { program in
                        NavigationLink(value: program) {
                            ProgramCardView(program: program)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 16)
            }
        }
        .navigationTitle("Programs")
        .navigationDestination(for: Program.self) { p in
            ProgramDetailView(program: p)
        }
    }
}
