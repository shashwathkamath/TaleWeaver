package com.kamath.taleweaver.cart.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamath.taleweaver.cart.domain.model.CartItem
import com.kamath.taleweaver.cart.domain.usecase.AddToCartUseCase
import com.kamath.taleweaver.cart.domain.usecase.ClearCartUseCase
import com.kamath.taleweaver.cart.domain.usecase.GetCartItemCountUseCase
import com.kamath.taleweaver.cart.domain.usecase.GetCartItemsUseCase
import com.kamath.taleweaver.cart.domain.usecase.IsItemInCartUseCase
import com.kamath.taleweaver.cart.domain.usecase.RemoveFromCartUseCase
import com.kamath.taleweaver.core.util.UiEvent
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.order.domain.model.Order
import com.kamath.taleweaver.order.domain.usecase.CreateOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CartEvent {
    data class AddToCart(val listing: Listing) : CartEvent
    data class RemoveFromCart(val listingId: String) : CartEvent
    object Checkout : CartEvent
    object ClearCart : CartEvent
}

sealed interface CartUiEvent {
    object CheckoutSuccess : CartUiEvent
    data class CheckoutError(val message: String) : CartUiEvent
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val addToCartUseCase: AddToCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val getCartItemCountUseCase: GetCartItemCountUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val isItemInCartUseCase: IsItemInCartUseCase,
    private val createOrderUseCase: CreateOrderUseCase
) : ViewModel() {

    val cartItems: StateFlow<List<CartItem>> = getCartItemsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cartItemCount: StateFlow<Int> = getCartItemCountUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _checkoutEventFlow = MutableSharedFlow<CartUiEvent>()
    val checkoutEventFlow = _checkoutEventFlow.asSharedFlow()

    fun isItemInCart(listingId: String): Flow<Boolean> {
        return isItemInCartUseCase(listingId)
    }

    fun onEvent(event: CartEvent) {
        when (event) {
            is CartEvent.AddToCart -> {
                viewModelScope.launch {
                    addToCartUseCase(event.listing)
                    _eventFlow.emit(UiEvent.ShowSnackbar("${event.listing.title} added to cart"))
                }
            }

            is CartEvent.RemoveFromCart -> {
                viewModelScope.launch {
                    removeFromCartUseCase(event.listingId)
                    _eventFlow.emit(UiEvent.ShowSnackbar("Item removed from cart"))
                }
            }

            is CartEvent.Checkout -> {
                viewModelScope.launch {
                    handleCheckout()
                }
            }

            is CartEvent.ClearCart -> {
                viewModelScope.launch {
                    clearCartUseCase()
                    _eventFlow.emit(UiEvent.ShowSnackbar("Cart cleared"))
                }
            }
        }
    }

    private suspend fun handleCheckout() {
        try {
            val currentCartItems = cartItems.value

            // Create order
            val order = Order(
                items = currentCartItems,
                totalAmount = currentCartItems.sumOf { it.listing.price },
                estimatedDeliveryDate = 0L // Will be set when seller confirms shipping
            )

            // Save order to Firestore
            createOrderUseCase(order).onSuccess { orderId ->
                // Clear cart
                clearCartUseCase()

                // Emit success event
                _checkoutEventFlow.emit(CartUiEvent.CheckoutSuccess)
            }.onFailure { error ->
                _checkoutEventFlow.emit(CartUiEvent.CheckoutError(error.message ?: "Failed to complete checkout"))
            }
        } catch (e: Exception) {
            _checkoutEventFlow.emit(CartUiEvent.CheckoutError(e.message ?: "An error occurred"))
        }
    }
}
