/**
 * Copyright (c) 2024-present, Staxtech (@gidraphdanford.dev)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package org.staxtech.android.pdf

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.google.gson.Gson
import com.infomaniak.lib.pdfview.PDFView
import com.infomaniak.lib.pdfview.link.LinkHandler
import com.infomaniak.lib.pdfview.listener.OnDrawListener
import com.infomaniak.lib.pdfview.listener.OnErrorListener
import com.infomaniak.lib.pdfview.listener.OnLoadCompleteListener
import com.infomaniak.lib.pdfview.listener.OnPageChangeListener
import com.infomaniak.lib.pdfview.listener.OnPageScrollListener
import com.infomaniak.lib.pdfview.listener.OnTapListener
import com.infomaniak.lib.pdfview.model.LinkTapEvent
import com.infomaniak.lib.pdfview.util.FitPolicy
import org.staxtech.android.pdf.events.TopChangeEvent
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.Objects
import kotlin.math.max


@SuppressLint("ViewConstructor")
class PdfView(context: ThemedReactContext?, set: AttributeSet?, mCallerContext: ReactContext?) :
    PDFView(context, set), OnPageChangeListener, OnLoadCompleteListener, OnErrorListener,
    OnTapListener, OnDrawListener, OnPageScrollListener, LinkHandler {
    private var reactContext: ReactContext? = null
    private var themedReactContext: ThemedReactContext? = null
    private var page = 1 // start from 1
    private var horizontal = false
    private var scale = 1f
    private var minScale = 1f
    private var maxScale = 3f
    private var path: String? = null
    private var spacing = 10
    private var password = ""
    private var enableAntialiasing = true
    private var enableAnnotationRendering = true
    private var autoSpacing = false
    private var pageFling = false
    private var pageSnap = false
    private var fitPolicy = FitPolicy.WIDTH
    private var singlePage = false
    private var originalWidth = 0f
    private var lastPageWidth = 0f
    private var lastPageHeight = 0f

    private var loadCompleteTime: Long = 0

    // used to store the parameters for `super.onSizeChanged`
    private var oldW = 0
    private var oldH = 0

    init {
        reactContext = mCallerContext
        themedReactContext = context
    }

    private fun sendEvent(message: String) {
        // showLog("loadComplete:: $event.toString()");
        val event = Arguments.createMap()
        event.putString("message", message)
        val dispatcher =
            UIManagerHelper.getEventDispatcherForReactTag(context as ThemedReactContext, id)
        val topChangeEvent = TopChangeEvent(UIManagerHelper.getSurfaceId(this), id, event)
        dispatcher?.dispatchEvent(topChangeEvent)
    }

    override fun onPageChanged(currentPage: Int, numberOfPages: Int) {
        // pdf lib page start from 0, convert it to our page (start from 1)
        val page = currentPage + 1
        this.page = page
        // showLog(String.format("%s %s / %s", path, page, numberOfPages))

        if (System.currentTimeMillis() - loadCompleteTime > 1000) {
            sendEvent("pageChanged|$page|$numberOfPages")
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if ((w > 0 && h > 0) || (this.oldW > 0) || (this.oldH > 0)) {
            super.onSizeChanged(w, h, this.oldW, this.oldH)
            this.oldW = w
            this.oldH = h
        }
    }

    override fun loadComplete(numberOfPages: Int) {
        val pageSize = getPageSize(0)
        val width = pageSize.width
        val height = pageSize.height

        this.zoomTo(this.scale)

        //create a new json Object for the TableOfContents
        val gson = Gson()
        sendEvent(
            "loadComplete|" + numberOfPages + "|" + width + "|" + height + "|" + gson.toJson(
                this.tableOfContents
            )
        )
        loadCompleteTime = System.currentTimeMillis()

        // showLog("loadComplete:: $gson.toJson(this.getTableOfContents())");
    }

    override fun onError(t: Throwable) {
        val eventMessage: String = if (Objects.requireNonNull<String?>(t.message)
                .contains("Password required or incorrect password")
        ) {
            "error|Password required or incorrect password."
        } else {
            "error|" + t.message
        }
        sendEvent(eventMessage)
    }

    override fun onPageScrolled(page: Int, positionOffset: Float){
        // showLog("onPageScrolled")
    }

    override fun onTap(e: MotionEvent): Boolean {
        sendEvent("pageSingleTap|" + page + "|" + e.x + "|" + e.y)
        return true
    }

    override fun onLayerDrawn(
        canvas: Canvas,
        pageWidth: Float,
        pageHeight: Float,
        displayedPage: Int
    ) {
        if (originalWidth == 0f) {
            originalWidth = pageWidth
        }

        if (lastPageWidth > 0 && lastPageHeight > 0 && (pageWidth != lastPageWidth || pageHeight != lastPageHeight)) {
            sendEvent("scaleChanged|" + (pageWidth / originalWidth))
        }

        lastPageWidth = pageWidth
        lastPageHeight = pageHeight
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (this.isRecycled) this.drawPdf()
    }

    fun drawPdf() {
        // showLog(String.format("drawPdf path:%s %s", this.path, this.page))

        if (this.path != null) {

            this.minZoom = this.minScale;
            this.maxZoom = this.maxScale;
            this.midZoom = (this.maxScale + this.minScale) / 2
            this.resetZoom()

            val configurator: Configurator

            if (path!!.startsWith("content://")) {
                val contentResolver = context.contentResolver
                val inputStream: InputStream?
                val uri = Uri.parse(this.path)
                try {
                    inputStream = contentResolver.openInputStream(uri)!!
                } catch (e: FileNotFoundException) {
                    throw RuntimeException(e.message)
                }
                configurator = this.fromStream(inputStream)
            } else {
                configurator = this.fromUri(getURI(this.path))
            }

            configurator.defaultPage(this.page - 1).swipeHorizontal(this.horizontal)
                .onPageChange(this).onLoad(this).onError(this).onDraw(this).onPageScroll(this)
                .spacing(this.spacing).password(this.password)
                .enableAntialiasing(this.enableAntialiasing).pageFitPolicy(this.fitPolicy)
                .pageSnap(this.pageSnap).autoSpacing(this.autoSpacing).pageFling(this.pageFling)
                .enableSwipe(!this.singlePage).enableDoubletap(!this.singlePage)
                .enableAnnotationRendering(this.enableAnnotationRendering).linkHandler(this)

            if (this.singlePage) {
                configurator.pages(this.page - 1)
                setTouchesEnabled(false)
            } else {
                configurator.onTap(this)
            }

            configurator.load()
        }
    }

    fun setPath(path: String?) {
        this.path = path
    }

    // page start from 1
    fun setPage(page: Int) {
        this.page = max(page.toDouble(), 1.0).toInt()
    }

    fun setScale(scale: Float) {
        this.scale = scale
    }

    fun setMinScale(minScale: Float) {
        this.minScale = minScale
    }

    fun setMaxScale(maxScale: Float) {
        this.maxScale = maxScale
    }

    fun setHorizontal(horizontal: Boolean) {
        this.horizontal = horizontal
    }

    fun setSpacing(spacing: Int) {
        this.spacing = spacing
    }

    fun setPassword(password: String) {
        this.password = password
    }

    fun setEnableAnnotationRendering(enableAnnotationRendering: Boolean) {
        this.enableAnnotationRendering = enableAnnotationRendering
    }

    fun setEnablePaging(enablePaging: Boolean) {
        if (enablePaging) {
            this.autoSpacing = true
            this.pageFling = true
            this.pageSnap = true
        } else {
            this.autoSpacing = false
            this.pageFling = false
            this.pageSnap = false
        }
    }

    fun setEnableAntialiasing(enableAntialiasing: Boolean) {
        this.enableAntialiasing = enableAntialiasing
    }

    fun setFitPolicy(fitPolicy: Int) {
        when (fitPolicy) {
            0 -> this.fitPolicy = FitPolicy.WIDTH
            1 -> this.fitPolicy = FitPolicy.HEIGHT
            2 -> {
                this.fitPolicy = FitPolicy.BOTH
            }

            else -> {
                this.fitPolicy = FitPolicy.BOTH
            }
        }
    }

    fun setSinglePage(singlePage: Boolean) {
        this.singlePage = singlePage
    }

    fun remove() {
        this.removeAllViews()
        if (this.parent != null) {
            (this.parent as ViewGroup).removeView(this)
        }
        this.recycle()
    }

    /**
     * @see [...](https://github.com/barteksc/AndroidPdfViewer/blob/master/android-pdf-viewer/src/main/java/com/github/barteksc/pdfviewer/link/DefaultLinkHandler.java)
     */
    override fun handleLinkEvent(event: LinkTapEvent) {
        val uri = event.link.uri
        val page = event.link.destPageIdx
        if (uri != null && uri.isNotEmpty()) {
            handleUri(uri)
        } else if (page != null) {
            handlePage(page)
        }
    }

    /**
     * @see [...](https://github.com/barteksc/AndroidPdfViewer/blob/master/android-pdf-viewer/src/main/java/com/github/barteksc/pdfviewer/link/DefaultLinkHandler.java)
     */
    private fun handleUri(uri: String) = sendEvent("linkPressed|$uri")

    /**
     * @see [...](https://github.com/barteksc/AndroidPdfViewer/blob/master/android-pdf-viewer/src/main/java/com/github/barteksc/pdfviewer/link/DefaultLinkHandler.java)
     */
    private fun handlePage(page: Int) = this.jumpTo(page)

    private fun showLog(message: String, logLevel: Int = 2, str: String = "") {
        when (logLevel) {
            0 -> {
                Log.e(TAG, message)
            }
            1 -> {
                Log.w(TAG, message)
            }
            2 -> {
                Log.i(TAG, message)
            }
            3 -> {
                Log.d(TAG, message)
            }
            else -> {
                Log.v(TAG, message)
            }
        }
        Log.i(TAG, str)
    }

    private fun getURI(uri: String?): Uri {
        val parsed = Uri.parse(uri)

        if (parsed.scheme == null || parsed.scheme!!.isEmpty()) {
            return Uri.fromFile(uri?.let { File(it) })
        }
        return parsed
    }

    private fun setTouchesEnabled(enabled: Boolean) = setTouchesEnabled(this, enabled)

    companion object {
        private const val TAG = "PdfView"

        @SuppressLint("ClickableViewAccessibility")
        private fun setTouchesEnabled(v: View, enabled: Boolean) {
            if (enabled) {
                v.setOnTouchListener(null)
            } else {
                v.setOnTouchListener { v, _ -> true }
            }

            if (v is ViewGroup) {
                val vg = v
                for (i in 0 until vg.childCount) {
                    val child = vg.getChildAt(i)
                    setTouchesEnabled(child, enabled)
                }
            }
        }
    }
}
