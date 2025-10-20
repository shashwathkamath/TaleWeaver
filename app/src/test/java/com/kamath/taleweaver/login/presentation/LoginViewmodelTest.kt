package com.kamath.taleweaver.login.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.login.domain.usecases.LoginUserUseCase
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
class LoginViewmodelTest {
    private lateinit var viewmodel: LoginScreenViewmodel
    private lateinit var loginUserUseCase: LoginUserUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loginUserUseCase = mock()
        viewmodel = LoginScreenViewmodel(loginUserUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEvent(onEmailChange)-updates email state`() = runTest {
        val newEmail = "test@example.com"
        viewmodel.onEvent(LoginUiEvent.OnEmailChange(newEmail))
        assertThat(viewmodel.uiState.value.email).isEqualTo(newEmail)
    }

    @Test
    fun `onEvent(onPasswordChange)-updates password state`() = runTest {
        val newPassword = "test123"
        viewmodel.onEvent(LoginUiEvent.OnPasswordChange(newPassword))
        assertThat(viewmodel.uiState.value.password).isEqualTo(newPassword)
    }

    @Test
    fun `state -isButtonEnabled is true when email and password are not blank`() = runTest {
        assertThat(viewmodel.uiState.value.isButtonEnabled).isFalse()
        viewmodel.onEvent(LoginUiEvent.OnEmailChange("test@example.com"))
        assertThat(viewmodel.uiState.value.isButtonEnabled).isFalse()
        viewmodel.onEvent(LoginUiEvent.OnPasswordChange("test123"))
        assertThat(viewmodel.uiState.value.isButtonEnabled).isTrue()
        viewmodel.onEvent(LoginUiEvent.OnEmailChange(""))
        assertThat(viewmodel.uiState.value.isButtonEnabled).isFalse()
    }

    @Test
    fun `login()-on success-updates state with success message`() = runTest {
        val email = "test@example.com"
        val password = "test123"
        whenever(loginUserUseCase(email, password)).thenReturn(
            flowOf(
                Resource.Loading(),
                Resource.Success(mock())
            )
        )
        viewmodel.onEvent(LoginUiEvent.OnEmailChange(email))
        viewmodel.onEvent(LoginUiEvent.OnPasswordChange(password))
        viewmodel.onEvent(LoginUiEvent.LoginButtonPress)

        viewmodel.uiState.test {
            val initialState = awaitItem()
            assertThat(initialState.isLoading).isFalse()

            val loadingState = awaitItem()
            assertThat(loadingState.isLoading).isTrue()

            val successState = awaitItem()
            assertThat(successState.isLoading).isFalse()
            assertThat(successState.successMessage).isEqualTo("Login Successful")
            assertThat(successState.errorMessage).isNull()
        }
    }

    @Test
    fun `login()-onError-update state with error message`() = runTest {
        val email = "test@example.com"
        val password = "wrongpassword"
        whenever(loginUserUseCase(email, password)).thenReturn(
            flowOf(
                Resource.Loading(),
                Resource.Error("An unknown error occurred")
            )
        )
        viewmodel.onEvent(LoginUiEvent.OnEmailChange(email))
        viewmodel.onEvent(LoginUiEvent.OnPasswordChange(password))
        viewmodel.onEvent(LoginUiEvent.LoginButtonPress)

        viewmodel.uiState.test {
            val initialState = awaitItem()
            assertThat(initialState.isLoading).isFalse()

            val loadingState = awaitItem()
            assertThat(loadingState.isLoading).isTrue()

            val errorState = awaitItem()
            assertThat(errorState.isLoading).isFalse()
            assertThat(errorState.errorMessage).isEqualTo("An unknown error occurred")
            assertThat(errorState.successMessage).isNull()
        }
    }

    @Test
    fun `onEvent(ErrorDismissed)-clears the error message`() = runTest {
        val email = "wrong@example.com"
        val password = "wrongpassword"
        val errorMessage = "An unknown error occurred"
        whenever(loginUserUseCase(email, password)).thenReturn(
            flowOf(
                Resource.Loading(),
                Resource.Error(errorMessage)
            )
        )
        viewmodel.onEvent(LoginUiEvent.OnEmailChange(email))
        viewmodel.onEvent(LoginUiEvent.OnPasswordChange(password))
        viewmodel.onEvent(LoginUiEvent.LoginButtonPress)

        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewmodel.uiState.value.errorMessage).isEqualTo(errorMessage)

        viewmodel.onEvent(LoginUiEvent.ErrorDismissed)
        assertThat(viewmodel.uiState.value.errorMessage).isNull()
    }
}