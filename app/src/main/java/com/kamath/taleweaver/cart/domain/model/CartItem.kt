package com.kamath.taleweaver.cart.domain.model

import com.kamath.taleweaver.home.feed.domain.model.Listing

data class CartItem(
    val listing: Listing,
    val addedAt: Long = System.currentTimeMillis()
)
