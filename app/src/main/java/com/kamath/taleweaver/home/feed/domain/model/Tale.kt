package com.kamath.taleweaver.home.feed.domain.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Tale(
    val id: String = "",
    val isRootTale: Boolean = true,
    val parentTaleId: String? = null,
    val authorId: String = "",
    val authorUsername: String = "",
    val authorDisplayName: String = "",
    val authorImageUrl: String = "",
    val title: String? = null,
    val content: String = "",
    val excerpt: String? = null,
    val isLocked: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null,
    val readCount: Long = 0,
    val likesCount: Long = 0,
    val subTalesCount: Long = 0,
    val restacksCount: Long = 0,
    val shareCount: Long = 0
)
