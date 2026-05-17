package com.lowerbackstretching.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.data.model.GlossaryEntry
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryScreen(onBack: () -> Unit, vm: AppViewModel = viewModel()) {
    val glossary = remember { vm.content.glossary }
    var query by remember { mutableStateOf("") }

    val filtered = remember(query, glossary) {
        if (query.isBlank()) glossary
        else glossary.filter {
            it.term.contains(query, ignoreCase = true)
                || it.definition.contains(query, ignoreCase = true)
        }
    }
    val grouped = remember(filtered) {
        filtered.groupBy { it.category }.toSortedMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Glossary") },
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search terms") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            grouped.forEach { (category, entries) ->
                item { SectionHeader(category.replaceFirstChar { it.titlecase() }) }
                items(entries, key = { it.term }) { entry -> EntryCard(entry) }
            }
            if (filtered.isEmpty()) {
                item {
                    Text(
                        "No matches.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryCard(entry: GlossaryEntry) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(entry.term, style = MaterialTheme.typography.titleMedium)
            Text(entry.definition, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
