package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.CouncilMessage
import com.example.viewmodel.ChatViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChatViewModelTest {

    private lateinit var app: Application
    private lateinit var chatViewModel: ChatViewModel

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        chatViewModel = ChatViewModel(app)
    }

    @Test
    fun `test Initial Messages State Is Empty Or Loaded`() = runBlocking {
        val messages = chatViewModel.messages.first()
        assertNotNull(messages)
    }

    @Test
    fun `test Clear Chat Removes Messages`() = runBlocking {
        chatViewModel.clearChat()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val messages = chatViewModel.messages.first()
        assertEquals(0, messages.size)
    }

    @Test
    fun `test Api Error State Initial Value Is Null`() {
        assertEquals(null, chatViewModel.apiErrorState.value)
    }
}
