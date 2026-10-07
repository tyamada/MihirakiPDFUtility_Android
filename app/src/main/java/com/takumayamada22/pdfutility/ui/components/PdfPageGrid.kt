package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
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
import com.takumayamada22.pdfutility.ui.PageState
import com.takumayamada22.pdfutility.ui.PdfUiState
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.ThumbnailSize
import kotlin.math.roundToInt

@Composable
fun PdfPageGrid(
    uiState: PdfUiState,
    viewModel: PdfViewModel,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggedPageId by remember { mutableStateOf<String?>(null) }
    var dragTargetIndex by remember { mutableStateOf<Int?>(null) }
    var draggingOffset by remember { mutableStateOf(Offset.Zero) }

    val currentUiState by rememberUpdatedState(uiState)
    val currentViewModel by rememberUpdatedState(viewModel)

    val minSize = when (uiState.thumbnailSize) {
        ThumbnailSize.S -> 75.dp
        ThumbnailSize.M -> 150.dp
        ThumbnailSize.L -> 300.dp
    }
    val mihirakiHeight = when (uiState.thumbnailSize) {
        ThumbnailSize.S -> 125.dp
        ThumbnailSize.M -> 250.dp
        ThumbnailSize.L -> 500.dp
    }

    if (uiState.isMihirakiView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxSize()
        ) {
            val pairs = if (uiState.showCover && uiState.pages.isNotEmpty()) {
                val firstPage = uiState.pages.take(1)
                val remainingPages = uiState.pages.drop(1)
                listOf(firstPage) + remainingPages.chunked(2)
            } else {
                uiState.pages.chunked(2)
            }
            items(pairs.size) { pairIndex ->
                val pair = pairs[pairIndex]
                val rowPages = if (uiState.isRtl) pair.reversed() else pair
                Row(
                    modifier = Modifier.fillMaxWidth().height(mihirakiHeight),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (uiState.isRtl && pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
                    }
                    
                    rowPages.forEach { page ->
                        val index = uiState.pages.indexOfFirst { it.id == page.id }
                        Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                            PageThumbnail(
                                page = page,
                                isSelected = uiState.selectedIndices.contains(index),
                                isDropTarget = false,
                                onClick = { viewModel.toggleSelection(index) },
                                onDoubleTap = { viewModel.openPreview(index) }
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
            columns = GridCells.Adaptive(minSize = minSize),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(uiState.pages, key = { page -> page.id }) { page ->
                val currentIndex = uiState.pages.indexOfFirst { it.id == page.id }
                val isDragging = draggedItemIndex == currentIndex
                val isDropTarget = dragTargetIndex == currentIndex
                PageThumbnail(
                    page = page,
                    isSelected = uiState.selectedIndices.contains(currentIndex),
                    isDropTarget = isDropTarget,
                    onClick = { viewModel.toggleSelection(currentIndex) },
                    onDoubleTap = { viewModel.openPreview(currentIndex) },
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { 
                                    val start = currentUiState.pages.indexOfFirst { it.id == page.id }
                                    draggedItemIndex = start
                                    draggedPageId = page.id
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    draggingOffset += dragAmount

                                    val start = currentUiState.pages.indexOfFirst { it.id == draggedPageId }
                                    if (start != -1) {
                                        val layoutInfo = gridState.layoutInfo
                                        val visibleItems = layoutInfo.visibleItemsInfo
                                        val draggedItemInfo = visibleItems.find { item -> item.index == start }
                                        
                                        if (draggedItemInfo != null) {
                                            val currentCenter = Offset(
                                                draggedItemInfo.offset.x + draggedItemInfo.size.width / 2f + draggingOffset.x,
                                                draggedItemInfo.offset.y + draggedItemInfo.size.height / 2f + draggingOffset.y
                                            )
                                            
                                            val targetItem = visibleItems.find { item ->
                                                currentCenter.x in item.offset.x.toFloat()..(item.offset.x + item.size.width).toFloat() &&
                                                currentCenter.y in item.offset.y.toFloat()..(item.offset.y + item.size.height).toFloat()
                                            }
                                            
                                            dragTargetIndex = targetItem?.index
                                        }
                                    }
                                },
                                onDragEnd = {
                                    val pageId = draggedPageId
                                    if (pageId != null) {
                                        val start = currentUiState.pages.indexOfFirst { it.id == pageId }
                                        if (start != -1) {
                                            val layoutInfo = gridState.layoutInfo
                                            val visibleItems = layoutInfo.visibleItemsInfo
                                            val draggedItemInfo = visibleItems.find { item -> item.index == start }
                                            
                                            if (draggedItemInfo != null) {
                                                val currentCenter = Offset(
                                                    draggedItemInfo.offset.x + draggedItemInfo.size.width / 2f + draggingOffset.x,
                                                    draggedItemInfo.offset.y + draggedItemInfo.size.height / 2f + draggingOffset.y
                                                )
                                                
                                                val targetItem = visibleItems.find { item ->
                                                    currentCenter.x in item.offset.x.toFloat()..(item.offset.x + item.size.width).toFloat() &&
                                                    currentCenter.y in item.offset.y.toFloat()..(item.offset.y + item.size.height).toFloat()
                                                }
                                                
                                                if (targetItem != null && targetItem.index != start) {
                                                    if (currentUiState.selectedIndices.contains(start) && currentUiState.selectedIndices.size > 1) {
                                                        currentViewModel.moveSelectedPages(start, targetItem.index)
                                                    } else {
                                                        currentViewModel.movePage(start, targetItem.index)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    draggedItemIndex = null
                                    draggedPageId = null
                                    dragTargetIndex = null
                                    draggingOffset = Offset.Zero
                                },
                                onDragCancel = {
                                    draggedItemIndex = null
                                    draggedPageId = null
                                    dragTargetIndex = null
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
    isDropTarget: Boolean,
    onClick: () -> Unit,
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.7f)
            .border(
                width = if (isDropTarget) 6.dp else if (isSelected) 4.dp else 1.dp,
                color = if (isDropTarget) MaterialTheme.colorScheme.tertiary else if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onDoubleTap = { onDoubleTap() }
                )
            }
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
