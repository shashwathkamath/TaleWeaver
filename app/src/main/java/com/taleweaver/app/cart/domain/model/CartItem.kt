package com.taleweaver.app.cart.domain.model

import com.taleweaver.app.home.feed.domain.model.Listing

data class CartItem(
    val listing: Listing,
    val addedAt: Long = System.currentTimeMillis()
)
