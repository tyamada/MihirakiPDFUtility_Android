package com.takumayamada22.pdfutility.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.R
import com.takumayamada22.pdfutility.ui.PdfViewModel
import kotlinx.coroutines.launch

internal data class TutorialPageData(
    val icon: ImageVector,
    val titleResId: Int,
    val descResId: Int,
)

@Composable
fun TutorialScreen(viewModel: PdfViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val pages = listOf(
        TutorialPageData(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            titleResId = R.string.tutorial_title_1,
            descResId = R.string.tutorial_desc_1,
        ),
        TutorialPageData(
            icon = Icons.Default.FileOpen,
            titleResId = R.string.tutorial_title_2,
            descResId = R.string.tutorial_desc_2,
        ),
        TutorialPageData(
            icon = Icons.Default.GridView,
            titleResId = R.string.tutorial_title_3,
            descResId = R.string.tutorial_desc_3,
        ),
        TutorialPageData(
            icon = Icons.Default.Settings,
            titleResId = R.string.tutorial_title_4,
            descResId = R.string.tutorial_desc_4,
        ),
        TutorialPageData(
            icon = Icons.Default.Save,
            titleResId = R.string.tutorial_title_5,
            descResId = R.string.tutorial_desc_5,
        ),
    )

    val pagerState = rememberPagerState { pages.size }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.dismissTutorial() }) {
                    Text(stringResource(R.string.tutorial_skip))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal Pager for slides
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = page.icon,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(page.titleResId),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(page.descResId),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // On the last slide, add a direct "Try Sample PDF" button
                    if ((index == pages.size - 1)) {
                        Spacer(modifier = Modifier.height(24.dp))
                        FilledTonalButton(
                            onClick = { viewModel.trySamplePdfFromTutorial() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.tutorial_try_sample))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Indicator dots and Next / Finish button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(color)
                                .size(if (pagerState.currentPage == iteration) 10.dp else 8.dp)
                        )
                    }
                }

                // Action button (Next / Finish)
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            viewModel.dismissTutorial()
                        }
                    }
                ) {
                    Text(
                        text = if (pagerState.currentPage < pages.size - 1) {
                            stringResource(R.string.tutorial_next)
                        } else {
                            stringResource(R.string.tutorial_finish)
                        }
                    )
                }
            }
        }
    }
}
