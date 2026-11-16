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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

private data class AdminUser(val uid: String, val name: String, val isAdmin: Boolean)

@Composable
fun AdminPanelScreen(background: Color, surface: Color, foreground: Color, onBack: () -> Unit) {
    var authorized by remember { mutableStateOf<Boolean?>(null) }
    var users by remember { mutableStateOf<List<AdminUser>>(emptyList()) }
    var userCount by remember { mutableIntStateOf(0) }
    var conversationCount by remember { mutableIntStateOf(0) }
    var disabled by remember { mutableStateOf<Set<String>>(emptySet()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        loading = true
        val current = FirebaseAuth.getInstance().currentUser
        if (current == null) { authorized = false; loading = false; return }
        current.getIdToken(true).addOnSuccessListener { token ->
            val isAdmin = token.claims["admin"] == true
            authorized = isAdmin
            if (!isAdmin) { loading = false; return@addOnSuccessListener }
            FirebaseFunctions.getInstance("europe-west1").getHttpsCallable("listAdminUsers").call()
                .addOnSuccessListener { callable ->
                    val statusRows = (callable.data as? Map<*, *>)?.get("users") as? List<*> ?: emptyList<Any>()
                    val statuses = statusRows.mapNotNull { row ->
                        val item = row as? Map<*, *> ?: return@mapNotNull null
                        val id = item["uid"] as? String ?: return@mapNotNull null
                        id to Pair(item["disabled"] as? Boolean ?: false, item["isAdmin"] as? Boolean ?: false)
                    }.toMap()
                    disabled = statuses.filterValues { it.first }.keys
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").get().addOnSuccessListener { userSnapshot ->
                        users = userSnapshot.documents.map { AdminUser(it.id, it.getString("displayName") ?: "کاربر", statuses[it.id]?.second == true) }
                        userCount = userSnapshot.size()
                        db.collection("conversations").get().addOnSuccessListener { conversations ->
                            conversationCount = conversations.size()
                            loading = false
                        }.addOnFailureListener { error = it.localizedMessage; loading = false }
                    }.addOnFailureListener { error = it.localizedMessage; loading = false }
                }.addOnFailureListener { error = it.localizedMessage; loading = false }
        }.addOnFailureListener { error = it.localizedMessage; authorized = false; loading = false }
    }

    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().background(background).statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = foreground) }
            Text("پنل مدیریت", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { load() }) { Text("به‌روزرسانی", color = Color(0xFFC9A45D)) }
        }
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFC9A45D)) }
            authorized != true -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("دسترسی مدیر برای این حساب فعال نیست.", color = Color(0xFFFFB4AB))
            }
            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = surface)) {
                        Column(Modifier.padding(16.dp)) { Text("کاربران", color = Color.Gray); Text(userCount.toString(), color = foreground, fontSize = 26.sp, fontWeight = FontWeight.Bold) }
                    }
                    Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = surface)) {
                        Column(Modifier.padding(16.dp)) { Text("گفتگوها", color = Color.Gray); Text(conversationCount.toString(), color = foreground, fontSize = 26.sp, fontWeight = FontWeight.Bold) }
                    }
                }
                Text("مدیریت حساب کاربران", color = foreground, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(users, key = { it.uid }) { user ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(surface).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(user.name, color = foreground, fontWeight = FontWeight.SemiBold)
                                Text(user.uid.take(12) + "…", color = Color.Gray, fontSize = 10.sp)
                            }
                            if (user.isAdmin) {
                                Text("مدیر", color = Color(0xFFC9A45D), fontSize = 11.sp)
                            } else {
                                TextButton(onClick = {
                                    val shouldDisable = user.uid !in disabled
                                    FirebaseFunctions.getInstance("europe-west1")
                                        .getHttpsCallable("setUserDisabled")
                                        .call(mapOf("uid" to user.uid, "disabled" to shouldDisable))
                                        .addOnSuccessListener {
                                            disabled = if (shouldDisable) disabled + user.uid else disabled - user.uid
                                        }
                                        .addOnFailureListener { error = it.localizedMessage ?: "عملیات مدیریتی ناموفق بود." }
                                }) { Text(if (user.uid in disabled) "فعال‌سازی" else "تعلیق", color = if (user.uid in disabled) Color(0xFF6CC8A5) else Color(0xFFE6A0A0)) }
                            }
                        }
                    }
                }
                if (error != null) Text(error!!, color = Color(0xFFFFB4AB), modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
