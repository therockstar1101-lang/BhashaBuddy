package com.bhashabuddy.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Sky = Color(0xFFEAF5FF)
private val Purple = Color(0xFF8067D8)
private val PalePurple = Color(0xFFF0ECFF)
private val Ink = Color(0xFF25243A)
private val Muted = Color(0xFF85849A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("bhasha_buddy", Context.MODE_PRIVATE)
        setContent {
            var dark by remember { mutableStateOf(prefs.getBoolean("dark", false)) }
            MaterialTheme(colorScheme = if (dark) darkColorScheme(primary = Color(0xFFB7A7FF), surface = Color(0xFF202033))
                          else lightColorScheme(primary = Purple, background = Sky, surface = Color.White)) {
                BhashaBuddyApp(
                    dark = dark,
                    onDarkChange = {
                        dark = it
                        prefs.edit().putBoolean("dark", it).apply()
                    },
                    prefs = prefs
                )
            }
        }
    }
}

@Composable
private fun BhashaBuddyApp(dark: Boolean, onDarkChange: (Boolean) -> Unit,
                           prefs: android.content.SharedPreferences) {
    var name by remember { mutableStateOf(prefs.getString("name", "") ?: "") }
    var age by remember { mutableStateOf(prefs.getString("age", "") ?: "") }
    var welcomed by remember { mutableStateOf(prefs.getBoolean("welcomed", false)) }
    var page by remember { mutableStateOf("Home") }
    var sourceLang by remember { mutableStateOf("Hindi") }
    var targetLang by remember { mutableStateOf("English") }
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(prefs.getStringSet("history", emptySet())?.toList()?.reversed() ?: emptyList()) }
    val dailyStreak = remember { updateDailyStreak(prefs) }
    val clipboard = LocalClipboardManager.current
    val surface = if (dark) Color(0xFF202033) else Color.White
    val textColor = if (dark) Color.White else Ink

    if (!welcomed) {
        WelcomeScreen(name, { name = it }, age, { age = it }) {
            prefs.edit().putString("name", name).putString("age", age).putBoolean("welcomed", true).apply()
            welcomed = true
        }
    } else {
        Column(Modifier.fillMaxSize().background(if (dark) Color(0xFF121321) else Sky)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.bhasha_buddy_logo), contentDescription = "Bhasha Buddy logo", modifier = Modifier.size(42.dp), contentScale = ContentScale.Fit)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("Bhasha Buddy", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text("Quick Translate", fontSize = 12.sp, color = Muted)
                }
                Text("🔥 $dailyStreak day streak", color = Purple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { onDarkChange(!dark) }) {
                    Icon(if (dark) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = "Theme", tint = textColor)
                }
            }

            when (page) {
                "Home" -> {
                    Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(13.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("Translate anything", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text("✨ Smart", color = Purple, fontSize = 12.sp)
                        }
                        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = surface)) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                LanguagePill(sourceLang, if (sourceLang == "Hindi") "🇮🇳" else "🇬🇧", textColor)
                                IconButton(onClick = { val old = sourceLang; sourceLang = targetLang; targetLang = old }) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = "Swap languages", tint = Purple)
                                }
                                LanguagePill(targetLang, if (targetLang == "Hindi") "🇮🇳" else "🇬🇧", textColor)
                            }
                        }
                        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = surface)) {
                            Column(Modifier.padding(15.dp)) {
                                Text("YOUR TEXT", fontSize = 10.sp, color = Muted, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                BasicTextField(value = input, onValueChange = { input = it },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 105.dp),
                                    textStyle = LocalTextStyle.current.copy(color = textColor, fontSize = 16.sp),
                                    decorationBox = { inner ->
                                        if (input.isEmpty()) Text("Type or paste text here…", color = Muted, fontSize = 16.sp)
                                        inner()
                                    })
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { clipboard.setText(AnnotatedString(input)) }) {
                                        Icon(Icons.Default.ContentCopy, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Copy")
                                    }
                                    TextButton(onClick = { input = "" }) { Text("Clear") }
                                }
                            }
                        }
                        Button(onClick = {
                            // Translation engine will be connected in the next implementation step.
                            output = if (input.isBlank()) "" else "Translation engine will be connected next. Your text: $input"
                            if (input.isNotBlank()) {
                                history = (history + "$sourceLang → $targetLang: $input").takeLast(50)
                                prefs.edit().putStringSet("history", history.toSet()).apply()
                            }
                        }, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                            Text("Translate  →", fontWeight = FontWeight.Bold)
                        }
                        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = surface)) {
                            Column(Modifier.padding(15.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("TRANSLATION", color = Purple, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f))
                                    IconButton(onClick = { clipboard.setText(AnnotatedString(output)) }) {
                                        Icon(Icons.Default.ContentCopy, "Copy translation", tint = Purple)
                                    }
                                }
                                Text(output.ifBlank { "Your translated text will appear here." }, color = textColor, fontSize = 16.sp)
                            }
                        }
                        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (dark) Color(0xFF30294A) else PalePurple)) {
                            Row(Modifier.fillMaxWidth().clickable { page = "AI Support" }.padding(15.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("✨", fontSize = 23.sp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Quick AI Support", color = if (dark) Color.White else Ink, fontWeight = FontWeight.Bold)
                                    Text("Meanings, examples and sentence help", color = Muted, fontSize = 12.sp)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = Purple)
                            }
                        }
                    }
                }
                "History" -> {
                    Column(Modifier.weight(1f).padding(16.dp)) {
                        Text("Translation History", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Spacer(Modifier.height(12.dp))
                        if (history.isEmpty()) Text("Your translations will appear here.", color = Muted)
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            items(history.reversed()) { item ->
                                Card(colors = CardDefaults.cardColors(containerColor = surface)) {
                                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(item, Modifier.weight(1f), color = textColor, fontSize = 14.sp)
                                        IconButton(onClick = { clipboard.setText(AnnotatedString(item)) }) {
                                            Icon(Icons.Default.ContentCopy, null, tint = Purple)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "AI Support" -> {
                    Column(Modifier.weight(1f).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Quick AI Support ✨", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Text("Ask for word meanings, grammar tips and example sentences.", color = Muted)
                        Card(colors = CardDefaults.cardColors(containerColor = surface)) {
                            Text("AI assistant connection will be added in a later step.", Modifier.padding(18.dp), color = textColor)
                        }
                    }
                }
                else -> {
                    Column(Modifier.weight(1f).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Dark Mode", Modifier.weight(1f), color = textColor)
                            Switch(checked = dark, onCheckedChange = onDarkChange)
                        }
                        Text("Name: $name   •   Age: $age", color = Muted)
                        Text("Bhasha Buddy • Version 1.0", color = Muted)
                    }
                }
            }

            NavigationBar(containerColor = surface) {
                listOf("Home", "History", "AI Support", "Settings").forEach { item ->
                    NavigationBarItem(selected = page == item, onClick = { page = item },
                        icon = {
                            Icon(when (item) {
                                "Home" -> Icons.Default.Home
                                "History" -> Icons.Default.History
                                "AI Support" -> Icons.Default.AutoAwesome
                                else -> Icons.Default.Settings
                            }, contentDescription = item)
                        }, label = { Text(item, fontSize = 10.sp) })
                }
            }
        }
    }
}

// One streak day per local calendar day; no account or login is required.
private fun updateDailyStreak(prefs: android.content.SharedPreferences): Int {
    val formatter = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
    val today = formatter.format(java.util.Date())
    val lastDay = prefs.getString("last_used_day", "") ?: ""
    var streak = prefs.getInt("streak", 0)

    if (lastDay != today) {
        val yesterdayCalendar = java.util.Calendar.getInstance()
        yesterdayCalendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterday = formatter.format(yesterdayCalendar.time)
        streak = if (lastDay == yesterday) streak + 1 else 1
        prefs.edit().putString("last_used_day", today).putInt("streak", streak).apply()
    }
    return streak
}

@Composable
private fun LanguagePill(language: String, flag: String, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clip(RoundedCornerShape(30.dp)).background(PalePurple).padding(horizontal = 12.dp, vertical = 9.dp)) {
        Text(flag)
        Spacer(Modifier.width(6.dp))
        Text(language, color = Ink, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun WelcomeScreen(name: String, onName: (String) -> Unit, age: String,
                          onAge: (String) -> Unit, onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Sky).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(painter = painterResource(id = R.drawable.bhasha_buddy_logo), contentDescription = "Bhasha Buddy mascot", modifier = Modifier.size(150.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.height(15.dp))
        Text("Welcome to", fontSize = 17.sp, color = Muted)
        Text("Bhasha Buddy", fontSize = 31.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
        Text("Your friendly language companion", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(28.dp))
        Card(shape = RoundedCornerShape(25.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text("Let's get started", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                OutlinedTextField(name, onName, label = { Text("Your Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(age, onAge, label = { Text("Your Age") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = onStart, enabled = name.isNotBlank() && age.toIntOrNull() != null,
                    modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("Let's Start") }
                Text("No account needed • Just start translating", color = Muted, fontSize = 11.sp)
            }
        }
    }
}
