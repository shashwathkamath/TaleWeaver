//package com.kamath.taleweaver.registration.presentation
//
//import app.cash.turbine.test
//import com.google.common.truth.Truth.assertThat
//import com.kamath.taleweaver.core.util.Resource
//import com.kamath.taleweaver.registration.domain.model.RegistrationData
//import com.kamath.taleweaver.registration.domain.usecases.RegisterUserUseCase
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.mockito.kotlin.mock
//import org.mockito.kotlin.whenever
//
//@ExperimentalCoroutinesApi
//class RegistrationViewModelTest {
//    private lateinit var viewModel: RegistrationViewModel
//    private lateinit var registerUserUseCase: RegisterUserUseCase
//    private val testDispatcher = StandardTestDispatcher()
//
//    @Before
//    fun setup() {
//        Dispatchers.setMain(testDispatcher)
//        registerUserUseCase = mock()
//        viewModel = RegistrationViewModel(registerUserUseCase)
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//    }
//
//    @Test
//    fun `onEvent(OnUserNameChange)- updates usernameState`() = runTest {
//        val newUsername = "creativewriter123"
//        viewModel.onEvent(RegistrationScreenEvent.OnUsernameChange(newUsername))
//        assertThat(viewModel.uiState.value.username).isEqualTo(newUsername)
//    }
//
//    @Test
//    fun `onEvent(onPasswordChange)-updates passwordState`() = runTest {
//        val newPassword = "creative123"
//        viewModel.onEvent(RegistrationScreenEvent.OnPasswordChange(newPassword))
//        assertThat(viewModel.uiState.value.password).isEqualTo(newPassword)
//    }
//
//    @Test
//    fun `onEvent(onEmailChange)-updates emailState`() = runTest {
//        val newEmail = "creative@example.com"
//        viewModel.onEvent(RegistrationScreenEvent.OnEmailChange(newEmail))
//        assertThat(viewModel.uiState.value.email).isEqualTo(newEmail)
//    }
//
//    @Test
//    fun `register()-on success-updates state with success message`() = runTest {
//        val newUsername = "creativewriter123"
//        val newPassword = "creative123"
//        val newEmail = "creative@example.com"
//        val newRegistrationData = RegistrationData(newUsername, newEmail, newPassword)
//        whenever(registerUserUseCase(newRegistrationData)).thenReturn(
//            flowOf(
//                Resource.Loading(),
//                Resource.Success(mock())
//            )
//        )
//        viewModel.onEvent(RegistrationScreenEvent.OnUsernameChange(newUsername))
//        viewModel.onEvent(RegistrationScreenEvent.OnPasswordChange(newPassword))
//        viewModel.onEvent(RegistrationScreenEvent.OnEmailChange(newEmail))
//
//        viewModel.uiState.test {
//            val initialState = awaitItem()
//            assertThat(initialState.username).isEqualTo(newUsername)
//            assertThat(initialState.isLoading).isFalse()
//
//            viewModel.onEvent(RegistrationScreenEvent.OnSignUpButtonPress)
//
//            val loadingState = awaitItem()
//            assertThat(loadingState.isLoading).isTrue()
//            assertThat(loadingState.errorMessage).isNull()
//
//            val successState = awaitItem()
//            assertThat(successState.isLoading).isFalse()
//            assertThat(successState.successMessage).isEqualTo("Sign Up Successful")
//        }
//    }
//
//    @Test
//    fun `register()-onError-update state with error message`() = runTest {
//        val newUsername = "creativewriter123"
//        val newPassword = "creative123"
//        val newEmail = "creative@example.com"
//        val newRegistrationData = RegistrationData(newUsername, newEmail, newPassword)
//        whenever(registerUserUseCase(newRegistrationData)).thenReturn(
//            flowOf(
//                Resource.Loading(),
//                Resource.Error("An unknown error occurred")
//            )
//        )
//        viewModel.onEvent(RegistrationScreenEvent.OnUsernameChange(newUsername))
//        viewModel.onEvent(RegistrationScreenEvent.OnPasswordChange(newPassword))
//        viewModel.onEvent(RegistrationScreenEvent.OnEmailChange(newEmail))
//
//        viewModel.uiState.test {
//            val initialState = awaitItem()
//            assertThat(initialState.username).isEqualTo(newUsername)
//            assertThat(initialState.isLoading).isFalse()
//
//            viewModel.onEvent(RegistrationScreenEvent.OnSignUpButtonPress)
//
//            val loadingState = awaitItem()
//            assertThat(loadingState.isLoading).isTrue()
//            assertThat(loadingState.errorMessage).isNull()
//
//            val errorState = awaitItem()
//            assertThat(errorState.isLoading).isFalse()
//            assertThat(errorState.errorMessage).isEqualTo("An unknown error occurred")
//        }
//    }
//
//    @Test
//    fun `register()-on success- emits NavigationToLogin event`() = runTest {
//        val newRegistrationData = RegistrationData(
//            "creativeWriter123",
//            "creative@example.com",
//            "creative123"
//        )
//        whenever(registerUserUseCase(newRegistrationData)).thenReturn(
//            flowOf(
//                Resource.Success(null),
//            )
//        )
//        viewModel.uiEvent.test {
//            viewModel.onEvent(RegistrationScreenEvent.OnUsernameChange(newRegistrationData.username))
//            viewModel.onEvent(RegistrationScreenEvent.OnEmailChange(newRegistrationData.email))
//            viewModel.onEvent(RegistrationScreenEvent.OnPasswordChange(newRegistrationData.password))
//
//            viewModel.onEvent(RegistrationScreenEvent.OnSignUpButtonPress)
//            val event = awaitItem()
//            assertThat(event).isInstanceOf(NavigationEvent.NavigateToLogin::class.java)
//            ensureAllEventsConsumed()
//        }
//    }
//}