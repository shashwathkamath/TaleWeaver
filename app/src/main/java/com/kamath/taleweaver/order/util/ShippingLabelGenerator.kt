package com.kamath.taleweaver.order.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Typeface
import com.kamath.taleweaver.order.domain.model.Address
import com.kamath.taleweaver.order.domain.model.Order
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Generates shipping label PDF for manual shipping
 * Uses Android's built-in PdfDocument API
 */
object ShippingLabelGenerator {

    private const val PAGE_WIDTH = 595  // A4 width in points (210mm)
    private const val PAGE_HEIGHT = 842 // A4 height in points (297mm)
    private const val MARGIN = 40f
    private const val LINE_SPACING = 25f

    /**
     * Generate shipping label PDF and save to cache directory
     * Returns the file path
     */
    fun generateLabel(context: Context, order: Order): File {
        val pdfDocument = PdfDocument()

        // Create a page
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Setup paint objects
        val titlePaint = Paint().apply {
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headingPaint = Paint().apply {
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            textSize = 14f
            isAntiAlias = true
        }

        val smallPaint = Paint().apply {
            textSize = 12f
            isAntiAlias = true
        }

        var yPosition = MARGIN + 30f

        // Title
        canvas.drawText("SHIPPING LABEL", MARGIN, yPosition, titlePaint)
        yPosition += LINE_SPACING * 2

        // Order ID
        canvas.drawText("Order ID: ${order.id}", MARGIN, yPosition, textPaint)
        yPosition += LINE_SPACING * 1.5f

        // Horizontal line
        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, textPaint)
        yPosition += LINE_SPACING * 1.5f

        // FROM section
        canvas.drawText("FROM (Seller):", MARGIN, yPosition, headingPaint)
        yPosition += LINE_SPACING

        order.sellerAddress?.let { address ->
            val fromLines = formatAddress(address)
            fromLines.forEach { line ->
                canvas.drawText(line, MARGIN + 20, yPosition, textPaint)
                yPosition += LINE_SPACING
            }
        }

        yPosition += LINE_SPACING

        // TO section
        canvas.drawText("TO (Buyer):", MARGIN, yPosition, headingPaint)
        yPosition += LINE_SPACING

        order.buyerAddress?.let { address ->
            val toLines = formatAddress(address)
            toLines.forEach { line ->
                canvas.drawText(line, MARGIN + 20, yPosition, textPaint)
                yPosition += LINE_SPACING
            }
        }

        yPosition += LINE_SPACING * 1.5f

        // Horizontal line
        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, textPaint)
        yPosition += LINE_SPACING * 1.5f

        // Book details
        canvas.drawText("CONTENTS:", MARGIN, yPosition, headingPaint)
        yPosition += LINE_SPACING
        canvas.drawText("Book: ${order.bookTitle}", MARGIN + 20, yPosition, textPaint)
        yPosition += LINE_SPACING
        canvas.drawText("Author: ${order.bookAuthor}", MARGIN + 20, yPosition, textPaint)
        yPosition += LINE_SPACING * 2

        // Instructions for seller
        canvas.drawText("INSTRUCTIONS FOR SELLER:", MARGIN, yPosition, headingPaint)
        yPosition += LINE_SPACING
        canvas.drawText("1. Print this label and attach to package", MARGIN + 20, yPosition, smallPaint)
        yPosition += LINE_SPACING * 0.8f
        canvas.drawText("2. Pack the book securely", MARGIN + 20, yPosition, smallPaint)
        yPosition += LINE_SPACING * 0.8f
        canvas.drawText("3. Book courier (India Post, DTDC, Blue Dart, etc.)", MARGIN + 20, yPosition, smallPaint)
        yPosition += LINE_SPACING * 0.8f
        canvas.drawText("4. Get tracking number from courier", MARGIN + 20, yPosition, smallPaint)
        yPosition += LINE_SPACING * 0.8f
        canvas.drawText("5. Update tracking number in app", MARGIN + 20, yPosition, smallPaint)
        yPosition += LINE_SPACING * 2

        // Recommended couriers
        canvas.drawText("Recommended Couriers:", MARGIN, yPosition, headingPaint)
        yPosition += LINE_SPACING
        canvas.drawText("• India Post (Book Post): ₹20-40 (cheapest for books)", MARGIN + 20, yPosition, smallPaint)
        yPosition += LINE_SPACING * 0.8f
        canvas.drawText("• Speed Post: ₹50-80 (faster, trackable)", MARGIN + 20, yPosition, smallPaint)
        yPosition += LINE_SPACING * 0.8f
        canvas.drawText("• DTDC/Blue Dart: ₹60-100 (fastest)", MARGIN + 20, yPosition, smallPaint)

        // Footer
        yPosition = PAGE_HEIGHT - MARGIN - 20
        canvas.drawText("Generated by TaleWeaver - Book Marketplace", MARGIN, yPosition, smallPaint)

        pdfDocument.finishPage(page)

        // Save to file
        val fileName = "shipping_label_${order.id}.pdf"
        val file = File(context.cacheDir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Timber.d("Shipping label generated: ${file.absolutePath}")
        } catch (e: Exception) {
            Timber.e(e, "Error writing PDF")
            throw e
        } finally {
            pdfDocument.close()
        }

        return file
    }

    /**
     * Format address into separate lines for display
     */
    private fun formatAddress(address: Address): List<String> {
        return buildList {
            add(address.name)
            add(address.phone)
            add(address.addressLine1)
            if (address.addressLine2.isNotBlank()) {
                add(address.addressLine2)
            }
            if (address.landmark.isNotBlank()) {
                add("Near: ${address.landmark}")
            }
            add("${address.city}, ${address.state}")
            add("PIN: ${address.pincode}")
            add(address.country)
        }
    }
}
