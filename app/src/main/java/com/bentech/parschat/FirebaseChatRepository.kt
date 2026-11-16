package com.bentech.parschat

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage

/**
 * Authenticated Firestore/Storage data access for PARS CHAT.
 * Publish firestore.rules and storage.rules before enabling these operations.
 */
class FirebaseChatRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    fun currentUserId(): String? = auth.currentUser?.uid
    fun signOut() = auth.signOut()

    fun register(email: String, password: String, displayName: String, onResult: (Result<String>) -> Unit) {
        val name = displayName.trim()
        if (name.length < 2 || email.isBlank() || password.length < 6) {
            onResult(Result.failure(IllegalArgumentException("نام، ایمیل معتبر و گذرواژه حداقل ۶ نویسه‌ای لازم است."))); return
        }
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val user = result.user ?: run { onResult(Result.failure(IllegalStateException("حساب ایجاد نشد."))); return@addOnSuccessListener }
                val profile = mapOf("uid" to user.uid, "displayName" to name, "photoUrl" to null,
                    "createdAt" to FieldValue.serverTimestamp(), "lastSeen" to FieldValue.serverTimestamp(),
                    "online" to true, "showOnline" to true, "showLastSeen" to true, "blockedCount" to 0)
                db.collection("users").document(user.uid).set(profile)
                    .addOnSuccessListener { onResult(Result.success(user.uid)) }
                    .addOnFailureListener { error -> user.delete(); onResult(Result.failure(error)) }
            }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun signIn(email: String, password: String, onResult: (Result<String>) -> Unit) {
        if (email.isBlank() || password.isBlank()) { onResult(Result.failure(IllegalArgumentException("ایمیل و گذرواژه را وارد کنید."))); return }
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) onResult(Result.failure(IllegalStateException("شناسه حساب دریافت نشد.")))
                else setPresence(true) { onResult(Result.success(uid)) }
            }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun saveDeviceToken(token: String, onResult: ((Result<Unit>) -> Unit)? = null) {
        val uid = currentUserId() ?: run { onResult?.invoke(Result.failure(IllegalStateException("ورود لازم است."))); return }
        db.collection("users").document(uid).collection("devices").document(token)
            .set(mapOf("token" to token, "updatedAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onResult?.invoke(Result.success(Unit)) }
            .addOnFailureListener { onResult?.invoke(Result.failure(it)) }
    }

    fun setPresence(online: Boolean, onResult: ((Result<Unit>) -> Unit)? = null) {
        val uid = currentUserId() ?: run { onResult?.invoke(Result.failure(IllegalStateException("ورود لازم است."))); return }
        val updates = if (online) mapOf("online" to true) else mapOf("online" to false, "lastSeen" to FieldValue.serverTimestamp())
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener { onResult?.invoke(Result.success(Unit)) }
            .addOnFailureListener { onResult?.invoke(Result.failure(it)) }
    }

    fun updatePrivacy(showOnline: Boolean, showLastSeen: Boolean, onResult: (Result<Unit>) -> Unit) {
        val uid = currentUserId() ?: run { onResult(Result.failure(IllegalStateException("ورود لازم است."))); return }
        db.collection("users").document(uid).update(mapOf("showOnline" to showOnline, "showLastSeen" to showLastSeen))
            .addOnSuccessListener { onResult(Result.success(Unit)) }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun updateDisplayName(name: String, onResult: (Result<Unit>) -> Unit) {
        val uid = currentUserId()
        val clean = name.trim()
        if (uid == null || clean.length !in 2..40) { onResult(Result.failure(IllegalArgumentException("نام باید بین ۲ تا ۴۰ نویسه باشد."))); return }
        db.collection("users").document(uid).update("displayName", clean)
            .addOnSuccessListener { onResult(Result.success(Unit)) }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun observeUsers(onUpdate: (QuerySnapshot?) -> Unit, onError: (Exception) -> Unit): ListenerRegistration =
        db.collection("users").orderBy("displayName", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error -> if (error != null) onError(error) else onUpdate(snapshot) }

    fun isBlocked(otherUid: String, onResult: (Result<Boolean>) -> Unit) {
        val uid = currentUserId() ?: run { onResult(Result.failure(IllegalStateException("ورود لازم است."))); return }
        db.collection("users").document(uid).collection("blocked").document(otherUid).get()
            .addOnSuccessListener { onResult(Result.success(it.exists())) }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun blockUser(otherUid: String, onResult: (Result<Unit>) -> Unit) {
        val uid = currentUserId()
        if (uid == null || uid == otherUid || otherUid.isBlank()) { onResult(Result.failure(IllegalArgumentException("کاربر مقصد معتبر نیست."))); return }
        db.collection("users").document(uid).collection("blocked").document(otherUid)
            .set(mapOf("uid" to otherUid, "createdAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onResult(Result.success(Unit)) }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun unblockUser(otherUid: String, onResult: (Result<Unit>) -> Unit) {
        val uid = currentUserId() ?: run { onResult(Result.failure(IllegalStateException("ورود لازم است."))); return }
        db.collection("users").document(uid).collection("blocked").document(otherUid).delete()
            .addOnSuccessListener { onResult(Result.success(Unit)) }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun observeBlocked(onUpdate: (QuerySnapshot?) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        val uid = currentUserId() ?: throw IllegalStateException("ورود لازم است.")
        return db.collection("users").document(uid).collection("blocked")
            .addSnapshotListener { snapshot, error -> if (error != null) onError(error) else onUpdate(snapshot) }
    }

    fun openConversation(otherUserId: String, onResult: (Result<String>) -> Unit) {
        val me = currentUserId()
        if (me == null || otherUserId.isBlank() || me == otherUserId) { onResult(Result.failure(IllegalArgumentException("کاربر مقصد معتبر نیست."))); return }
        val participants = listOf(me, otherUserId).sorted()
        val id = participants.joinToString("_")
        val ref = db.collection("conversations").document(id)
        db.collection("users").document(me).collection("blocked").document(otherUserId).get()
            .addOnSuccessListener { blocked ->
                if (blocked.exists()) { onResult(Result.failure(IllegalStateException("ابتدا کاربر را از فهرست مسدودها خارج کنید."))); return@addOnSuccessListener }
                db.collection("users").document(otherUserId).collection("blocked").document(me).get()
                    .addOnSuccessListener { blockedByOther ->
                        if (blockedByOther.exists()) { onResult(Result.failure(IllegalStateException("امکان شروع گفتگو با این کاربر وجود ندارد."))); return@addOnSuccessListener }
                        ref.get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) onResult(Result.success(id))
                            else ref.set(mapOf("participants" to participants, "type" to "private", "createdBy" to me,
                                "createdAt" to FieldValue.serverTimestamp(), "updatedAt" to FieldValue.serverTimestamp()))
                                .addOnSuccessListener { onResult(Result.success(id)) }.addOnFailureListener { onResult(Result.failure(it)) }
                        }.addOnFailureListener { onResult(Result.failure(it)) }
                    }.addOnFailureListener { onResult(Result.failure(it)) }
            }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun createGroup(name: String, memberIds: List<String>, onResult: (Result<String>) -> Unit) {
        val uid = currentUserId()
        val clean = name.trim()
        if (uid == null || clean.length !in 2..60) { onResult(Result.failure(IllegalArgumentException("نام گروه باید بین ۲ تا ۶۰ نویسه باشد."))); return }
        val members = (memberIds + uid).distinct()
        if (members.size < 2 || members.size > 50) { onResult(Result.failure(IllegalArgumentException("گروه باید دست‌کم ۲ و حداکثر ۵۰ عضو داشته باشد."))); return }
        val ref = db.collection("conversations").document()
        ref.set(mapOf("participants" to members, "type" to "group", "name" to clean, "createdBy" to uid,
            "admins" to listOf(uid), "createdAt" to FieldValue.serverTimestamp(), "updatedAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onResult(Result.success(ref.id)) }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun observeConversations(onUpdate: (QuerySnapshot?) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        val uid = currentUserId() ?: throw IllegalStateException("ورود لازم است.")
        return db.collection("conversations").whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, error -> if (error != null) onError(error) else onUpdate(snapshot) }
    }

    fun observeMessages(conversationId: String, onUpdate: (QuerySnapshot?) -> Unit, onError: (Exception) -> Unit): ListenerRegistration =
        db.collection("conversations").document(conversationId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error -> if (error != null) onError(error) else onUpdate(snapshot) }

    fun sendText(conversationId: String, text: String, replyToId: String? = null, replyToText: String? = null, onResult: (Result<Unit>) -> Unit) {
        val uid = currentUserId()
        val body = text.trim()
        if (uid == null || body.isEmpty() || body.length > 4000) { onResult(Result.failure(IllegalArgumentException("متن پیام معتبر نیست."))); return }
        val conversation = db.collection("conversations").document(conversationId)
        val message = conversation.collection("messages").document()
        val data = mutableMapOf<String, Any>("senderId" to uid, "text" to body, "createdAt" to FieldValue.serverTimestamp(),
            "type" to "text", "readBy" to mapOf(uid to true))
        if (!replyToId.isNullOrBlank()) { data["replyToId"] = replyToId; data["replyToText"] = replyToText.orEmpty().take(250) }
        val batch = db.batch()
        batch.set(message, data)
        batch.update(conversation, mapOf("updatedAt" to FieldValue.serverTimestamp(), "lastMessage" to body, "lastMessageAt" to FieldValue.serverTimestamp()))
        batch.commit().addOnSuccessListener { onResult(Result.success(Unit)) }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun editMessage(conversationId: String, messageId: String, newText: String, onResult: (Result<Unit>) -> Unit) {
        val body = newText.trim()
        if (body.isEmpty() || body.length > 4000) { onResult(Result.failure(IllegalArgumentException("متن پیام معتبر نیست."))); return }
        db.collection("conversations").document(conversationId).collection("messages").document(messageId)
            .update(mapOf("text" to body, "editedAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onResult(Result.success(Unit)) }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun deleteMessage(conversationId: String, messageId: String, onResult: (Result<Unit>) -> Unit) {
        db.collection("conversations").document(conversationId).collection("messages").document(messageId)
            .update(mapOf("deleted" to true, "text" to "این پیام حذف شد", "deletedAt" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { onResult(Result.success(Unit)) }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun markRead(conversationId: String, messageIds: List<String>, onResult: ((Result<Unit>) -> Unit)? = null) {
        val uid = currentUserId() ?: run { onResult?.invoke(Result.failure(IllegalStateException("ورود لازم است."))); return }
        val batch = db.batch()
        messageIds.forEach { id ->
            batch.update(db.collection("conversations").document(conversationId).collection("messages").document(id), "readBy.$uid", true)
        }
        if (messageIds.isEmpty()) { onResult?.invoke(Result.success(Unit)); return }
        batch.commit().addOnSuccessListener { onResult?.invoke(Result.success(Unit)) }.addOnFailureListener { onResult?.invoke(Result.failure(it)) }
    }

    fun forwardMessage(destinationConversationId: String, sourceText: String, onResult: (Result<Unit>) -> Unit) {
        sendText(destinationConversationId, sourceText, onResult = onResult)
    }

    fun uploadMedia(conversationId: String, uri: Uri, mimeType: String, onResult: (Result<String>) -> Unit) {
        val uid = currentUserId() ?: run { onResult(Result.failure(IllegalStateException("ورود لازم است."))); return }
        val safeType = when {
            mimeType.startsWith("image/") -> "image"
            mimeType.startsWith("audio/") -> "audio"
            else -> { onResult(Result.failure(IllegalArgumentException("فقط تصویر و صوت پشتیبانی می‌شود."))); return }
        }
        val ref = storage.reference.child("conversations/$conversationId/$uid/${System.currentTimeMillis()}_${safeType}")
        ref.putFile(uri).continueWithTask { task -> if (!task.isSuccessful) throw task.exception ?: IllegalStateException("بارگذاری ناموفق"); ref.downloadUrl }
            .addOnSuccessListener { download ->
                val message = db.collection("conversations").document(conversationId).collection("messages").document()
                val body = if (safeType == "image") "تصویر" else "پیام صوتی"
                message.set(mapOf("senderId" to uid, "text" to body, "type" to safeType, "mediaUrl" to download.toString(),
                    "mimeType" to mimeType, "createdAt" to FieldValue.serverTimestamp(), "readBy" to mapOf(uid to true)))
                    .addOnSuccessListener {
                        db.collection("conversations").document(conversationId).update(mapOf("lastMessage" to body, "lastMessageAt" to FieldValue.serverTimestamp(), "updatedAt" to FieldValue.serverTimestamp()))
                        onResult(Result.success(download.toString()))
                    }.addOnFailureListener { onResult(Result.failure(it)) }
            }.addOnFailureListener { onResult(Result.failure(it)) }
    }
}
