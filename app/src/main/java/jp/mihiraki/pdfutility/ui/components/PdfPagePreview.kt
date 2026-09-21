package jp.mihiraki.pdfutility.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import jp.mihiraki.pdfutility.ui.PdfUiState
import jp.mihiraki.pdfutility.ui.PdfViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PdfPagePreview(
    uiState: PdfUiState,
    viewModel: PdfViewModel,
    modifier: Modifier = Modifier
) {
    val startIndex = uiState.previewPageIndex ?: return
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { uiState.pages.size })

    BackHandler {
        viewModel.closePreview()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            ZoomableImage(
                bitmapProvider = { viewModel.getHighResThumbnail(pageIndex) }
            )
        }

        TopAppBar(
            title = {
                Text(
                    "Page ${pagerState.currentPage + 1} / ${uiState.pages.size}",
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.closePreview() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.5f),
                titleContentColor = Color.White
            )
        )
    }
}

@Composable
fun ZoomableImage(bitmapProvider: suspend () -> Bitmap?) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(bitmapProvider) {
        withContext(Dispatchers.IO) {
            bitmap = bitmapProvider()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale > 1f) {
                        offset += pan
                    } else {
                        offset = Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )
        } ?: CircularProgressIndicator(color = Color.White)
    }
}
