package com.bentech.parschat.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bentech.parschat.FirebaseChatRepository
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FirebaseFirestore

private data class BlockedEntry(val uid: String, val name: String)

@Composable
fun BlockedUsersScreen(background: Color, surface: Color, foreground: Color, onBack: () -> Unit) {
    val repository = remember { FirebaseChatRepository() }
    var users by remember { mutableStateOf<List<BlockedEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    DisposableEffect(Unit) {
        val listener = repository.observeBlocked(
            onUpdate = { snapshot: QuerySnapshot? ->
                val entries = snapshot?.documents.orEmpty()
                users = entries.map { BlockedEntry(it.id, it.id) }
                loading = false
                entries.forEach { blocked ->
                    FirebaseFirestore.getInstance().collection("users").document(blocked.id).get().addOnSuccessListener { profile ->
                        val name = profile.getString("displayName")
                        if (!name.isNullOrBlank()) users = users.map { if (it.uid == blocked.id) it.copy(name = name) else it }
                    }
                }
            },
            onError = { error = it.localizedMessage; loading = false }
        )
        onDispose { listener.remove() }
    }
    Column(Modifier.fillMaxSize().background(background).statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = foreground) }
            Text("کاربران مسدودشده", color = foreground, fontSize = 21.sp)
        }
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFC9A45D)) }
            error != null -> Text(error!!, color = Color(0xFFFFB4AB))
            users.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("فهرست مسدودشده‌ها خالی است.", color = Color.Gray) }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(users, key = { it.uid }) { user ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(surface).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(user.name, color = foreground, modifier = Modifier.weight(1f))
                        TextButton(onClick = { repository.unblockUser(user.uid) { result -> result.onFailure { error = it.localizedMessage } } }) { Text("رفع مسدودیت", color = Color(0xFF6CC8A5)) }
                    }
                }
            }
        }
    }
}
