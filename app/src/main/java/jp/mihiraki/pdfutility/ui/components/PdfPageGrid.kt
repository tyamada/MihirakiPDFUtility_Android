package jp.mihiraki.pdfutility.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import jp.mihiraki.pdfutility.ui.PageState
import jp.mihiraki.pdfutility.ui.PdfUiState
import jp.mihiraki.pdfutility.ui.PdfViewModel
import kotlin.math.roundToInt

@Composable
fun PdfPageGrid(
    uiState: PdfUiState,
    viewModel: PdfViewModel,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggingOffset by remember { mutableStateOf(Offset.Zero) }

    if (uiState.isMihirakiView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxSize()
        ) {
            val pairs = uiState.pages.chunked(2)
            items(pairs.size) { pairIndex ->
                val pair = pairs[pairIndex]
                val rowPages = if (uiState.isRtl) pair.reversed() else pair
                Row(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (uiState.isRtl && pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
                    }
                    
                    rowPages.forEach { page ->
                        val index = uiState.pages.indexOf(page)
                        Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                            PageThumbnail(
                                page = page,
                                isSelected = uiState.selectedIndices.contains(index),
                                onClick = { viewModel.toggleSelection(index) }
                            )
                        }
                    }
                    
                    if (!uiState.isRtl && pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
                    }
                }
            }
        }
    } else {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxSize()
        ) {
            itemsIndexed(uiState.pages, key = { _, page -> page.id }) { index, page ->
                val isDragging = draggedItemIndex == index
                PageThumbnail(
                    page = page,
                    isSelected = uiState.selectedIndices.contains(index),
                    onClick = { viewModel.toggleSelection(index) },
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { draggedItemIndex = index },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    draggingOffset += dragAmount
                                    
                                    val layoutInfo = gridState.layoutInfo
                                    val visibleItems = layoutInfo.visibleItemsInfo
                                    val draggedItemInfo = visibleItems.find { it.index == draggedItemIndex }
                                    
                                    if (draggedItemInfo != null) {
                                        val currentCenter = Offset(
                                            draggedItemInfo.offset.x + draggedItemInfo.size.width / 2f + draggingOffset.x,
                                            draggedItemInfo.offset.y + draggedItemInfo.size.height / 2f + draggingOffset.y
                                        )
                                        
                                        val targetItem = visibleItems.find { item ->
                                            currentCenter.x in item.offset.x.toFloat()..(item.offset.x + item.size.width).toFloat() &&
                                            currentCenter.y in item.offset.y.toFloat()..(item.offset.y + item.size.height).toFloat()
                                        }
                                        
                                        if (targetItem != null && targetItem.index != draggedItemIndex) {
                                            viewModel.movePage(draggedItemIndex!!, targetItem.index)
                                            draggedItemIndex = targetItem.index
                                            draggingOffset = Offset.Zero
                                        }
                                    }
                                },
                                onDragEnd = {
                                    draggedItemIndex = null
                                    draggingOffset = Offset.Zero
                                },
                                onDragCancel = {
                                    draggedItemIndex = null
                                    draggingOffset = Offset.Zero
                                }
                            )
                        }
                        .offset { 
                            if (isDragging) {
                                IntOffset(draggingOffset.x.roundToInt(), draggingOffset.y.roundToInt())
                            } else {
                                IntOffset.Zero
                            }
                        }
                        .zIndex(if (isDragging) 1f else 0f)
                )
            }
        }
    }
}

@Composable
fun PageThumbnail(
    page: PageState, 
    isSelected: Boolean, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.7f)
            .border(
                width = if (isSelected) 4.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
            )
            .clickable(onClick = onClick)
    ) {
        page.thumbnail?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } ?: Box(modifier = Modifier.fillMaxSize().background(Color.White))
        
        Text(
            text = "${page.originalIndex + 1}",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
