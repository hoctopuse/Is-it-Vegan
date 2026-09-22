package com.example.isitvegan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object OcrThreading {
    suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    suspend fun <T> cpu(block: () -> T): T = withContext(Dispatchers.Default) { block() }
}
