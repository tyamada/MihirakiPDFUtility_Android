package com.takumayamada22.pdfutility
import com.takumayamada22.pdfutility.ui.components.useWideStartLayout
import org.junit.Assert.*
import org.junit.Test
class StartLayoutTest {
    @Test fun availableSpaceAndTextSizeDecideLayout() {
        assertFalse(useWideStartLayout(360f, 640f, 1f))
        assertTrue(useWideStartLayout(800f, 360f, 1f))
        assertFalse(useWideStartLayout(500f, 300f, 1f)) // split window
        assertFalse(useWideStartLayout(800f, 360f, 2f)) // large text needs a full-width column
        assertFalse(useWideStartLayout(800f, 1000f, 1f)) // tall window
        assertTrue(useWideStartLayout(1200f, 600f, 2f))
    }
}
