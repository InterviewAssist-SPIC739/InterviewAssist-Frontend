package com.simats.interviewassist.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.simats.interviewassist.data.models.UserProfileResponse
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PdfManager {

    fun generateUserDataPdf(context: Context, userData: UserProfileResponse): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint()

        // Background
        canvas.drawColor(Color.WHITE)

        // Header
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 24f
        titlePaint.color = Color.BLACK
        canvas.drawText("InterviewAssist - My Data Report", 50f, 80f, titlePaint)

        // Divider
        paint.color = Color.LTGRAY
        canvas.drawRect(50f, 100f, 545f, 102f, paint)

        // Content
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.style = Paint.Style.FILL
        paint.textSize = 14f
        paint.color = Color.DKGRAY

        var yPos = 140f
        val lineSpacing = 30f

        canvas.drawText("Account Information", 50f, yPos, titlePaint.apply { textSize = 18f })
        yPos += 40f

        canvas.drawText("Full Name: ${userData.firstName} ${userData.lastName}", 50f, yPos, paint)
        yPos += lineSpacing
        canvas.drawText("Email: ${userData.email}", 50f, yPos, paint)
        yPos += lineSpacing
        canvas.drawText("Role: ${userData.role}", 50f, yPos, paint)
        yPos += lineSpacing
        canvas.drawText("Account ID: ${userData.id}", 50f, yPos, paint)
        yPos += 50f

        val profile = userData.profile
        if (profile != null) {
            canvas.drawText("Profile Information", 50f, yPos, titlePaint.apply { textSize = 18f })
            yPos += 40f

            canvas.drawText("Phone: ${profile.phoneNumber ?: "Not provided"}", 50f, yPos, paint)
            yPos += lineSpacing
            canvas.drawText("Major: ${profile.major ?: "Not provided"}", 50f, yPos, paint)
            yPos += lineSpacing
            canvas.drawText("Graduation Year: ${profile.expectedGradYear ?: "Not provided"}", 50f, yPos, paint)
            yPos += lineSpacing
            canvas.drawText("Current Year: ${profile.currentYear ?: "Not provided"}", 50f, yPos, paint)
            yPos += lineSpacing
            
            // Bio (handle multi-line if needed, but keeping it simple for now)
            val bioText = profile.bio ?: "No bio provided"
            canvas.drawText("Bio: ", 50f, yPos, paint)
            yPos += 20f
            
            // Basic wrapping for bio
            val margin = 70f
            val maxWidth = pageInfo.pageWidth - (margin * 2)
            val words = bioText.split(" ")
            var line = ""
            for (word in words) {
                if (paint.measureText("$line $word") <= maxWidth) {
                    line = if (line.isEmpty()) word else "$line $word"
                } else {
                    canvas.drawText(line, margin, yPos, paint)
                    yPos += 20f
                    line = word
                }
            }
            canvas.drawText(line, margin, yPos, paint)
        } else {
            canvas.drawText("Profile details not completed yet.", 50f, yPos, paint)
        }

        // Footer
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("Generated on: ${java.util.Date()}", 50f, 800f, paint)

        pdfDocument.finishPage(page)

        return savePdfToStorage(context, pdfDocument, "InterviewAssist_Data_${userData.firstName}.pdf")
    }

    fun generateUserGuidePdf(context: Context): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint()

        canvas.drawColor(Color.WHITE)

        // Title
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 28f
        titlePaint.color = Color.parseColor("#1E293B") // Dark slate
        canvas.drawText("Interview Assist - User Guide", 50f, 80f, titlePaint)

        // Subtitle
        paint.textSize = 14f
        paint.color = Color.GRAY
        canvas.drawText("Everything you need to know to get started", 50f, 110f, paint)

        // Divider
        paint.color = Color.LTGRAY
        canvas.drawRect(50f, 130f, 545f, 132f, paint)

        var yPos = 170f
        val lineSpacing = 25f
        val sectionSpacing = 45f

        fun drawSection(title: String, points: List<String>) {
            titlePaint.textSize = 18f
            titlePaint.color = Color.parseColor("#2563EB") // Primary Blue
            canvas.drawText(title, 50f, yPos, titlePaint)
            yPos += 30f

            paint.textSize = 14f
            paint.color = Color.DKGRAY
            for (point in points) {
                canvas.drawText("• $point", 70f, yPos, paint)
                yPos += lineSpacing
            }
            yPos += sectionSpacing
        }

        drawSection("1. Getting Started", listOf(
            "Register as a Student or Alumni using your email.",
            "Complete your profile to access all platform features.",
            "Set up Two-Factor Authentication (2FA) for extra security."
        ))

        drawSection("2. Features for Students", listOf(
            "Search for companies to see specific interview experiences.",
            "Read detailed blog-style interview reviews from verified alumni.",
            "Ask questions to alumni directly through the Q&A section.",
            "Save experiences you find helpful for quick access later."
        ))

        drawSection("3. Features for Alumni", listOf(
            "Share your interview experiences with the community.",
            "Answer questions from students to help them prepare.",
            "Edit or update your shared experiences at any time."
        ))

        drawSection("4. Privacy & Security", listOf(
            "Manage your profile visibility in settings.",
            "Download a copy of your personal data at any time.",
            "Quickly change your password or enable 2FA.",
            "Contact admin@gmail.com for any technical support."
        ))

        // Footer
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("© 2026 Interview Assist. All rights reserved.", 50f, 800f, paint)

        pdfDocument.finishPage(page)
        return savePdfToStorage(context, pdfDocument, "InterviewAssist_UserGuide.pdf")
    }

    private fun savePdfToStorage(context: Context, pdfDocument: PdfDocument, fileName: String): Uri? {
        val outputStream: OutputStream?
        var uri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = uri?.let { resolver.openOutputStream(it) }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                uri = Uri.fromFile(file)
                outputStream = FileOutputStream(file)
            }

            outputStream?.use {
                pdfDocument.writeTo(it)
            }
            pdfDocument.close()
            return uri
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }
}
