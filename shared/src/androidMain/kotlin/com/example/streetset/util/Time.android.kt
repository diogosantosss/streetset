package com.example.streetset.util

actual object Time {
    actual fun nowMillis(): Long = System.currentTimeMillis()
}