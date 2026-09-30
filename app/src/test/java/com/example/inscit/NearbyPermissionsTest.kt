package com.example.inscit

import com.example.inscit.transfer.NearbyPermissions
import org.junit.Assert.*
import org.junit.Test

class NearbyPermissionsTest {

    @Test
    fun required_isNeverEmpty() {
        assertTrue(NearbyPermissions.required().isNotEmpty())
    }

    @Test
    fun legacyBranch_requiresFineLocation() {
        // Local JVM unit tests run with SDK_INT == 0, so the pre-31 branch applies.
        // That branch must contain fine location (BLE scan needs it below Android 12).
        assertTrue(
            NearbyPermissions.required().contains("android.permission.ACCESS_FINE_LOCATION")
        )
    }
}
