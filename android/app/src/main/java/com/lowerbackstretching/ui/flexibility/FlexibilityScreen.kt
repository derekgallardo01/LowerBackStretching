package com.lowerbackstretching.ui.flexibility

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.core.FlexibilityDelta
import com.lowerbackstretching.data.db.FlexibilityTestEntity
import com.lowerbackstretching.core.flexibilityDelta
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.ScreenHeader
import com.lowerbackstretching.ui.components.SectionHeader
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlexibilityScreen(onBack: () -> Unit, vm: AppViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val history by vm.flexibility.all().collectAsState(initial = emptyList())

    var sitAndReach by remember { mutableStateOf("") }
    var toeTouch by remember { mutableStateOf("") }
    var shoulderReach by remember { mutableStateOf("") }

    fun save() {
        scope.launch {
            vm.flexibility.record(
                sitAndReachCm = sitAndReach.toFloatOrNull(),
                toeTouchCm = toeTouch.toFloatOrNull(),
                shoulderReachCm = shoulderReach.toFloatOrNull(),
            )
            sitAndReach = ""
            toeTouch = ""
            shoulderReach = ""
        }
    }

    val anyValueEntered = listOf(sitAndReach, toeTouch, shoulderReach)
        .any { it.toFloatOrNull() != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flexibility self-test") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ScreenHeader("New measurement") }
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MeasurementField("Sit & reach (cm)", sitAndReach) { sitAndReach = it }
                        MeasurementField("Toe touch (cm past toes)", toeTouch) { toeTouch = it }
                        MeasurementField("Shoulder reach (cm)", shoulderReach) { shoulderReach = it }
                        Button(
                            onClick = ::save,
                            enabled = anyValueEntered,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Save measurement")
                        }
                    }
                }
            }

            if (history.isNotEmpty()) {
                val latest = history.first()
                val previous = history.drop(1).firstOrNull()
                item { SectionHeader("History") }
                item {
                    val delta = remember(latest, previous) { flexibilityDelta(latest, previous) }
                    LatestCard(latest = latest, delta = delta)
                }
                if (history.size > 1) {
                    item { SectionHeader("Earlier", topPadding = 8.dp) }
                    items(history.drop(1)) { row -> HistoryRow(row) }
                }
            }
        }
    }
}

@Composable
private fun MeasurementField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            // Allow only numeric + decimal characters.
            if (input.all { it.isDigit() || it == '.' || it == '-' }) onChange(input)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun LatestCard(latest: FlexibilityTestEntity, delta: FlexibilityDelta) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(formatTime(latest.recordedAtEpochMillis), style = MaterialTheme.typography.titleMedium)
            MeasurementRow("Sit & reach", latest.sitAndReachCm, delta.sitAndReachCm)
            MeasurementRow("Toe touch", latest.toeTouchCm, delta.toeTouchCm)
            MeasurementRow("Shoulder reach", latest.shoulderReachCm, delta.shoulderReachCm)
        }
    }
}

@Composable
private fun MeasurementRow(label: String, value: Float?, delta: Float?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            buildString {
                append(value?.let { "%.1f cm".format(it) } ?: "—")
                if (delta != null) append("   (${formatDelta(delta)} cm)")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if ((delta ?: 0f) > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HistoryRow(entry: FlexibilityTestEntity) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(formatTime(entry.recordedAtEpochMillis), style = MaterialTheme.typography.titleSmall)
            Text(
                listOfNotNull(
                    entry.sitAndReachCm?.let { "Sit&reach %.1f".format(it) },
                    entry.toeTouchCm?.let { "Toes %.1f".format(it) },
                    entry.shoulderReachCm?.let { "Shoulder %.1f".format(it) },
                ).joinToString(" · ").ifEmpty { "(no measurements)" },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun formatTime(epochMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))

private fun formatDelta(d: Float): String =
    if (d >= 0) "+%.1f".format(d) else "%.1f".format(d)
