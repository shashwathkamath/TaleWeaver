package com.kamath.taleweaver.core.util

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object FirebaseDiagnostics {
    private const val TAG = "FirestoreDiagnostics"
    private val httpClient = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    suspend fun listRootCollections(): List<String> = withContext(Dispatchers.IO) {
        val projectId = FirebaseApp.getInstance().options.projectId
        if (projectId.isNullOrBlank()) {
            Log.e(TAG, "Missing Firebase projectId")
            return@withContext emptyList()
        }

        val user = FirebaseAuth.getInstance().currentUser
        val token = try {
            user?.getIdToken(true)?.await()?.token
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to fetch ID token",
                e
            )
            null
        }
        if (token.isNullOrBlank()) {
            Log.e(
                TAG,
                "No ID token available. Make sure a user is signed in."
            )
            return@withContext emptyList()
        }

        val url =
            "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents:listCollectionIds"
        val body = JSONObject()
            .put("parent", "projects/$projectId/databases/(default)/documents")
            .toString()

        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody(JSON))
            .addHeader("Authorization", "Bearer $token")
            .build()

        try {
            httpClient.newCall(request).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    Log.e(TAG, "listRootCollections failed: $text")
                    return@withContext emptyList()
                }
                val json = JSONObject(text)
                val arr = json.optJSONArray("collectionIds") ?: return@withContext emptyList()
                val result = mutableListOf<String>()
                for (i in 0 until arr.length()) result.add(arr.getString(i))
                Log.d(TAG, "Root collections: $result")
                return@withContext result.toList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing collections", e)
            emptyList()
        }
    }

    suspend fun sampleCollections(collectionNames: List<String>, limit: Long = 3) {
        val firestore = FirebaseFirestore.getInstance()
        withContext(Dispatchers.IO) {
            collectionNames.forEach { col ->
                try {
                    val snap = firestore.collection(col).limit(limit).get().await()
                    Log.d(TAG, "Sample from \"$col\": count=${snap.size()}")
                    snap.documents.forEach { d ->
                        Log.d(TAG, "doc id=${d.id}, data=${d.data}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sampling collection $col", e)
                }
            }
        }
    }
}