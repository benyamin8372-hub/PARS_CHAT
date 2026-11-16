package com.bentech.parschat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.DisposableEffect
import com.bentech.parschat.FirebaseChatRepository
import com.bentech.parschat.ui.components.DimensionalAvatar

private data class DirectoryEntry(val uid: String, val name: String)

@Composable
fun UserDirectoryScreen(
    background: Color,
    surface: Color,
    foreground: Color,
    onBack: () -> Unit,
    onSelect: (String, String) -> Unit
) {
    val repository = remember { FirebaseChatRepository() }
    val currentUid = remember { repository.currentUserId() }
    var users by remember { mutableStateOf<List<DirectoryEntry>>(emptyList()) }
    var search by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(currentUid) {
        if (currentUid == null) {
            loading = false
            error = "برای دیدن مخاطبان ابتدا وارد حساب شو."
            onDispose { }
        } else {
            val registration = try {
                repository.observeUsers(
                    onUpdate = { snapshot ->
                        users = snapshot?.documents.orEmpty().mapNotNull { doc ->
                            val uid = doc.id
                            val displayName = doc.getString("displayName")?.trim().orEmpty()
                            if (uid == currentUid || displayName.isBlank()) null
                            else DirectoryEntry(uid, displayName)
                        }
                        loading = false
                        error = null
                    },
                    onError = { exception ->
                        loading = false
                        error = exception.localizedMessage ?: "دریافت مخاطبان انجام نشد."
                    }
                )
            } catch (exception: Exception) {
                loading = false
                error = exception.localizedMessage ?: "اتصال به فهرست مخاطبان برقرار نشد."
                null
            }
            onDispose { registration?.remove() }
        }
    }

    val filtered = users.filter { it.name.contains(search.trim(), ignoreCase = true) }

    Column(Modifier.fillMaxSize().background(background).statusBarsPadding().navigationBarsPadding().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = foreground) }
            Column(Modifier.weight(1f)) {
                Text("مخاطبان", color = foreground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("کاربران ثبت‌نام‌شده در پارس چت", color = Color.Gray, fontSize = 11.sp)
            }
            Text(users.size.toString(), color = Color(0xFFC9A45D), fontWeight = FontWeight.Bold)
        }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(surface)
                .border(1.dp, Color(0x55C9A45D), RoundedCornerShape(15.dp)).padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = Color(0xFF559B96))
            BasicTextField(
                value = search, onValueChange = { search = it }, modifier = Modifier.weight(1f).padding(start = 10.dp),
                singleLine = true, textStyle = TextStyle(color = foreground, fontSize = 14.sp),
                decorationBox = { inner -> if (search.isEmpty()) Text("جستجوی نام کاربران...", color = Color.Gray, fontSize = 13.sp); inner() }
            )
            if (search.isNotEmpty()) IconButton(onClick = { search = "" }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.Gray)
            }
        }
        Spacer(Modifier.height(14.dp))
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFC9A45D)) }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error!!, color = Color(0xFFFFB4AB), textAlign = TextAlign.Center)
            }
            filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (search.isBlank()) "هنوز کاربر دیگری ثبت‌نام نکرده است." else "کاربری با این نام پیدا نشد.", color = Color.Gray, textAlign = TextAlign.Center)
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(filtered, key = { it.uid }) { user ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(surface)
                            .clickable { onSelect(user.uid, user.name) }.padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DimensionalAvatar(label = user.name, diameter = 48.dp)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(user.name, color = foreground, fontWeight = FontWeight.SemiBold)
                            Text("برای شروع گفتگو لمس کن", color = Color.Gray, fontSize = 11.sp)
                        }
                        Text("گفتگو", color = Color(0xFFC9A45D), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
