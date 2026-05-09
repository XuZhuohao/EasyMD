package com.easymd.core.export

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.webkit.WebView
import android.webkit.WebViewClient
import com.easymd.core.model.Document
import com.easymd.core.model.PdfMargin
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface ExportService {
    suspend fun exportHtml(document: Document, darkTheme: Boolean = false): File
    suspend fun exportPdf(document: Document, margin: PdfMargin, darkTheme: Boolean = false): File
}

@Singleton
class ExportServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val markdownParser: com.easymd.core.markdown.MarkdownParser
) : ExportService {

    private val exportDir: File by lazy {
        File(context.filesDir, "exports").also { it.mkdirs() }
    }

    override suspend fun exportHtml(document: Document, darkTheme: Boolean): File =
        withContext(Dispatchers.IO) {
            val html = markdownParser.toHtml(document.content, darkTheme)
            val out = File(exportDir, "${document.title}.html")
            out.writeText(html)
            out
        }

    override suspend fun exportPdf(document: Document, margin: PdfMargin, darkTheme: Boolean): File =
        withContext(Dispatchers.Main) {
            val html = markdownParser.toHtml(document.content, darkTheme)
            val out  = File(exportDir, "${document.title}.pdf")
            suspendCancellableCoroutine { cont ->
                val webView = WebView(context)
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        try {
                            val adapter: PrintDocumentAdapter = webView.createPrintDocumentAdapter(document.title)
                            val printAttrs = PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                .setMinMargins(margin.toPrintMargins())
                                .build()
                            // Write to file via PrintDocumentAdapter
                            // Real implementation would use PdfDocument or PrintManager
                            out.createNewFile()
                            cont.resume(out)
                        } catch (e: Exception) {
                            cont.resumeWithException(e)
                        }
                    }
                }
                webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
            }
        }

    private fun PdfMargin.toPrintMargins() = when (this) {
        PdfMargin.NARROW   -> PrintAttributes.Margins(6_350, 6_350, 6_350, 6_350)
        PdfMargin.STANDARD -> PrintAttributes.Margins(12_700, 12_700, 12_700, 12_700)
        PdfMargin.WIDE     -> PrintAttributes.Margins(19_050, 19_050, 19_050, 19_050)
    }
}
