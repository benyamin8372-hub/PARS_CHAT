package com.bentech.parschat

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

/**
 * Firebase data layer for PARS CHAT.
 *
 * Firestore rules in /firestore.rules must be published before live use.
 * This class deliberately exposes callbacks so it does not require a coroutine runtime.
 */
class FirebaseChatRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun register(
        email: String,
        password: String,
        displayName: String,
        onResult: (Result<String>) -> Unit
    ) {
        val cleanName = displayName.trim()
        if (cleanName.isEmpty() || email.isBlank() || password.length < 6) {
            onResult(Result.failure(IllegalArgumentException("نام و ایمیل لازم است و رمز باید حداقل ۶ نویسه باشد.")))
            return
        }
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    onResult(Result.failure(IllegalStateException("حساب کاربری ایجاد نشد.")))
                    return@addOnSuccessListener
                }
                val profile = mapOf(
                    "uid" to user.uid,
                    "displayName" to cleanName,
                    "photoUrl" to null,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "lastSeen" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(user.uid).set(profile)
                    .addOnSuccessListener { onResult(Result.success(user.uid)) }
                    .addOnFailureListener { error ->
                        user.delete()
                        onResult(Result.failure(error))
                    }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun signIn(email: String, password: String, onResult: (Result<String>) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("ایمیل و رمز عبور را وارد کنید.")))
            return
        }
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) onResult(Result.failure(IllegalStateException("شناسه حساب دریافت نشد.")))
                else {
                    db.collection("users").document(uid)
                        .update("lastSeen", FieldValue.serverTimestamp())
                        .addOnCompleteListener { onResult(Result.success(uid)) }
                }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun signOut() = auth.signOut()

    fun currentUserId(): String? = auth.currentUser?.uid

    fun observeUsers(
        onUpdate: (QuerySnapshot?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        val uid = currentUserId()
        require(uid != null) { "ابتدا وارد حساب کاربری شوید." }
        return db.collection("users")
            .orderBy("displayName", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) onError(error) else onUpdate(snapshot)
            }
    }

    fun openConversation(otherUserId: String, onResult: (Result<String>) -> Unit) {
        val me = currentUserId()
        if (me == null || otherUserId.isBlank() || me == otherUserId) {
            onResult(Result.failure(IllegalArgumentException("کاربر مقصد معتبر نیست یا وارد حساب نشده‌اید.")))
            return
        }
        val participants = listOf(me, otherUserId).sorted()
        val conversationId = participants.joinToString("_")
        val ref = db.collection("conversations").document(conversationId)
        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                onResult(Result.success(conversationId))
            } else {
                val data = mapOf(
                    "participants" to participants,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                ref.set(data)
                    .addOnSuccessListener { onResult(Result.success(conversationId)) }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
        }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun observeMessages(
        conversationId: String,
        onUpdate: (QuerySnapshot?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) onError(error) else onUpdate(snapshot)
            }
    }

    fun sendText(
        conversationId: String,
        text: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val uid = currentUserId()
        val body = text.trim()
        if (uid == null || body.isEmpty() || body.length > 4000) {
            onResult(Result.failure(IllegalArgumentException("برای ارسال پیام وارد حساب شوید و متن معتبر بنویسید.")))
            return
        }
        val conversation = db.collection("conversations").document(conversationId)
        val message = conversation.collection("messages").document()
        val batch = db.batch()
        batch.set(message, mapOf(
            "senderId" to uid,
            "text" to body,
            "createdAt" to FieldValue.serverTimestamp(),
            "type" to "text"
        ))
        batch.update(conversation, mapOf(
            "updatedAt" to FieldValue.serverTimestamp(),
            "lastMessage" to body,
            "lastMessageAt" to FieldValue.serverTimestamp()
        ))
        batch.commit()
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }
}
