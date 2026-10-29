package com.example.streetset.util

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual object Time {
    actual fun nowMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()
}