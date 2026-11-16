package com.bentech.parschat.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bentech.parschat.FirebaseChatRepository
import com.bentech.parschat.ui.components.FaravaharChatMark
import com.google.firebase.firestore.FirebaseFirestore

private data class GroupMemberRow(val uid: String, val name: String)

@Composable
fun GroupManagementScreen(groupId: String, background: Color, surface: Color, foreground: Color, onBack: () -> Unit) {
    val repo = remember { FirebaseChatRepository() }
    val uid = remember { repo.currentUserId() }
    var groupName by remember { mutableStateOf("") }
    var draftName by remember { mutableStateOf("") }
    var members by remember { mutableStateOf<List<GroupMemberRow>>(emptyList()) }
    var candidates by remember { mutableStateOf<List<GroupMemberRow>>(emptyList()) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var admins by remember { mutableStateOf<Set<String>>(emptySet()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val isAdmin = uid != null && uid in admins

    DisposableEffect(groupId) {
        val db = FirebaseFirestore.getInstance()
        val groupListener = db.collection("conversations").document(groupId).addSnapshotListener { snapshot, failure ->
            if (failure != null) { error = failure.localizedMessage; loading = false }
            else if (snapshot != null && snapshot.exists()) {
                groupName = snapshot.getString("name") ?: "گروه"
                draftName = groupName
                val ids = (snapshot.get("participants") as? List<*>)?.filterIsInstance<String>().orEmpty()
                admins = (snapshot.get("admins") as? List<*>)?.filterIsInstance<String>()?.toSet().orEmpty()
                members = ids.map { GroupMemberRow(it, it.take(8)) }
                ids.forEach { memberId ->
                    db.collection("users").document(memberId).get().addOnSuccessListener { profile ->
                        val display = profile.getString("displayName") ?: memberId.take(8)
                        members = members.map { if (it.uid == memberId) it.copy(name = display) else it }
                    }
                }
                loading = false
            }
        }
        val usersListener = repo.observeUsers(onUpdate = { snapshot ->
            val currentMembers = members.map { it.uid }.toSet()
            candidates = snapshot?.documents.orEmpty().mapNotNull { doc ->
                val name = doc.getString("displayName") ?: return@mapNotNull null
                if (doc.id == uid || doc.id in currentMembers) null else GroupMemberRow(doc.id, name)
            }
        }, onError = { error = it.localizedMessage })
        onDispose { groupListener.remove(); usersListener.remove() }
    }

    Column(Modifier.fillMaxSize().background(background).statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = foreground) }
            FaravaharChatMark(diameter = 38.dp, glow = .72f)
            Spacer(Modifier.width(10.dp))
            Column {
                Text("مدیریت گروه", color = foreground, fontSize = 21.sp)
                Text("$groupName • ${members.size} عضو", color = Color.Gray, fontSize = 11.sp)
            }
        }
        if (loading) Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFC9A45D)) }
        else {
            if (isAdmin) {
                OutlinedTextField(draftName, { draftName = it }, Modifier.fillMaxWidth(), label = { Text("نام گروه") }, singleLine = true)
                Button(onClick = { repo.renameGroup(groupId, draftName) { it.onFailure { e -> error = e.localizedMessage } } },
                    enabled = draftName.trim().length in 2..60, modifier = Modifier.fillMaxWidth()) { Text("ذخیره نام گروه") }
            }
            Text("اعضای فعلی", color = foreground, modifier = Modifier.padding(vertical = 10.dp))
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(members, key = { it.uid }) { member ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(surface).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(member.name, color = foreground, modifier = Modifier.weight(1f))
                        if (member.uid in admins) Text("مدیر", color = Color(0xFFC9A45D), fontSize = 11.sp)
                        else if (isAdmin) TextButton(onClick = { repo.removeGroupMember(groupId, member.uid) { it.onFailure { e -> error = e.localizedMessage } } }) { Text("حذف") }
                    }
                }
            }
            if (isAdmin) {
                Text("افزودن عضو", color = foreground, modifier = Modifier.padding(vertical = 8.dp))
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(candidates, key = { it.uid }) { candidate ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(surface).clickable {
                            selected = if (candidate.uid in selected) selected - candidate.uid else selected + candidate.uid
                        }.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(candidate.uid in selected, { checked -> selected = if (checked) selected + candidate.uid else selected - candidate.uid })
                            Text(candidate.name, color = foreground)
                        }
                    }
                }
                Button(onClick = { repo.addGroupMembers(groupId, selected.toList()) { result ->
                    result.onSuccess { selected = emptySet() }.onFailure { error = it.localizedMessage }
                } }, enabled = selected.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("افزودن ${selected.size} عضو") }
            }
        }
        if (error != null) Text(error!!, color = Color(0xFFFFB4AB), fontSize = 12.sp)
    }
}
