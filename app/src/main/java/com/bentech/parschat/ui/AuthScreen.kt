package com.bentech.parschat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bentech.parschat.FirebaseChatRepository
import com.google.firebase.messaging.FirebaseMessaging

private val AuthNight = Color(0xFF091326)
private val AuthGold = Color(0xFFC9A45D)
private val AuthIvory = Color(0xFFF3EFE5)

@Composable
fun AuthScreen(onSuccess: (String) -> Unit, onBack: () -> Unit) {
    var registering by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        error = null
        val cleanEmail = email.trim().lowercase()
        if (registering && displayName.trim().length < 2) {
            error = "نام نمایشی باید دست‌کم دو نویسه باشد."
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            error = "نشانی ایمیل معتبر نیست."
            return
        }
        if (password.length < 6) {
            error = "گذرواژه باید دست‌کم ۶ نویسه داشته باشد."
            return
        }
        if (registering && password != confirmPassword) {
            error = "تکرار گذرواژه با گذرواژه یکسان نیست."
            return
        }
        loading = true
        val repository = FirebaseChatRepository()
        val callback: (Result<String>) -> Unit = { result ->
            loading = false
            result.onSuccess { uid ->
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token -> FirebaseChatRepository().saveDeviceToken(token) }
                onSuccess(uid)
            }
                .onFailure { failure -> error = failure.localizedMessage ?: "عملیات انجام نشد." }
        }
        if (registering) repository.register(cleanEmail, password, displayName.trim(), callback)
        else repository.signIn(cleanEmail, password, callback)
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(AuthNight, Color(0xFF14264A), Color(0xFF1D3457))))) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = AuthIvory) }
                Spacer(Modifier.weight(1f))
                Text("PARS CHAT", color = AuthGold, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text(if (registering) "ساخت حساب تازه" else "ورود به حساب", color = AuthIvory, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("دروازه‌ای امن به گفتگوهای تو", color = Color(0xFFB9C7D9), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
            if (registering) {
                OutlinedTextField(displayName, { displayName = it; error = null }, Modifier.fillMaxWidth(), label = { Text("نام نمایشی") }, singleLine = true, enabled = !loading)
                Spacer(Modifier.height(10.dp))
            }
            OutlinedTextField(email, { email = it; error = null }, Modifier.fillMaxWidth(), label = { Text("ایمیل") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), enabled = !loading)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                password, { password = it; error = null }, Modifier.fillMaxWidth(),
                label = { Text("گذرواژه") }, singleLine = true, enabled = !loading,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } }
            )
            if (registering) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(confirmPassword, { confirmPassword = it; error = null }, Modifier.fillMaxWidth(), label = { Text("تکرار گذرواژه") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), enabled = !loading)
            }
            if (error != null) {
                Text(error!!, color = Color(0xFFFFB4AB), fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
            }
            Spacer(Modifier.height(22.dp))
            Button(onClick = { submit() }, enabled = !loading, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AuthGold, contentColor = AuthNight)) {
                if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = AuthNight, strokeWidth = 2.dp)
                else Text(if (registering) "ایجاد حساب" else "ورود", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            TextButton(onClick = { registering = !registering; error = null }) {
                Text(if (registering) "حساب داری؟ ورود" else "حساب نداری؟ ایجاد حساب", color = Color(0xFFE2CB98))
            }
            Spacer(Modifier.weight(1f))
            Text("ساخته بنیامین نوری دفرازی", color = Color(0xFF9EADC2), fontSize = 11.sp)
        }
    }
}
