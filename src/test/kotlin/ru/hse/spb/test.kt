package ru.hse.spb

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class TestSource {
    @Test
    fun testSample1() {
        val edges = listOf(
                Pair(1, 3),
                Pair(4, 3),
                Pair(4, 2),
                Pair(1, 2))
        assertArrayEquals(intArrayOf(0, 0, 0, 0), solve(edges).toIntArray())
    }

    @Test
    fun testSample2() {
        val edges = listOf(
                Pair(1, 2),
                Pair(3, 4),
                Pair(6, 4),
                Pair(2, 3),
                Pair(1, 3),
                Pair(3, 5))
        assertArrayEquals(intArrayOf(0, 0, 0, 1, 1, 2), solve(edges).toIntArray())
    }
}