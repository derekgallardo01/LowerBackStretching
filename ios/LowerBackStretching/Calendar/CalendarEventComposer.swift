import EventKit
import EventKitUI
import SwiftUI
import UIKit

/// Presents the system "new event" sheet pre-filled with a stretching
/// break. Wraps `EKEventEditViewController` for SwiftUI.
///
/// On iOS 17+ we ask for write-only access (less intrusive than full
/// read/write); older versions fall back to full access.
struct CalendarEventComposer: UIViewControllerRepresentable {

    let title: String
    let minutesFromNow: Int
    let durationMinutes: Int
    let onDismiss: () -> Void

    private static let store = EKEventStore()

    func makeUIViewController(context: Context) -> UIViewController {
        let container = UIViewController()
        Task {
            let granted = await requestAccess()
            if granted, let edit = await buildEditController(context: context) {
                container.present(edit, animated: true)
            } else {
                await MainActor.run { onDismiss() }
            }
        }
        return container
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator(onDismiss: onDismiss) }

    final class Coordinator: NSObject, EKEventEditViewDelegate {
        let onDismiss: () -> Void
        init(onDismiss: @escaping () -> Void) { self.onDismiss = onDismiss }

        func eventEditViewController(
            _ controller: EKEventEditViewController,
            didCompleteWith action: EKEventEditViewAction
        ) {
            controller.dismiss(animated: true) { self.onDismiss() }
        }
    }

    @MainActor
    private func buildEditController(context: Context) -> EKEventEditViewController? {
        let edit = EKEventEditViewController()
        edit.eventStore = Self.store
        edit.editViewDelegate = context.coordinator
        let event = EKEvent(eventStore: Self.store)
        event.title = title
        event.notes = "Take a few minutes to stretch."
        event.startDate = Date().addingTimeInterval(TimeInterval(minutesFromNow * 60))
        event.endDate = event.startDate.addingTimeInterval(TimeInterval(durationMinutes * 60))
        edit.event = event
        return edit
    }

    /// Request the minimal permission iOS 17+ supports; fall back to
    /// full access on older versions.
    private func requestAccess() async -> Bool {
        if #available(iOS 17.0, *) {
            return await withCheckedContinuation { cont in
                Self.store.requestWriteOnlyAccessToEvents { granted, _ in
                    cont.resume(returning: granted)
                }
            }
        } else {
            return await withCheckedContinuation { cont in
                Self.store.requestAccess(to: .event) { granted, _ in
                    cont.resume(returning: granted)
                }
            }
        }
    }
}
