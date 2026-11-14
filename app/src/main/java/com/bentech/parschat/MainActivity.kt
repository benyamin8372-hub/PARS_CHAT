package com.bentech.parschat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PersianNavy = Color(0xFF0E1740)
private val PersianGold = Color(0xFFD4AF37)
private val PersianTeal = Color(0xFF40C4B8)
private val samplePeople = listOf("آریا", "ماندانا", "کوروش", "آتوسا", "داریوش", "رکسانا", "بهرام", "شیرین")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ParsChatApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParsChatApp() {
    var page by remember { mutableStateOf("splash") }
    var selected by remember { mutableStateOf(samplePeople.first()) }
    var dark by remember { mutableStateOf(false) }
    val background = if (dark) Color(0xFF090F25) else Color(0xFFF4F1E8)
    val panel = if (dark) Color(0xFF172344) else Color.White

    MaterialTheme(colorScheme = if (dark) darkColorScheme(primary = PersianGold, secondary = PersianTeal) else lightColorScheme(primary = PersianNavy, secondary = PersianTeal)) {
        when (page) {
            "splash" -> {
                Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(PersianNavy, Color(0xFF243B68)))).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("𐎠", fontSize = 94.sp, color = PersianGold)
                    Text("PARS CHAT", fontSize = 32.sp, color = PersianGold, fontWeight = FontWeight.Black)
                    Text("پیوندی به شکوه ایران", color = Color.White, modifier = Modifier.padding(top = 10.dp))
                    Text("ساخته بنیامین نوری دفرازی", color = PersianTeal, modifier = Modifier.padding(22.dp))
                    Button(onClick = { page = "intro" }) { Text("آغاز") }
                }
            }
            "intro" -> {
                Column(Modifier.fillMaxSize().background(PersianNavy).padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("میراث کهن، ارتباط نو", color = PersianGold, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text("گفتگوهایت را در فضایی الهام‌گرفته از هنر و معماری ایران باستان دنبال کن.", color = Color.White, modifier = Modifier.padding(vertical = 24.dp))
                    Button(onClick = { page = "login" }) { Text("ادامه") }
                }
            }
            "login" -> {
                var username by remember { mutableStateOf("") }
                Column(Modifier.fillMaxSize().background(PersianNavy).padding(24.dp), verticalArrangement = Arrangement.Center) {
                    Text("به پارس چت خوش آمدی", color = PersianGold, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("نام نمایشی") }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp))
                    Button(onClick = { page = "chats" }, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) { Text("ورود") }
                }
            }
            "chats" -> Scaffold(
                containerColor = background,
                topBar = { TopAppBar(title = { Text("PARS CHAT", color = PersianGold, fontWeight = FontWeight.Black) }, actions = {
                    IconButton(onClick = { page = "profile" }) { Icon(Icons.Default.Person, null, tint = PersianGold) }
                    IconButton(onClick = { page = "settings" }) { Icon(Icons.Default.Settings, null, tint = PersianGold) }
                }) },
                floatingActionButton = { FloatingActionButton(onClick = { selected = "گفتگوی تازه"; page = "conversation" }, containerColor = PersianGold) { Icon(Icons.Default.Add, null, tint = PersianNavy) } }
            ) { padding ->
                Column(Modifier.padding(padding).padding(14.dp)) {
                    Text("درود، بنیامین", color = if (dark) Color.White else PersianNavy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("هشت گفتگوی نمونه", color = Color.Gray, modifier = Modifier.padding(bottom = 10.dp))
                    LazyColumn { items(samplePeople) { person ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp).background(panel, RoundedCornerShape(18.dp)).clickable { selected = person; page = "conversation" }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(48.dp).background(Brush.linearGradient(listOf(PersianGold, PersianTeal)), CircleShape), contentAlignment = Alignment.Center) { Text(person.take(1), color = PersianNavy, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                            Column(Modifier.padding(start = 12.dp)) {
                                Text(person, color = if (dark) Color.White else PersianNavy, fontWeight = FontWeight.Bold)
                                Text("پیام نمونه • برای مشاهده گفتگو لمس کنید", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    } }
                }
            }
            "conversation" -> ConversationPage(selected, panel, { page = "call" }, { page = "chats" })
            "call" -> Column(Modifier.fillMaxSize().background(PersianNavy), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("تماس تصویری", color = PersianGold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("حالت نمایشی — بدون اتصال WebRTC", color = Color.White, modifier = Modifier.padding(16.dp))
                Button(onClick = { page = "conversation" }, colors = ButtonDefaults.buttonColors(containerColor = PersianGold)) { Text("پایان تماس", color = PersianNavy) }
            }
            "settings" -> Column(Modifier.fillMaxSize().background(background).padding(22.dp)) {
                Text("تنظیمات", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("حالت تاریک", Modifier.weight(1f)); Switch(checked = dark, onCheckedChange = { dark = it }) }
                Text("درباره برنامه", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp))
                Text("PARS CHAT — ساخته بنیامین نوری دفرازی")
                Button(onClick = { page = "chats" }, modifier = Modifier.padding(top = 20.dp)) { Text("بازگشت") }
            }
            else -> Column(Modifier.fillMaxSize().background(background).padding(24.dp)) {
                Text("نمایه کاربر", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("بنیامین نوری دفرازی", modifier = Modifier.padding(vertical = 12.dp))
                Button(onClick = { page = "chats" }) { Text("بازگشت") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationPage(name: String, panel: Color, onCall: () -> Unit, onBack: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf("درود! به پارس چت خوش آمدی.", "این یک گفتگوی نمونه و محلی است.") }
    Scaffold(
        topBar = { TopAppBar(title = { Text(name) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = { IconButton(onClick = onCall) { Icon(Icons.Default.Videocam, null, tint = PersianGold) } }) },
        bottomBar = { Row(Modifier.fillMaxWidth().background(panel).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = draft, onValueChange = { draft = it }, modifier = Modifier.weight(1f), placeholder = { Text("پیام بنویسید") }, maxLines = 3)
            IconButton(onClick = { if (draft.isNotBlank()) { messages.add(draft.trim()); draft = "" } }) { Icon(Icons.Default.Send, null, tint = PersianGold) }
        } }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            items(messages) { message ->
                Text(message, Modifier.padding(vertical = 5.dp).background(Color(0xFFE9D99E), RoundedCornerShape(16.dp)).padding(14.dp), color = PersianNavy)
            }
        }
    }
}
