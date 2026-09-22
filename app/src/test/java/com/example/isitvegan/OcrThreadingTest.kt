package com.example.isitvegan

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OcrThreadingTest {
    @Test fun imageWorkRunsAwayFromTheCallingThread() = runBlocking {
        val callingThread = Thread.currentThread().name
        val workerThread = OcrThreading.io { Thread.currentThread().name }

        assertNotEquals(callingThread, workerThread)
    }
}
