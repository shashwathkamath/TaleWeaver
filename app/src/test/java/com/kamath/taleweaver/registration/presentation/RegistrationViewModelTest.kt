package com.kamath.taleweaver.registration.presentation

import com.google.common.truth.Truth.assertThat
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.login.domain.usecases.SendOtpUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class RegistrationViewModelTest {
    private lateinit var viewModel: RegistrationViewModel
    private lateinit var sendOtpUseCase: SendOtpUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sendOtpUseCase = mock()
        viewModel = RegistrationViewModel(sendOtpUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `username change updates state`() = runTest {
        viewModel.onEvent(RegistrationScreenEvent.OnUsernameChange("alice"))
        assertThat(viewModel.uiState.value.username).isEqualTo("alice")
    }

    @Test
    fun `email change updates state`() = runTest {
        viewModel.onEvent(RegistrationScreenEvent.OnEmailChange("alice@example.com"))
        assertThat(viewModel.uiState.value.email).isEqualTo("alice@example.com")
    }

    @Test
    fun `button enabled only when both username and email non-blank`() = runTest {
        assertThat(viewModel.uiState.value.isButtonEnabled).isFalse()
        viewModel.onEvent(RegistrationScreenEvent.OnUsernameChange("alice"))
        assertThat(viewModel.uiState.value.isButtonEnabled).isFalse()
        viewModel.onEvent(RegistrationScreenEvent.OnEmailChange("alice@example.com"))
        assertThat(viewModel.uiState.value.isButtonEnabled).isTrue()
    }

    @Test
    fun `sign up error sets error message`() = runTest {
        val email = "alice@example.com"
        val errorMsg = "Could not send code"
        whenever(sendOtpUseCase(email)).thenReturn(
            flowOf(ApiResult.Loading(), ApiResult.Error(errorMsg))
        )
        viewModel.onEvent(RegistrationScreenEvent.OnUsernameChange("alice"))
        viewModel.onEvent(RegistrationScreenEvent.OnEmailChange(email))
        viewModel.onEvent(RegistrationScreenEvent.OnSignUpButtonPress)

        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.errorMessage).isEqualTo(errorMsg)
    }
}
