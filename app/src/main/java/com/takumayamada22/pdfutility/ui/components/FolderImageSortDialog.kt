package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.domain.ImageFileItem
import com.takumayamada22.pdfutility.domain.ImageSortField
import com.takumayamada22.pdfutility.domain.ImageSortOrder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderImageSortDialog(
    images: List<ImageFileItem>,
    sortField: ImageSortField,
    sortOrder: ImageSortOrder,
    onSortChanged: (ImageSortField, ImageSortOrder) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.folder_images_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                Text(
                    text = stringResource(R.string.sort_options_label),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = sortField == ImageSortField.NAME,
                        onClick = { onSortChanged(ImageSortField.NAME, sortOrder) },
                        label = { Text(stringResource(R.string.sort_by_name)) }
                    )
                    FilterChip(
                        selected = sortField == ImageSortField.CREATION_DATE,
                        onClick = { onSortChanged(ImageSortField.CREATION_DATE, sortOrder) },
                        label = { Text(stringResource(R.string.sort_by_creation)) }
                    )
                    FilterChip(
                        selected = sortField == ImageSortField.MODIFIED_DATE,
                        onClick = { onSortChanged(ImageSortField.MODIFIED_DATE, sortOrder) },
                        label = { Text(stringResource(R.string.sort_by_modified)) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = sortOrder == ImageSortOrder.ASCENDING,
                        onClick = { onSortChanged(sortField, ImageSortOrder.ASCENDING) },
                        label = { Text(stringResource(R.string.sort_ascending)) }
                    )
                    FilterChip(
                        selected = sortOrder == ImageSortOrder.DESCENDING,
                        onClick = { onSortChanged(sortField, ImageSortOrder.DESCENDING) },
                        label = { Text(stringResource(R.string.sort_descending)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${stringResource(R.string.image_count_label)}: ${images.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(images) { image ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = image.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val dateStr = dateFormatter.format(Date(if (sortField == ImageSortField.CREATION_DATE) image.creationTime else image.lastModifiedTime))
                                Text(
                                    text = "${if (sortField == ImageSortField.CREATION_DATE) stringResource(R.string.sort_by_creation) else stringResource(R.string.sort_by_modified)}: $dateStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.btn_create_pdf_from_images))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
