package com.kamath.taleweaver.login.presentation

import app.cash.turbine.test
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
class LoginViewModelTest {
    private lateinit var viewmodel: LoginScreenViewmodel
    private lateinit var sendOtpUseCase: SendOtpUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sendOtpUseCase = mock()
        viewmodel = LoginScreenViewmodel(sendOtpUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEmailChange updates email state and enables button`() = runTest {
        val email = "test@example.com"
        viewmodel.onEvent(LoginUiEvent.OnEmailChange(email))
        assertThat(viewmodel.uiState.value.email).isEqualTo(email)
        assertThat(viewmodel.uiState.value.isButtonEnabled).isTrue()
    }

    @Test
    fun `empty email disables button`() = runTest {
        viewmodel.onEvent(LoginUiEvent.OnEmailChange("test@example.com"))
        assertThat(viewmodel.uiState.value.isButtonEnabled).isTrue()
        viewmodel.onEvent(LoginUiEvent.OnEmailChange(""))
        assertThat(viewmodel.uiState.value.isButtonEnabled).isFalse()
    }

    @Test
    fun `sendCode success navigates to OTP screen`() = runTest {
        val email = "test@example.com"
        whenever(sendOtpUseCase(email)).thenReturn(
            flowOf(ApiResult.Loading(), ApiResult.Success(Unit))
        )
        viewmodel.onEvent(LoginUiEvent.OnEmailChange(email))
        viewmodel.onEvent(LoginUiEvent.SendCodeButtonPress)

        viewmodel.uiState.test {
            val idle = awaitItem()
            assertThat(idle.isLoading).isFalse()

            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()

            val done = awaitItem()
            assertThat(done.isLoading).isFalse()
            assertThat(done.errorMessage).isNull()
        }
    }

    @Test
    fun `sendCode error sets error message`() = runTest {
        val email = "test@example.com"
        val errorMsg = "Could not send code"
        whenever(sendOtpUseCase(email)).thenReturn(
            flowOf(ApiResult.Loading(), ApiResult.Error(errorMsg))
        )
        viewmodel.onEvent(LoginUiEvent.OnEmailChange(email))
        viewmodel.onEvent(LoginUiEvent.SendCodeButtonPress)

        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewmodel.uiState.value.errorMessage).isEqualTo(errorMsg)
    }

    @Test
    fun `ErrorDismissed clears error message`() = runTest {
        val email = "test@example.com"
        whenever(sendOtpUseCase(email)).thenReturn(
            flowOf(ApiResult.Loading(), ApiResult.Error("error"))
        )
        viewmodel.onEvent(LoginUiEvent.OnEmailChange(email))
        viewmodel.onEvent(LoginUiEvent.SendCodeButtonPress)
        testDispatcher.scheduler.advanceUntilIdle()

        viewmodel.onEvent(LoginUiEvent.ErrorDismissed)
        assertThat(viewmodel.uiState.value.errorMessage).isNull()
    }
}
