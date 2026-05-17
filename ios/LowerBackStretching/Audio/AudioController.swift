import Foundation
import AVFoundation

/// Wraps platform audio APIs for the three streams the app cares about:
///
/// - **Music loop** — long, low-volume background music. One `AVAudioPlayer`.
/// - **Ambient loop** — long nature soundscape. One `AVAudioPlayer`.
/// - **Chimes** — short one-shot bell/ding. A pool of `AVAudioPlayer`s
///   keyed by resource name.
///
/// Calls are idempotent: setting a track that's already playing only
/// updates volume; setting a different track stops the previous and
/// starts the new one. Missing bundle resources fail silently so the
/// app still works in dev when the MP3s haven't been added.
///
/// Music ducking: when a chime fires we temporarily set the shared
/// `AVAudioSession` category options to `.duckOthers`. Other apps that
/// respect audio focus (Spotify, Apple Music) lower their volume for
/// the duration.
final class AudioController {

    static let shared = AudioController()
    private init() {}

    private var musicPlayer: AVAudioPlayer?
    private var ambientPlayer: AVAudioPlayer?
    private var chimePlayers: [String: AVAudioPlayer] = [:]

    private var currentMusic: MusicTrack = .none
    private var currentAmbient: AmbientTrack = .none

    // MARK: Music

    func setMusic(_ track: MusicTrack, volume: Float) {
        if track == currentMusic {
            musicPlayer?.volume = clamp(volume)
            return
        }
        musicPlayer?.stop()
        musicPlayer = nil
        currentMusic = track

        guard let name = track.resourceName,
              let player = makeLoopingPlayer(named: name, volume: volume) else { return }
        musicPlayer = player
        player.play()
    }

    func setMusicVolume(_ volume: Float) {
        musicPlayer?.volume = clamp(volume)
    }

    // MARK: Ambient

    func setAmbient(_ track: AmbientTrack, volume: Float) {
        if track == currentAmbient {
            ambientPlayer?.volume = clamp(volume)
            return
        }
        ambientPlayer?.stop()
        ambientPlayer = nil
        currentAmbient = track

        guard let name = track.resourceName,
              let player = makeLoopingPlayer(named: name, volume: volume) else { return }
        ambientPlayer = player
        player.play()
    }

    func setAmbientVolume(_ volume: Float) {
        ambientPlayer?.volume = clamp(volume)
    }

    // MARK: Chimes

    func playChime(_ track: ChimeTrack) {
        guard let name = track.resourceName else { return }
        let player = chimePlayers[name] ?? loadChime(named: name)
        guard let player else { return }
        chimePlayers[name] = player

        duckOthers(true)
        player.currentTime = 0
        player.play()
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
            self?.duckOthers(false)
        }
    }

    // MARK: Lifecycle

    /// Tear down everything. Call when the player screen disappears.
    func stopAll() {
        musicPlayer?.stop(); musicPlayer = nil; currentMusic = .none
        ambientPlayer?.stop(); ambientPlayer = nil; currentAmbient = .none
        chimePlayers.values.forEach { $0.stop() }
        chimePlayers.removeAll()
        deactivateSession()
    }

    // MARK: Internals

    private func makeLoopingPlayer(named: String, volume: Float) -> AVAudioPlayer? {
        guard let url = bundleURL(for: named) else { return nil }
        do {
            activateSession(duck: false)
            let player = try AVAudioPlayer(contentsOf: url)
            player.numberOfLoops = -1
            player.volume = clamp(volume)
            player.prepareToPlay()
            return player
        } catch {
            return nil
        }
    }

    private func loadChime(named: String) -> AVAudioPlayer? {
        guard let url = bundleURL(for: named) else { return nil }
        do {
            let player = try AVAudioPlayer(contentsOf: url)
            player.prepareToPlay()
            return player
        } catch {
            return nil
        }
    }

    private func bundleURL(for name: String) -> URL? {
        Bundle.main.url(forResource: name, withExtension: "mp3")
            ?? Bundle.main.url(forResource: name, withExtension: "m4a")
            ?? Bundle.main.url(forResource: name, withExtension: "caf")
    }

    private func duckOthers(_ enabled: Bool) {
        activateSession(duck: enabled)
    }

    private func activateSession(duck: Bool) {
        let session = AVAudioSession.sharedInstance()
        var options: AVAudioSession.CategoryOptions = [.mixWithOthers]
        if duck { options.insert(.duckOthers) }
        try? session.setCategory(.playback, mode: .default, options: options)
        try? session.setActive(true, options: [])
    }

    private func deactivateSession() {
        try? AVAudioSession.sharedInstance().setActive(false, options: [.notifyOthersOnDeactivation])
    }

    private func clamp(_ v: Float) -> Float { min(max(v, 0), 1) }
}
