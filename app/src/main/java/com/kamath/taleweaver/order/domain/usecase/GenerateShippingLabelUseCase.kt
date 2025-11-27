package com.kamath.taleweaver.order.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.order.domain.model.Order
import com.kamath.taleweaver.order.util.ShippingLabelGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Generates shipping label PDF and uploads to Firebase Storage
 * Returns the download URL
 */
class GenerateShippingLabelUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) {

    suspend operator fun invoke(order: Order): ApiResult<String> {
        return try {
            // Validate that both addresses exist
            if (order.buyerAddress == null || order.sellerAddress == null) {
                return ApiResult.Error("Missing buyer or seller address")
            }

            Timber.d("Generating shipping label for order: ${order.id}")

            // Generate PDF locally
            val pdfFile = ShippingLabelGenerator.generateLabel(context, order)

            // Upload to Firebase Storage
            val storagePath = "shipping_labels/${order.id}.pdf"
            val storageRef = storage.reference.child(storagePath)

            val uploadTask = storageRef.putFile(Uri.fromFile(pdfFile)).await()
            Timber.d("Label uploaded successfully")

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Timber.d("Label download URL: $downloadUrl")

            // Clean up local file
            pdfFile.delete()

            ApiResult.Success(downloadUrl)

        } catch (e: Exception) {
            Timber.e(e, "Error generating shipping label")
            ApiResult.Error(e.message ?: "Failed to generate shipping label")
        }
    }
}
