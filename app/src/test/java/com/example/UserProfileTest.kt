package com.example

import com.example.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UserProfileTest {

    @Test
    fun `user profile model instantiation`() {
        val profile = UserProfile(
            uid = "user_123",
            displayName = "Alex Carter",
            email = "alex@syncline.io"
        )

        assertEquals("user_123", profile.uid)
        assertEquals("Alex Carter", profile.displayName)
        assertEquals("alex@syncline.io", profile.email)
    }

    @Test
    fun `user profile default values`() {
        val profile = UserProfile()
        assertEquals("", profile.uid)
        assertEquals("", profile.displayName)
        assertEquals("", profile.email)
    }
}
