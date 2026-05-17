package com.lowerbackstretching.ui.share

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.share.buildRoutineLink
import com.lowerbackstretching.share.renderQrBitmap
import com.lowerbackstretching.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareRoutineScreen(
    routineId: Long,
    onBack: () -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val routine by vm.customRoutines.all().collectAsState(initial = emptyList())
    val target = routine.firstOrNull { it.id == routineId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share routine") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
    ) { inner ->
        if (target == null) {
            Box(Modifier.padding(inner).padding(16.dp)) {
                Text("Routine no longer exists.")
            }
            return@Scaffold
        }
        val link = remember(target.name, target.stretchIdsCsv) {
            buildRoutineLink(target.name, target.stretchIds)
        }
        val qrBitmap = remember(link) { renderQrBitmap(link, sizePx = 512) }

        Column(
            modifier = Modifier.padding(inner),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(PaddingValues(horizontal = 16.dp, vertical = 16.dp)),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    target.name,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "${target.stretchIds.size} stretches",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                if (qrBitmap != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR code for $link",
                            modifier = Modifier.size(240.dp).padding(12.dp),
                        )
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        link,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(link)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null)
                    Text("  Copy link")
                }
                Button(
                    onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, target.name)
                            putExtra(Intent.EXTRA_TEXT, link)
                        }
                        ctx.startActivity(Intent.createChooser(send, "Share routine"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Text("  Share…")
                }
            }
        }
    }
}
