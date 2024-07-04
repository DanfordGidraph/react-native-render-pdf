/**
 * Copyright (c) 2024-present, Staxtech (@gidraphdanford.dev)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package org.staxtech.android.pdf

import com.facebook.infer.annotation.Assertions
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.RNPDFPdfViewManagerDelegate
import com.facebook.react.viewmanagers.RNPDFPdfViewManagerInterface

class PdfManager(private val mCallerContext: ReactApplicationContext) : SimpleViewManager<PdfView>(), RNPDFPdfViewManagerInterface<PdfView?> {
    private var mPdfView: PdfView? = null
    private var mDelegate: ViewManagerDelegate<PdfView> = RNPDFPdfViewManagerDelegate(this)

    override fun getDelegate(): ViewManagerDelegate<PdfView> {
        return mDelegate
    }

    override fun getName() = REACT_CLASS

    public override fun createViewInstance(context: ThemedReactContext): PdfView {
        mPdfView = PdfView(context, null, mCallerContext)
        return mPdfView as PdfView
    }

    override fun onDropViewInstance(pdfView: PdfView) {
        mPdfView = null
        pdfView.remove()
    }

    @ReactProp(name = "path")
     override fun setPath(pdfView: PdfView?, path: String?) {
        pdfView?.setPath(path)
    }

    // page start from 1
    @ReactProp(name = "page")
     override fun setPage(pdfView: PdfView?, page: Int) {
        pdfView?.setPage(page)
    }

    @ReactProp(name = "scale")
     override fun setScale(pdfView: PdfView?, scale: Float) {
        pdfView?.setScale(scale)
    }

    @ReactProp(name = "minScale")
     override fun setMinScale(pdfView: PdfView?, minScale: Float) {
        pdfView?.setMinScale(minScale)
    }

    @ReactProp(name = "maxScale")
     override fun setMaxScale(pdfView: PdfView?, maxScale: Float) {
        pdfView?.setMaxScale(maxScale)
    }

    @ReactProp(name = "horizontal")
     override fun setHorizontal(pdfView: PdfView?, horizontal: Boolean) {
        pdfView?.setHorizontal(horizontal)
    }

     override fun setShowsHorizontalScrollIndicator(view: PdfView?, value: Boolean) {
        // NOOP on Android
    }

     override fun setShowsVerticalScrollIndicator(view: PdfView?, value: Boolean) {
        // NOOP on Android
    }

    @ReactProp(name = "spacing")
     override fun setSpacing(pdfView: PdfView?, spacing: Int) {
        pdfView?.setSpacing(spacing)
    }

    @ReactProp(name = "password")
     override fun setPassword(pdfView: PdfView?, password: String?) {
        pdfView?.setPassword(password!!)
    }

    @ReactProp(name = "enableAntialiasing")
     override fun setEnableAntialiasing(pdfView: PdfView?, enableAntialiasing: Boolean) {
        pdfView?.setEnableAntialiasing(enableAntialiasing)
    }

    @ReactProp(name = "enableAnnotationRendering")
     override fun setEnableAnnotationRendering(
        pdfView: PdfView?,
        enableAnnotationRendering: Boolean
    ) {
        pdfView?.setEnableAnnotationRendering(enableAnnotationRendering)
    }

    @ReactProp(name = "enablePaging")
     override fun setEnablePaging(pdfView: PdfView?, enablePaging: Boolean) {
        pdfView?.setEnablePaging(enablePaging)
    }

     override fun setEnableRTL(view: PdfView?, value: Boolean) {
        // NOOP on Android
    }

    @ReactProp(name = "fitPolicy")
     override fun setFitPolicy(pdfView: PdfView?, fitPolicy: Int) {
        pdfView?.setFitPolicy(fitPolicy)
    }

    @ReactProp(name = "singlePage")
     override fun setSinglePage(pdfView: PdfView?, singlePage: Boolean) {
        pdfView?.setSinglePage(singlePage)
    }

    // It seems funny, but this method is called through delegate on Paper, but on Fabric we need to
    // use `receiveCommand` method and call this one there
    override fun setNativePage(pdfView: PdfView?, page: Int) {
        pdfView?.setPage(page)
    }

    override fun receiveCommand(root: PdfView, commandId: String, args: ReadableArray?) {
        Assertions.assertNotNull(root)
        if ("setNativePage" == commandId) {
            Assertions.assertNotNull(args)
            checkNotNull(args)
            setNativePage(root, args.getInt(0))
        }
    }

    public override fun onAfterUpdateTransaction(pdfView: PdfView) {
        super.onAfterUpdateTransaction(pdfView)
        pdfView.drawPdf()
    }

    companion object {
        const val REACT_CLASS: String = "RNPDFPdfView"
    }
}
