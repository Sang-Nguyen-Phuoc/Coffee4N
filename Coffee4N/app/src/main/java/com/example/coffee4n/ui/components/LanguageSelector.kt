package com.example.coffee4n.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.coffee4n.utils.LanguageManager

@Composable
fun LanguageSelector(
    modifier: Modifier = Modifier,
    onLanguageSelected: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentLanguageCode = LanguageManager.getSavedLanguage(context)
    val currentLanguage = LanguageManager.getAvailableLanguages()
        .find { it.code == currentLanguageCode }
        ?: LanguageManager.getAvailableLanguages()[0]

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Language",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${currentLanguage.flag} ${currentLanguage.code.uppercase()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LanguageManager.getAvailableLanguages().forEach { language ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(language.flag)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(language.name)
                        }
                    },
                    onClick = {
                        LanguageManager.setLocale(context, language.code)
                        onLanguageSelected(language.code)
                        expanded = false
                    }
                )
            }
        }
    }
}