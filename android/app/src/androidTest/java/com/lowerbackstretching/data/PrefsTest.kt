package com.lowerbackstretching.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.audio.AmbientTrack
import com.lowerbackstretching.audio.AudioDefaults
import com.lowerbackstretching.audio.ChimeTrack
import com.lowerbackstretching.audio.MusicTrack
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrefsTest {

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs = Prefs(ctx)

    @Before
    fun reset() = runBlocking {
        prefs.resetForTests()
    }

    @Test
    fun defaults_when_nothing_has_been_written() = runBlocking {
        assertThat(prefs.reminderEnabled.first()).isFalse()
        assertThat(prefs.reminderHour.first()).isEqualTo(ReminderDefaults.HOUR)
        assertThat(prefs.reminderMinute.first()).isEqualTo(ReminderDefaults.MINUTE)
        assertThat(prefs.onboardingDone.first()).isFalse()
    }

    @Test
    fun setReminder_persists_all_three_values() = runBlocking {
        prefs.setReminder(enabled = true, hour = 7, minute = 30)
        assertThat(prefs.reminderEnabled.first()).isTrue()
        assertThat(prefs.reminderHour.first()).isEqualTo(7)
        assertThat(prefs.reminderMinute.first()).isEqualTo(30)
    }

    @Test
    fun setReminder_overwrites_previous_values() = runBlocking {
        prefs.setReminder(enabled = true, hour = 7, minute = 30)
        prefs.setReminder(enabled = false, hour = 21, minute = 0)
        assertThat(prefs.reminderEnabled.first()).isFalse()
        assertThat(prefs.reminderHour.first()).isEqualTo(21)
        assertThat(prefs.reminderMinute.first()).isEqualTo(0)
    }

    @Test
    fun markOnboardingDone_flips_the_flag() = runBlocking {
        assertThat(prefs.onboardingDone.first()).isFalse()
        prefs.markOnboardingDone()
        assertThat(prefs.onboardingDone.first()).isTrue()
    }

    @Test
    fun inProgress_round_trip_and_clear() = runBlocking {
        assertThat(prefs.inProgressSession.first()).isNull()

        prefs.saveInProgress(InProgressSession("lower-back-relief-7day", 3, 2))
        val read = prefs.inProgressSession.first()
        assertThat(read).isEqualTo(InProgressSession("lower-back-relief-7day", 3, 2))

        prefs.clearInProgress()
        assertThat(prefs.inProgressSession.first()).isNull()
    }

    @Test
    fun saveInProgress_overwrites_previous_record() = runBlocking {
        prefs.saveInProgress(InProgressSession("p1", 1, 0))
        prefs.saveInProgress(InProgressSession("p2", 4, 3))
        assertThat(prefs.inProgressSession.first()).isEqualTo(InProgressSession("p2", 4, 3))
    }

    @Test
    fun audio_defaults_when_nothing_written() = runBlocking {
        assertThat(prefs.musicTrack.first()).isEqualTo(MusicTrack.NONE)
        assertThat(prefs.ambientTrack.first()).isEqualTo(AmbientTrack.NONE)
        assertThat(prefs.chimeTrack.first()).isEqualTo(ChimeTrack.NONE)
        assertThat(prefs.musicVolume.first()).isEqualTo(AudioDefaults.MUSIC_VOLUME)
        assertThat(prefs.ambientVolume.first()).isEqualTo(AudioDefaults.AMBIENT_VOLUME)
    }

    @Test
    fun audio_track_and_volume_round_trip() = runBlocking {
        prefs.setMusicTrack(MusicTrack.LOFI)
        prefs.setMusicVolume(0.25f)
        prefs.setAmbientTrack(AmbientTrack.OCEAN)
        prefs.setAmbientVolume(0.9f)
        prefs.setChimeTrack(ChimeTrack.BELL)

        assertThat(prefs.musicTrack.first()).isEqualTo(MusicTrack.LOFI)
        assertThat(prefs.musicVolume.first()).isEqualTo(0.25f)
        assertThat(prefs.ambientTrack.first()).isEqualTo(AmbientTrack.OCEAN)
        assertThat(prefs.ambientVolume.first()).isEqualTo(0.9f)
        assertThat(prefs.chimeTrack.first()).isEqualTo(ChimeTrack.BELL)
    }

    @Test
    fun audio_volume_clamps_to_unit_range() = runBlocking {
        prefs.setMusicVolume(-1f)
        assertThat(prefs.musicVolume.first()).isEqualTo(0f)
        prefs.setMusicVolume(2f)
        assertThat(prefs.musicVolume.first()).isEqualTo(1f)
    }
}
