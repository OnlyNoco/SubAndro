package com.fansubcreator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fansubcreator.translation.Language
import com.fansubcreator.translation.TranslationManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationPanel(
    originalTexts: List<String>,
    translatedTexts: List<String>,
    onTranslatedTextsUpdated: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val translationManager = remember { TranslationManager() }
    val scope = rememberCoroutineScope()
    
    var fromLanguage by remember { mutableStateOf(Language("ja", "Japanese", "🇯🇵")) }
    var toLanguage by remember { mutableStateOf(Language("en", "English", "🇺🇸")) }
    var useOnlineTranslation by remember { mutableStateOf(true) }
    var isTranslating by remember { mutableStateOf(false) }
    var translationProgress by remember { mutableStateOf(0 to 0) }
    
    val supportedLanguages = remember { translationManager.getSupportedLanguages() }
    
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Translation Panel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = useOnlineTranslation,
                        onCheckedChange = { useOnlineTranslation = it }
                    )
                    Text(
                        text = if (useOnlineTranslation) "Online" else "Offline",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Language selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // From language
                var fromExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = !fromExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "${fromLanguage.flag} ${fromLanguage.name}",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("From") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        supportedLanguages.forEach { language ->
                            DropdownMenuItem(
                                text = { Text("${language.flag} ${language.name}") },
                                onClick = {
                                    fromLanguage = language
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Swap button
                IconButton(
                    onClick = {
                        val temp = fromLanguage
                        fromLanguage = toLanguage
                        toLanguage = temp
                    }
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Swap Languages")
                }
                
                // To language
                var toExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = !toExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "${toLanguage.flag} ${toLanguage.name}",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("To") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        supportedLanguages.forEach { language ->
                            DropdownMenuItem(
                                text = { Text("${language.flag} ${language.name}") },
                                onClick = {
                                    toLanguage = language
                                    toExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Translation controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isTranslating = true
                            translationProgress = 0 to originalTexts.size
                            
                            val results = translationManager.batchTranslate(
                                texts = originalTexts,
                                fromLang = fromLanguage.code,
                                toLang = toLanguage.code,
                                useOnline = useOnlineTranslation,
                                onProgress = { current, total ->
                                    translationProgress = current to total
                                }
                            )
                            
                            val newTranslations = results.map { result ->
                                result.getOrElse { "Translation failed" }
                            }
                            
                            onTranslatedTextsUpdated(newTranslations)
                            isTranslating = false
                        }
                    },
                    enabled = !isTranslating && originalTexts.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isTranslating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Translating...")
                    } else {
                        Icon(Icons.Default.Translate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Translate All")
                    }
                }
                
                OutlinedButton(
                    onClick = {
                        onTranslatedTextsUpdated(emptyList())
                    },
                    enabled = translatedTexts.isNotEmpty()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear")
                }
            }
            
            // Progress indicator
            if (isTranslating) {
                Column {
                    Text(
                        text = "Progress: ${translationProgress.first}/${translationProgress.second}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    LinearProgressIndicator(
                        progress = if (translationProgress.second > 0) {
                            translationProgress.first.toFloat() / translationProgress.second
                        } else 0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Translation pairs
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(originalTexts.size) { index ->
                    TranslationItem(
                        originalText = originalTexts.getOrNull(index) ?: "",
                        translatedText = translatedTexts.getOrNull(index) ?: "",
                        onTranslatedTextChanged = { newTranslation ->
                            val updatedList = translatedTexts.toMutableList()
                            if (index < updatedList.size) {
                                updatedList[index] = newTranslation
                            } else {
                                // Fill gaps and add new translation
                                while (updatedList.size <= index) {
                                    updatedList.add("")
                                }
                                updatedList[index] = newTranslation
                            }
                            onTranslatedTextsUpdated(updatedList)
                        },
                        onSingleTranslate = {
                            scope.launch {
                                val result = translationManager.translate(
                                    text = originalTexts[index],
                                    fromLang = fromLanguage.code,
                                    toLang = toLanguage.code,
                                    useOnline = useOnlineTranslation
                                )
                                
                                result.onSuccess { translation ->
                                    val updatedList = translatedTexts.toMutableList()
                                    while (updatedList.size <= index) {
                                        updatedList.add("")
                                    }
                                    updatedList[index] = translation
                                    onTranslatedTextsUpdated(updatedList)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TranslationItem(
    originalText: String,
    translatedText: String,
    onTranslatedTextChanged: (String) -> Unit,
    onSingleTranslate: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Original text
            Text(
                text = "Original:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = originalText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Divider()
            
            // Translation controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Translation:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                IconButton(
                    onClick = onSingleTranslate,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Translate,
                        contentDescription = "Translate",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Translated text editor
            OutlinedTextField(
                value = translatedText,
                onValueChange = onTranslatedTextChanged,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text("Enter translation...") }
            )
        }
    }
}