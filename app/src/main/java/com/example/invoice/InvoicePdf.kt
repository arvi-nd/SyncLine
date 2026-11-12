package com.example.invoice

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.model.Invoice
import com.example.model.formatAmount
import java.io.File

/** Builds a one-page A4 PDF of an invoice and hands it to the share sheet. */
object InvoicePdf {
    private const val PAGE_W = 595 // A4 at 72 dpi
    private const val PAGE_H = 842
    private const val MARGIN = 48f

    private val darkGreen = Color.parseColor("#163300")
    private val lime = Color.parseColor("#9FE870")
    private val ink = Color.parseColor("#0E0F0C")
    private val muted = Color.parseColor("#6B6F66")
    private val line = Color.parseColor("#E2E6DC")

    fun create(context: Context, invoice: Invoice, issuerName: String): File {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create())
        draw(page.canvas, invoice, issuerName)
        doc.finishPage(page)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val safeNumber = invoice.invoiceNumber.ifBlank { invoice.id }.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val file = File(dir, "Invoice-$safeNumber.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun share(context: Context, invoice: Invoice, issuerName: String) {
        val file = create(context, invoice, issuerName)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Invoice ${invoice.invoiceNumber}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share invoice").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun text(size: Float, color: Int, bold: Boolean = false) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    private fun draw(c: Canvas, inv: Invoice, issuerName: String) {
        val right = PAGE_W - MARGIN
        val contentW = (PAGE_W - 2 * MARGIN).toInt()
        val status = when {
            inv.status.equals("CANCELLED", true) -> "CANCELLED"
            inv.isPaid -> "PAID"
            else -> "UNPAID"
        }

        // Header band
        c.drawRect(0f, 0f, PAGE_W.toFloat(), 120f, Paint().apply { color = darkGreen })
        c.drawText("INVOICE", MARGIN, 62f, text(30f, lime, bold = true))
        c.drawText(inv.invoiceNumber.ifBlank { inv.id }, MARGIN, 90f, text(14f, Color.WHITE))

        // Status pill
        val pill = text(12f, darkGreen, bold = true)
        val pillW = pill.measureText(status) + 28f
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = if (status == "CANCELLED") Color.LTGRAY else lime }
        c.drawRoundRect(right - pillW, 46f, right, 74f, 14f, 14f, pillPaint)
        c.drawText(status, right - pillW + 14f, 65f, pill)

        var y = 164f
        // From / Billed to
        val label = text(10f, muted, bold = true)
        val value = text(14f, ink, bold = true)
        c.drawText("FROM", MARGIN, y, label)
        c.drawText("BILLED TO", PAGE_W / 2f, y, label)
        c.drawText(issuerName.ifBlank { "—" }, MARGIN, y + 20f, value)
        c.drawText(inv.clientName.ifBlank { "—" }, PAGE_W / 2f, y + 20f, value)
        y += 60f

        // Dates / project
        c.drawText("ISSUED", MARGIN, y, label)
        c.drawText("DUE DATE", PAGE_W / 2f, y, label)
        val plain = text(14f, ink)
        val issued = inv.createdAtTs?.toDate()?.toInstant()?.atZone(java.time.ZoneId.systemDefault())
            ?.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.US))
            ?: inv.createdAt.takeUnless { it.isBlank() || it.equals("Today", ignoreCase = true) } ?: "—"
        c.drawText(issued, MARGIN, y + 20f, plain)
        c.drawText(inv.dueDate.ifBlank { "—" }, PAGE_W / 2f, y + 20f, plain)
        y += 60f

        if (!inv.projectName.isNullOrBlank()) {
            c.drawText("PROJECT", MARGIN, y, label)
            c.drawText(inv.projectName, MARGIN, y + 20f, plain)
            y += 60f
        }

        // Line item
        val rule = Paint().apply { color = line; strokeWidth = 1f }
        c.drawLine(MARGIN, y, right, y, rule)
        c.drawText("DESCRIPTION", MARGIN, y + 24f, label)
        val amountLabel = "AMOUNT"
        c.drawText(amountLabel, right - label.measureText(amountLabel), y + 24f, label)
        c.drawLine(MARGIN, y + 36f, right, y + 36f, rule)

        val titleLayout = StaticLayout.Builder
            .obtain(inv.title, 0, inv.title.length, text(15f, ink), contentW - 150)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL).build()
        c.save(); c.translate(MARGIN, y + 52f); titleLayout.draw(c); c.restore()
        val amountText = inv.formatAmount()
        val amountPaint = text(15f, ink, bold = true)
        c.drawText(amountText, right - amountPaint.measureText(amountText), y + 68f, amountPaint)
        y += 52f + titleLayout.height + 20f

        c.drawLine(MARGIN, y, right, y, rule)
        y += 40f

        // Total
        val totalLabel = text(12f, muted, bold = true)
        c.drawText("TOTAL DUE", right - 220f, y, totalLabel)
        val total = text(26f, darkGreen, bold = true)
        c.drawText(amountText, right - total.measureText(amountText), y + 34f, total)
        y += 70f

        if (!inv.notes.isNullOrBlank()) {
            c.drawText("NOTES", MARGIN, y, label)
            val notes = StaticLayout.Builder
                .obtain(inv.notes, 0, inv.notes.length, text(12f, ink), contentW)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL).build()
            c.save(); c.translate(MARGIN, y + 12f); notes.draw(c); c.restore()
        }

        if (inv.isPaid && !inv.paidAt.isNullOrBlank()) {
            c.drawText(inv.paidAt, MARGIN, PAGE_H - 90f, text(12f, darkGreen, bold = true))
        }

        // Footer
        c.drawLine(MARGIN, PAGE_H - 64f, right, PAGE_H - 64f, rule)
        c.drawText("Created with SyncLine", MARGIN, PAGE_H - 44f, text(10f, muted))
    }
}
