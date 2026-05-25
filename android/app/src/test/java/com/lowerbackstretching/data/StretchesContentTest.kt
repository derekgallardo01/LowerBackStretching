package com.lowerbackstretching.data

import com.google.common.truth.Truth.assertThat
import com.lowerbackstretching.core.model.Stretch
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File

/**
 * Build-time guard: fail the unit test if any bundled stretch still has
 * a `PLACEHOLDER_*` youtubeId. Empty string is acceptable — that's the
 * explicit "no video yet" sentinel the player handles gracefully. We
 * only fail on placeholders so the project can't accidentally ship the
 * dev-time sentinel into a release build.
 */
class StretchesContentTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `no stretch ships with a PLACEHOLDER_ youtubeId`() {
        val file = File("src/main/assets/stretches.json")
        assertThat(file.exists()).isTrue()

        val stretches: List<Stretch> = json.decodeFromString(file.readText())
        val placeholders = stretches.filter { it.youtubeId.startsWith("PLACEHOLDER_") }

        assertThat(placeholders.map { it.id }).isEmpty()
    }
}
