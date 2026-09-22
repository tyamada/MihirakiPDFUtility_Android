package com.takumayamada22.pdfutility

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class PdfApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(applicationContext)
    }
}
