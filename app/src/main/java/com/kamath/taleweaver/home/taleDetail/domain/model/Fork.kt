package com.kamath.taleweaver.home.taleDetail.domain.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Fork(
    val content:String,
    @ServerTimestamp
    val createdAt: Date,
    val likesCount:Int,
    val parentId:String,
    val status:String,
    val submitterDisplayName:String,
    val submitterId:String,
    val submitterUsername:String,
    val title:String
)
