package com.bhashabuddy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Sky = Color(0xFFEAF5FF)
private val Purple = Color(0xFF7866D8)
private val Ink = Color(0xFF20243A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BhashaBuddyApp() }
    }
}

@Composable
fun BhashaBuddyApp() {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var started by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var translated by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(listOf<String>()) }
    var dark by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (dark) darkColorScheme(primary = Color(0xFFB9AEFF))
                      else lightColorScheme(primary = Purple, background = Sky)
    ) {
        Surface(Modifier.fillMaxSize(), color = if (dark) Color(0xFF101421) else Sky) {
            if (!started) {
                WelcomeScreen(name, { name = it }, age, { age = it }) {
                    if (name.isNotBlank() && age.toIntOrNull() != null) started = true
                }
            } else {
                TranslatorScreen(
                    name = name, input = input, onInput = { input = it },
                    translated = translated, historyCount = history.size,
                    dark = dark, onDark = { dark = it },
                    onTranslate = {
                        // Step 1 prototype: connect a translation service in a later step.
                        translated = "Translation service will be connected in the next step."
                        if (input.isNotBlank()) history = listOf(input) + history
                    }
                )
            }
        }
    }
}

@Composable
private fun WelcomeScreen(name: String, onName: (String) -> Unit, age: String,
                          onAge: (String) -> Unit, onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text("🌐", fontSize = 54.sp)
        Text("Bhasha Buddy", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("Your friendly language companion", color = Color.Gray)
        Spacer(Modifier.height(30.dp))
        Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Let's get to know you", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                OutlinedTextField(value = name, onValueChange = onName, label = { Text("Your name") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = age, onValueChange = onAge, label = { Text("Your age") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                    Text("Let's Start")
                }
                Text("No account needed • Just start translating", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun TranslatorScreen(name: String, input: String, onInput: (String) -> Unit,
                              translated: String, historyCount: Int, dark: Boolean,
                              onDark: (Boolean) -> Unit, onTranslate: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Bhasha Buddy", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Hello, $name 👋", color = Color.Gray)
            }
            IconButton(onClick = { onDark(!dark) }) { Text(if (dark) "☀️" else "🌙") }
            Icon(Icons.Default.History, contentDescription = "History")
            Text("$historyCount")
        }
        Text("Quick Translate", fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Card(shape = RoundedCornerShape(22.dp)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🇮🇳  Hindi", fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.SwapHoriz, contentDescription = "Swap languages", tint = Purple)
                Text("🇬🇧  English", fontWeight = FontWeight.SemiBold)
            }
        }
        Card(shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(14.dp)) {
                OutlinedTextField(value = input, onValueChange = onInput,
                    placeholder = { Text("Type or paste text here…") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 130.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = {}) { Icon(Icons.Default.Mic, contentDescription = "Voice input") }
                    IconButton(onClick = {}) { Icon(Icons.Default.ContentCopy, contentDescription = "Copy") }
                }
            }
        }
        Button(onClick = onTranslate, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("Translate") }
        Card(shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Translation", color = Purple, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(translated.ifBlank { "Your translated text will appear here." })
            }
        }
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE9FF))) {
            Column(Modifier.padding(16.dp)) {
                Text("✨ Quick AI Support", color = Ink, fontWeight = FontWeight.Bold)
                Text("Ask for meanings, examples and sentence help.", color = Ink, fontSize = 13.sp)
            }
        }
        Text("🔥 Daily streak: starts when you use the app each day", fontSize = 13.sp, color = Color.Gray)
    }
}
