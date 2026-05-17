package com.lowerbackstretching.audio

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TracksTest {

    @Test
    fun music_fromStorage_round_trips_all_cases() {
        for (track in MusicTrack.entries) {
            assertThat(MusicTrack.fromStorage(track.storageValue)).isEqualTo(track)
        }
    }

    @Test
    fun music_fromStorage_falls_back_to_none_for_unknown_or_null() {
        assertThat(MusicTrack.fromStorage(null)).isEqualTo(MusicTrack.NONE)
        assertThat(MusicTrack.fromStorage("invalid")).isEqualTo(MusicTrack.NONE)
    }

    @Test
    fun ambient_fromStorage_round_trips_all_cases() {
        for (track in AmbientTrack.entries) {
            assertThat(AmbientTrack.fromStorage(track.storageValue)).isEqualTo(track)
        }
    }

    @Test
    fun chime_fromStorage_round_trips_all_cases() {
        for (track in ChimeTrack.entries) {
            assertThat(ChimeTrack.fromStorage(track.storageValue)).isEqualTo(track)
        }
    }

    @Test
    fun none_variants_have_no_resource_name() {
        assertThat(MusicTrack.NONE.resName).isNull()
        assertThat(AmbientTrack.NONE.resName).isNull()
        assertThat(ChimeTrack.NONE.resName).isNull()
    }

    @Test
    fun non_none_tracks_have_resource_name() {
        for (track in MusicTrack.entries.filter { it != MusicTrack.NONE }) {
            assertThat(track.resName).isNotNull()
        }
        for (track in AmbientTrack.entries.filter { it != AmbientTrack.NONE }) {
            assertThat(track.resName).isNotNull()
        }
        for (track in ChimeTrack.entries.filter { it != ChimeTrack.NONE }) {
            assertThat(track.resName).isNotNull()
        }
    }
}
