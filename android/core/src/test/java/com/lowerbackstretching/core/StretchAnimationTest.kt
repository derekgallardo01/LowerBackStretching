package com.lowerbackstretching.core

import com.lowerbackstretching.core.model.Pose
import com.lowerbackstretching.core.model.StretchAnimationSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StretchAnimationTest {

    private val poseA = Pose(
        name = "a",
        joints = mapOf("head" to listOf(0.0, 0.0), "foot" to listOf(0.0, 1.0)),
    )
    private val poseB = Pose(
        name = "b",
        joints = mapOf("head" to listOf(1.0, 0.0), "foot" to listOf(1.0, 1.0)),
    )
    private val poseC = Pose(
        name = "c",
        joints = mapOf("head" to listOf(0.5, 0.5), "foot" to listOf(0.5, 1.0)),
    )

    @Test
    fun `t=0 returns the first pose exactly`() {
        val pose = interpolatedPose(listOf(poseA, poseB), 0f)
        assertEquals(0f, pose.getValue("head").first, 1e-5f)
        assertEquals(0f, pose.getValue("head").second, 1e-5f)
    }

    @Test
    fun `t at integer pose boundary returns the next pose exactly`() {
        val pose = interpolatedPose(listOf(poseA, poseB), 1f)
        // At t=1 we wrap into segment [B→A], and easing at frac=0 is 0 → pose B.
        assertEquals(1f, pose.getValue("head").first, 1e-5f)
    }

    @Test
    fun `mid-segment uses ease-in-out so midpoint is the literal average`() {
        // At raw=0.5, eased = (1 - cos(pi*0.5))/2 = 0.5 — average.
        val pose = interpolatedPose(listOf(poseA, poseB), 0.5f)
        assertEquals(0.5f, pose.getValue("head").first, 1e-5f)
    }

    @Test
    fun `easing produces slow start and slow end`() {
        // At raw=0.1 the eased value should be well below 0.1 (slow start).
        val early = interpolatedPose(listOf(poseA, poseB), 0.1f).getValue("head").first
        assertTrue("eased start should be slower than linear: $early", early < 0.08f)
        // At raw=0.9 the eased value should be well above 0.9 (slow end).
        val late = interpolatedPose(listOf(poseA, poseB), 0.9f).getValue("head").first
        assertTrue("eased end should be slower than linear: $late", late > 0.92f)
    }

    @Test
    fun `t wraps past the end of the loop`() {
        // t = n + 0.5 should be equivalent to t = 0.5.
        val n = 2f
        val wrapped = interpolatedPose(listOf(poseA, poseB), n + 0.5f).getValue("head")
        val ref = interpolatedPose(listOf(poseA, poseB), 0.5f).getValue("head")
        assertEquals(ref.first, wrapped.first, 1e-5f)
        assertEquals(ref.second, wrapped.second, 1e-5f)
    }

    @Test
    fun `last segment interpolates back to the first pose`() {
        // For [A, B, C], t ∈ [2, 3) interpolates C → A.
        val pose = interpolatedPose(listOf(poseA, poseB, poseC), 2.5f)
        // At eased midpoint: average of C and A on head.x: (0.5 + 0.0) / 2 = 0.25
        assertEquals(0.25f, pose.getValue("head").first, 1e-5f)
    }

    @Test
    fun `empty poses returns empty map without crashing`() {
        assertTrue(interpolatedPose(emptyList(), 0f).isEmpty())
        assertTrue(interpolatedPose(emptyList(), 99f).isEmpty())
    }

    @Test
    fun `joint missing from second pose is held in place`() {
        val withMissing = Pose(joints = mapOf("head" to listOf(1.0, 1.0)))
        val pose = interpolatedPose(listOf(poseA, withMissing), 0.5f)
        // "foot" is missing from second pose, so it should stay at poseA's foot.
        assertEquals(0f, pose.getValue("foot").first, 1e-5f)
        assertEquals(1f, pose.getValue("foot").second, 1e-5f)
    }

    @Test
    fun `tValueForElapsed maps elapsed time to the configured loop length`() {
        val spec = StretchAnimationSpec(loopSeconds = 4.0, poses = listOf(poseA, poseB))
        // Half-way through the loop with 2 poses should be t = 1.0.
        assertEquals(1f, tValueForElapsed(2.0, spec), 1e-5f)
        // A full loop wraps back to 0.
        assertEquals(0f, tValueForElapsed(4.0, spec), 1e-5f)
    }

    @Test
    fun `tValueForElapsed handles zero loopSeconds without dividing by zero`() {
        val spec = StretchAnimationSpec(loopSeconds = 0.0, poses = listOf(poseA, poseB))
        // Should not throw; falls back to a default of 1 second per loop.
        val t = tValueForElapsed(0.0, spec)
        assertEquals(0f, t, 1e-5f)
    }
}
