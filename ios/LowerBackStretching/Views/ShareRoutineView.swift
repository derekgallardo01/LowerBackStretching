import SwiftUI
import UIKit

struct ShareRoutineView: View {
    let routine: CustomRoutine

    private var link: String {
        buildRoutineLink(name: routine.name, stretchIds: routine.stretchIds)
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Text(routine.name).font(.title2.weight(.semibold))
                Text("\(routine.stretchIds.count) stretches")
                    .foregroundStyle(.secondary)

                QrCodeView(text: link, size: 240)
                    .padding(12)
                    .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 16))

                Text(link)
                    .font(.caption.monospaced())
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(12)
                    .background(Color.secondary.opacity(0.12), in: RoundedRectangle(cornerRadius: 12))
                    .textSelection(.enabled)

                Button {
                    UIPasteboard.general.string = link
                } label: {
                    Label("Copy link", systemImage: "doc.on.doc")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)

                ShareLink(item: link, subject: Text(routine.name)) {
                    Label("Share…", systemImage: "square.and.arrow.up")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
            }
            .padding(16)
        }
        .navigationTitle("Share routine")
        .navigationBarTitleDisplayMode(.inline)
    }
}
