package jp.mihiraki.pdfutility.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.ui.input.key.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: PdfViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val gridState = rememberLazyGridState()
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggingOffset by remember { mutableStateOf(Offset.Zero) }

    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.loadPdf(it) }
    }
    val appendLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.appendPdf(it) }
    }
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { viewModel.savePdf(it) }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.onKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown && event.isCtrlPressed) {
                when (event.key) {
                    Key.O -> {
                        openLauncher.launch("application/pdf")
                        true
                    }
                    Key.S -> {
                        saveLauncher.launch("edited_${uiState.fileName ?: "document"}.pdf")
                        true
                    }
                    Key.A -> {
                        viewModel.selectAll()
                        true
                    }
                    else -> false
                }
            } else if (event.type == KeyEventType.KeyDown && event.key == Key.Delete) {
                if (uiState.selectedIndices.isNotEmpty()) {
                    showDeleteConfirmation = true
                }
                true
            } else {
                false
            }
        },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.fileName ?: "Mihiraki PDF Utility",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                actions = {
                    if (uiState.selectedIndices.isEmpty()) {
                        IconButton(onClick = { openLauncher.launch("application/pdf") }) {
                            Icon(Icons.Default.FileOpen, contentDescription = "Open")
                        }
                        if (uiState.pages.isNotEmpty()) {
                            IconButton(onClick = { appendLauncher.launch("application/pdf") }) {
                                Icon(Icons.Default.Add, contentDescription = "Add PDF")
                            }
                            if (uiState.isMihirakiView) {
                                IconButton(onClick = { viewModel.toggleRtl() }) {
                                    Icon(
                                        if (uiState.isRtl) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Toggle RTL"
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.toggleMihirakiView() }) {
                                Icon(
                                    if (uiState.isMihirakiView) Icons.Default.GridView else Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = "Toggle View"
                                )
                            }
                            IconButton(onClick = { saveLauncher.launch("edited_${uiState.fileName ?: "document"}.pdf") }) {
                                Icon(Icons.Default.Save, contentDescription = "Save")
                            }
                        }
                    } else {
                        IconButton(onClick = { viewModel.selectAll() }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                        }
                        IconButton(onClick = { viewModel.invertSelection() }) {
                            Icon(Icons.Default.Flip, contentDescription = "Invert Selection")
                        }
                        IconButton(onClick = { viewModel.rotateSelected(90) }) {
                            Icon(Icons.Default.RotateRight, contentDescription = "Rotate Right")
                        }
                        IconButton(onClick = { viewModel.insertBlankAfterSelected() }) {
                            Icon(Icons.Default.AddBox, contentDescription = "Insert Blank")
                        }
                        IconButton(onClick = { viewModel.moveSelected(-1) }) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                        }
                        IconButton(onClick = { viewModel.moveSelected(1) }) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                        }
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.pages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No PDF loaded. Tap the open icon to start.")
                }
            } else {
                if (uiState.isMihirakiView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                            onDragStart = { offset ->
                                                draggedItemIndex = index
                                                draggingOffset = Offset.Zero
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                draggingOffset += dragAmount
                                                
                                                // Find target index
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
                                                        draggingOffset = Offset.Zero // Reset offset as item has jumped
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

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if (uiState.showPasswordDialog) {
        var password by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.cancelPasswordDialog() },
            title = { Text("Password Required") },
            text = {
                Column {
                    Text("This PDF is password protected.")
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.handlePassword(password)
                }) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelPasswordDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Pages") },
            text = { Text("Are you sure you want to delete the ${uiState.selectedIndices.size} selected pages?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSelected()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
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
            .aspectRatio(0.7f) // A4-ish
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
