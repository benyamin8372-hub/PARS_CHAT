package com.bentech.parschat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bentech.parschat.FirebaseChatRepository
import com.bentech.parschat.ui.components.FaravaharChatMark
import com.google.firebase.firestore.QuerySnapshot

private data class GroupCandidate(val uid: String, val name: String)

@Composable
fun GroupCreateScreen(
    background: Color,
    surface: Color,
    foreground: Color,
    onBack: () -> Unit,
    onCreated: (String, String) -> Unit
) {
    val repository = remember { FirebaseChatRepository() }
    val currentUid = remember { repository.currentUserId() }
    var title by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<GroupCandidate>>(emptyList()) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(currentUid) {
        val listener = repository.observeUsers(
            onUpdate = { snapshot: QuerySnapshot? ->
                users = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val name = doc.getString("displayName")?.trim().orEmpty()
                    if (doc.id == currentUid || name.isBlank()) null else GroupCandidate(doc.id, name)
                }
                loading = false
            },
            onError = { loading = false; error = it.localizedMessage ?: "دریافت کاربران ناموفق بود." }
        )
        onDispose { listener.remove() }
    }

    Column(Modifier.fillMaxSize().background(background).statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = foreground) }
            FaravaharChatMark(diameter = 38.dp, glow = .72f)
            Spacer(Modifier.width(10.dp))
            Text("ساخت گروه تازه", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        OutlinedTextField(
            value = title, onValueChange = { title = it; error = null }, modifier = Modifier.fillMaxWidth(),
            label = { Text("نام گروه") }, singleLine = true, enabled = !saving
        )
        Text("اعضا را انتخاب کن (حداقل یک نفر دیگر)", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
        when {
            loading -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFC9A45D)) }
            users.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { Text("کاربر دیگری برای افزودن وجود ندارد.", color = Color.Gray) }
            else -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(users, key = { it.uid }) { user ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(surface)
                            .clickable { selected = if (user.uid in selected) selected - user.uid else selected + user.uid }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = user.uid in selected, onCheckedChange = { checked ->
                            selected = if (checked) selected + user.uid else selected - user.uid
                        })
                        Text(user.name, color = foreground, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        if (error != null) Text(error!!, color = Color(0xFFFFB4AB), fontSize = 12.sp)
        Button(
            onClick = {
                saving = true
                repository.createGroup(title, selected.toList()) { result ->
                    saving = false
                    result.onSuccess { onCreated(it, title.trim()) }
                        .onFailure { error = it.localizedMessage ?: "ساخت گروه انجام نشد." }
                }
            },
            enabled = !saving && title.trim().length >= 2 && selected.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC9A45D), contentColor = Color(0xFF091326))
        ) {
            if (saving) CircularProgressIndicator(Modifier.size(18.dp), color = Color(0xFF091326))
            else Text("ایجاد گروه با ${selected.size + 1} عضو", fontWeight = FontWeight.Bold)
        }
    }
}
