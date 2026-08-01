package com.lowerbackstretching.audio

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.core.AmbientTrack
import com.lowerbackstretching.core.ChimeTrack
import com.lowerbackstretching.core.MusicTrack
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The audio MP3s are not checked into the repo (see `app/AUDIO_FILES.md`), so
 * whether any given track is playable depends on the build. These tests pin the
 * *gating contract* rather than the presence of the files: Settings must never
 * offer a track this build can't actually play, and "None" must always survive
 * so the user can still turn audio off.
 *
 * This is what stops the original bug from returning — nine selectable tracks
 * that all played silence because `res/raw/` didn't exist.
 */
@RunWith(AndroidJUnit4::class)
class AudioAvailabilityTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun none_is_always_offered_for_every_stream() {
        assertThat(AudioController.availableMusicTracks(context)).contains(MusicTrack.NONE)
        assertThat(AudioController.availableAmbientTracks(context)).contains(AmbientTrack.NONE)
        assertThat(AudioController.availableChimeTracks(context)).contains(ChimeTrack.NONE)
    }

    @Test
    fun offered_tracks_all_resolve_to_a_real_raw_resource() {
        val offered = AudioController.availableMusicTracks(context).map { it.resName } +
            AudioController.availableAmbientTracks(context).map { it.resName } +
            AudioController.availableChimeTracks(context).map { it.resName }

        for (resName in offered.filterNotNull()) {
            val id = context.resources.getIdentifier(resName, "raw", context.packageName)
            assertThat(id).isNotEqualTo(0)
        }
    }

    @Test
    fun tracks_without_a_bundled_file_are_filtered_out() {
        val missing = MusicTrack.entries.filter {
            it.resName != null && !AudioController.isTrackAvailable(context, it.resName)
        }
        assertThat(AudioController.availableMusicTracks(context)).containsNoneIn(missing)
    }

    @Test
    fun hasAnyAudioAssets_agrees_with_the_per_stream_lists() {
        val anyPlayable = AudioController.availableMusicTracks(context).size > 1 ||
            AudioController.availableAmbientTracks(context).size > 1 ||
            AudioController.availableChimeTracks(context).size > 1
        assertThat(AudioController.hasAnyAudioAssets(context)).isEqualTo(anyPlayable)
    }

    @Test
    fun null_resName_is_treated_as_available() {
        // NONE carries no resource and must never be filtered out.
        assertThat(AudioController.isTrackAvailable(context, null)).isTrue()
    }
}
