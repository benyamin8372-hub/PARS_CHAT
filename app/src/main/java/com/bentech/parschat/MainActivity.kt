package com.bentech.parschat

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.combinedClickable
import androidx.activity.compose.rememberLauncherForActivityResult
import coil.compose.AsyncImage
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import java.io.File
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bentech.parschat.ui.components.DimensionalAvatar
import com.bentech.parschat.ui.components.ParsMessageBubble
import com.bentech.parschat.ui.components.PersianMedallion
import com.bentech.parschat.ui.components.FaravaharChatMark
import com.bentech.parschat.ui.components.GoldRimCard
import com.bentech.parschat.ui.AuthScreen
import com.bentech.parschat.ui.UserDirectoryScreen
import com.bentech.parschat.ui.GroupCreateScreen
import com.bentech.parschat.ui.AdminPanelScreen
import com.bentech.parschat.ui.BlockedUsersScreen
import com.bentech.parschat.ui.GroupManagementScreen
import com.bentech.parschat.FirebaseChatRepository
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Night = Color(0xFF09132F)
private val Navy = Color(0xFF14264F)
private val Gold = Color(0xFFD8B45A)
private val Turquoise = Color(0xFF55C8BC)
private val Cream = Color(0xFFF5F1E7)
private val Ink = Color(0xFF172344)
private data class Person(val name:String,val last:String,val time:String,val unread:Int,val avatar:String,val uid:String="",val conversationId:String="",val isGroup:Boolean=false,val photoUrl:String?=null)
private data class Message(val body:String,val mine:Boolean,val time:String,val id:String="",val senderId:String="",val type:String="text",val mediaUrl:String?=null,val edited:Boolean=false,val deleted:Boolean=false,val readBy:Map<String,Boolean> = emptyMap(),val replyToId:String?=null,val replyToText:String?=null,val forwardedFrom:String?=null)

class MainActivity:ComponentActivity(){
 private val presenceRepository by lazy { FirebaseChatRepository() }
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=android.graphics.Color.rgb(9,19,47);window.navigationBarColor=android.graphics.Color.rgb(9,19,47);setContent{ParsChatApp()}}
 override fun onStart(){super.onStart();if(FirebaseAuth.getInstance().currentUser!=null)presenceRepository.setPresence(true)}
 override fun onStop(){if(FirebaseAuth.getInstance().currentUser!=null)presenceRepository.setPresence(false);super.onStop()}
}
@Composable private fun ParsChatApp(){
 var page by remember{mutableStateOf(if(FirebaseAuth.getInstance().currentUser!=null) "home" else "welcome")}
 var selected by remember{mutableStateOf(Person("","", "",0,""))}
 var contacts by remember{mutableStateOf<List<Person>>(emptyList())}
 var conversationsLoading by remember{mutableStateOf(FirebaseAuth.getInstance().currentUser!=null)}
 var conversationsError by remember{mutableStateOf<String?>(null)}
 var blockedIds by remember{mutableStateOf<Set<String>>(emptySet())}
 var sessionUid by remember{mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid)}
 val repository = remember { FirebaseChatRepository() }
 var conversationId by remember{mutableStateOf<String?>(null)}
 val context= LocalContext.current
 val notificationPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ }
 LaunchedEffect(sessionUid){
  if(sessionUid!=null && android.os.Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
   notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
 }
 var dark by remember{mutableStateOf(true)}
 var name by remember{mutableStateOf(FirebaseAuth.getInstance().currentUser?.displayName ?: "کاربر")}
 var photoUrl by remember{mutableStateOf<String?>(null)}
 var showOnline by remember{mutableStateOf(true)}
 var showLastSeen by remember{mutableStateOf(true)}
 LaunchedEffect(sessionUid){
  val uid=sessionUid
  if(uid!=null) FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener { doc -> name=doc.getString("displayName") ?: name;photoUrl=doc.getString("photoUrl");showOnline=doc.getBoolean("showOnline")!=false;showLastSeen=doc.getBoolean("showLastSeen")!=false }
 }
 DisposableEffect(sessionUid){
  if(sessionUid==null){blockedIds=emptySet();onDispose{}}
  else{
   val blockedListener=repository.observeBlocked(onUpdate={snapshot->blockedIds=snapshot?.documents.orEmpty().map{it.id}.toSet()},onError={})
   onDispose{blockedListener.remove()}
  }
 }
 DisposableEffect(sessionUid){
  val uid=sessionUid
  if(uid==null){
   contacts=emptyList();conversationsLoading=false;conversationsError=null
   onDispose { }
  }else{
   conversationsLoading=true
   val registration=repository.observeConversations(onUpdate={snapshot->
    conversationsLoading=false;conversationsError=null
    contacts=emptyList()
    val documents=snapshot?.documents.orEmpty()
    documents.forEach { conversation ->
     val participants=(conversation.get("participants") as? List<*>)?.filterIsInstance<String>().orEmpty()
     val isGroup=conversation.getString("type")=="group"
     val lastMessage=conversation.getString("lastMessage") ?: "گفتگوی تازه"
     val time=conversation.getTimestamp("lastMessageAt")?.toDate()?.let{SimpleDateFormat("HH:mm",Locale.getDefault()).format(it)} ?: ""
     if(isGroup){
      val groupName=conversation.getString("name") ?: "گروه"
      val item=Person(groupName,lastMessage,time,0,groupName.take(1),"",conversation.id,true)
      if(sessionUid==uid) contacts=(contacts.filterNot{it.conversationId==conversation.id}+item)
     }else{
      val otherUid=participants.firstOrNull { it!=uid } ?: return@forEach
      FirebaseFirestore.getInstance().collection("users").document(otherUid).get().addOnSuccessListener { profile ->
       if(sessionUid==uid && profile.exists()){
        val displayName=profile.getString("displayName")?.takeIf{it.isNotBlank()} ?: "کاربر"
        val item=Person(displayName,lastMessage,time,0,displayName.take(1),otherUid,conversation.id,false,profile.getString("photoUrl"))
        contacts=(contacts.filterNot{it.conversationId==conversation.id}+item)
       }
      }
     }
    }
   },onError={exception->conversationsLoading=false;conversationsError=exception.localizedMessage ?: "دریافت گفتگوها ناموفق بود."})
   onDispose { registration.remove() }
  }
 }
 DisposableEffect(sessionUid, contacts.map { it.conversationId }.sorted().joinToString("|")) {
  val uid=sessionUid
  val listeners=mutableListOf<com.google.firebase.firestore.ListenerRegistration>()
  if(uid!=null){
   contacts.forEach { person ->
    if(person.conversationId.isNotBlank()){
     val registration=repository.observeMessages(person.conversationId,onUpdate={snapshot->
      val unread=snapshot?.documents.orEmpty().count { doc ->
       doc.getString("senderId")!=uid && (doc.get("readBy") as? Map<*,*>)?.get(uid)!=true
      }
      contacts=contacts.map { if(it.conversationId==person.conversationId)it.copy(unread=unread) else it }
     },onError={})
     listeners.add(registration)
    }
   }
  }
  onDispose { listeners.forEach { it.remove() } }
 }
 var search by remember{mutableStateOf("")}
 val chats=remember{mutableStateMapOf<String,MutableList<Message>>()}
 val bg=if(dark)Night else Cream
 val surface=if(dark)Color(0xFF17264A) else Color.White
 val fg=if(dark)Color(0xFFF5F0E4) else Ink
 MaterialTheme(colorScheme=darkColorScheme(primary=Gold,secondary=Turquoise,background=bg,surface=surface,onSurface=fg)){
  when(page){
   "welcome"->Welcome{page="auth"}
   "auth"->AuthScreen(onSuccess={uid->sessionUid=uid;page="home";FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener { doc -> name=doc.getString("displayName") ?: name }},onBack={page="welcome"})
   "home"->Home(bg,surface,fg,name,search,contacts.filter{it.isGroup || it.uid !in blockedIds},conversationsLoading,conversationsError,{search=it},{selected=it;conversationId=it.conversationId;page="chat"},{page="profile"},{page="settings"},{page="calls"},{page="groupCreate"})
   "chat"->Chat(selected,bg,surface,fg,conversationId,contacts,{page="home"},{page="groupManage"})
   "call"->CallPage(selected.name){page="chat"}
   "groupManage"->GroupManagementScreen(selected.conversationId,bg,surface,fg){page="chat"}
   "admin"->AdminPanelScreen(bg,surface,fg){page="settings"}
   "blocked"->BlockedUsersScreen(bg,surface,fg){page="settings"}
   "groupCreate"->GroupCreateScreen(bg,surface,fg,onBack={page="home"},onCreated={id,groupName->selected=Person(groupName,"گروه تازه","",0,groupName.take(1),"",id,true);conversationId=id;page="chat"})
   "calls"->UserDirectoryScreen(bg,surface,fg,onBack={page="home"},onSelect={uid,displayName->FirebaseChatRepository().openConversation(uid){result->result.onSuccess{id->selected=Person(displayName,"گفتگوی تازه","",0,displayName.take(1),uid,id);conversationId=id;page="chat"}.onFailure{error->Toast.makeText(context,error.localizedMessage ?: "گفتگو باز نشد",Toast.LENGTH_LONG).show()}}})
   "settings"->Page("تنظیمات","شخصی‌سازی تجربه پارس چت",bg,fg,{page="home"}){
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(surface).padding(16.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.DarkMode,null,tint=Gold);Text("حالت تاریک",Modifier.weight(1f).padding(start=12.dp),color=fg);Switch(dark,{dark=it})}
     Spacer(Modifier.height(10.dp))
     Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(surface).padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text("نمایش وضعیت آنلاین",Modifier.weight(1f),color=fg);Switch(showOnline,{showOnline=it;repository.updatePrivacy(it,showLastSeen){result->result.onFailure{Toast.makeText(context,it.localizedMessage,Toast.LENGTH_SHORT).show()}}})}
     Spacer(Modifier.height(8.dp))
     Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(surface).padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text("نمایش آخرین بازدید",Modifier.weight(1f),color=fg);Switch(showLastSeen,{showLastSeen=it;repository.updatePrivacy(showOnline,it){result->result.onFailure{Toast.makeText(context,it.localizedMessage,Toast.LENGTH_SHORT).show()}}})}
     Spacer(Modifier.height(8.dp))
     OutlinedButton(onClick={page="blocked"},modifier=Modifier.fillMaxWidth()){Text("مدیریت کاربران مسدودشده")}
     OutlinedButton(onClick={page="admin"},modifier=Modifier.fillMaxWidth()){Text("پنل مدیریت")}

    Spacer(Modifier.height(16.dp));Text("درباره برنامه",color=fg,fontWeight=FontWeight.Bold);Text("PARS CHAT  •  نسخه ۱.۱.۰",color=Gold);Text("ساخته بنیامین نوری دفرازی",color=fg);Spacer(Modifier.height(24.dp));Button(onClick={FirebaseMessaging.getInstance().token.addOnSuccessListener{token->repository.removeDeviceToken(token){repository.setPresence(false){FirebaseAuth.getInstance().signOut();sessionUid=null;page="welcome"}}}.addOnFailureListener{repository.setPresence(false){FirebaseAuth.getInstance().signOut();sessionUid=null;page="welcome"}}},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF8D3D45))){Text("خروج از حساب",color=Color.White)}
   }
   else->Profile(name,photoUrl,{newName->name=newName;repository.updateDisplayName(newName){result->result.onFailure{Toast.makeText(context,it.localizedMessage,Toast.LENGTH_SHORT).show()}}},{uri->repository.uploadProfilePhoto(uri){result->result.onSuccess{photoUrl=it}.onFailure{Toast.makeText(context,it.localizedMessage ?: "بارگذاری عکس ناموفق بود",Toast.LENGTH_LONG).show()}}},bg,surface,fg){page="home"}
  }
 }
}
@Composable private fun Welcome(onGo:()->Unit){
 val inf= rememberInfiniteTransition(label="glow")
 val glow by inf.animateFloat(.25f,.8f,infiniteRepeatable(tween(1800),RepeatMode.Reverse),label="g")
 Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Night,Navy,Color(0xFF263D6A))))){
  Canvas(Modifier.fillMaxSize()){val c=Offset(size.width/2,size.height*.42f);for(i in 1..5)drawCircle(Gold.copy(alpha=glow*.1f),i*52f,c,style= androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()));for(i in 0..15){val a=Math.PI*2*i/16;drawLine(Gold.copy(alpha=.12f),Offset(c.x,c.y),Offset(c.x+kotlin.math.cos(a).toFloat()*300,c.y+kotlin.math.sin(a).toFloat()*300),1.dp.toPx())}}
  Column(Modifier.fillMaxSize().padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
   ParsBrandLockup(emblemSize=116.dp,glow=glow)
   Text("شکوهِ گذشته، پیوندِ امروز",color=Turquoise,fontSize=16.sp,modifier=Modifier.padding(top=20.dp))
   Text("پیام‌رسانی با الهام از تمدن ایران‌زمین",color=Color(0xFFD5DCEC),fontSize=13.sp,modifier=Modifier.padding(top=12.dp))
   Spacer(Modifier.height(48.dp));Button(onClick=onGo,modifier=Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=Gold)){Text("ورود به پارس چت",color=Night,fontWeight=FontWeight.Bold,fontSize=16.sp)}
   Spacer(Modifier.height(36.dp));Text("ساخته بنیامین نوری دفرازی",color=Color(0xFFB8C5DD),fontSize=12.sp)
  }
 }
}
@Composable private fun Home(bg:Color,surface:Color,fg:Color,name:String,search:String,contacts:List<Person>,loading:Boolean,error:String?,onSearch:(String)->Unit,onOpen:(Person)->Unit,onProfile:()->Unit,onSettings:()->Unit,onCalls:()->Unit,onCreateGroup:()->Unit){
 var filter by remember { mutableStateOf("همه") }
 val filtered = contacts.filter { person ->
  val matchesSearch = person.name.contains(search, ignoreCase = true) || person.last.contains(search, ignoreCase = true)
  val matchesFilter = when(filter) { "خوانده‌نشده" -> person.unread > 0; "گروه‌ها" -> person.isGroup; else -> true }
  matchesSearch && matchesFilter
 }
 Scaffold(containerColor=bg,topBar={
  Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Night,Color(0xFF1B3155),Night))).padding(horizontal=18.dp,vertical=14.dp),verticalAlignment=Alignment.CenterVertically){
   FaravaharChatMark(diameter=52.dp)
   Column(Modifier.weight(1f).padding(start=12.dp)){
    Text("چت پارس",color=Gold,fontWeight=FontWeight.Black,fontSize=18.sp)
    Text("PARS CHAT  •  درود، $name",color=Color(0xFFD7DFEA),fontSize=10.sp)
   }
   IconButton(onProfile){Icon(Icons.Default.AccountCircle,null,tint=Gold)}
   IconButton(onSettings){Icon(Icons.Default.Settings,null,tint=Gold)}
  }
 },bottomBar={
  NavigationBar(containerColor=surface,contentColor=fg){
   NavigationBarItem(selected=true,onClick={},icon={Icon(Icons.Default.ChatBubble,null)},label={Text("گفتگوها")})
   NavigationBarItem(selected=false,onClick=onCalls,icon={Icon(Icons.Default.People,null)},label={Text("مخاطبان")})
   NavigationBarItem(selected=false,onClick=onCreateGroup,icon={Icon(Icons.Default.GroupAdd,null)},label={Text("گروه")})
   NavigationBarItem(selected=false,onClick=onSettings,icon={Icon(Icons.Default.Tune,null)},label={Text("تنظیمات")})
  }
 },floatingActionButton={
  FloatingActionButton(onClick=onCalls,containerColor=Gold,contentColor=Night,shape=RoundedCornerShape(18.dp)){Icon(Icons.Default.Edit,null)}
 }){ padding ->
  Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=16.dp)){
   Spacer(Modifier.height(18.dp))
   Row(verticalAlignment=Alignment.CenterVertically){
    Column(Modifier.weight(1f)){
     Text("گفتگوهای تو",color=fg,fontSize=25.sp,fontWeight=FontWeight.Bold)
     Text("پیوندی تازه در امتداد تاریخ",color=Color.Gray,fontSize=12.sp,modifier=Modifier.padding(top=3.dp))
    }
    GoldRimCard(radius=14.dp){
     Text(contacts.size.toString(),color=Color(0xFFE9D29B),fontSize=19.sp,fontWeight=FontWeight.Bold)
     Text("گفتگو",color=Color(0xFFCBD4E1),fontSize=9.sp)
    }
   }
   Row(Modifier.fillMaxWidth().padding(top=16.dp).clip(RoundedCornerShape(16.dp)).background(surface).border(1.dp,Gold.copy(alpha=.22f),RoundedCornerShape(16.dp)).padding(horizontal=13.dp,vertical=13.dp),verticalAlignment=Alignment.CenterVertically){
    Icon(Icons.Default.Search,null,tint=Turquoise)
    BasicTextField(value=search,onValueChange=onSearch,modifier=Modifier.weight(1f).padding(start=10.dp),singleLine=true,textStyle=TextStyle(color=fg,fontSize=14.sp),decorationBox={ inner -> if(search.isEmpty())Text("جستجو در گفتگوها و پیام‌ها...",color=Color.Gray,fontSize=13.sp);inner() })
    if(search.isNotEmpty()) IconButton(onClick={onSearch("")},modifier=Modifier.size(24.dp)){Icon(Icons.Default.Close,null,tint=Color.Gray)}
   }
   Row(Modifier.fillMaxWidth().padding(top=17.dp,bottom=12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
    listOf("همه","خوانده‌نشده","گروه‌ها").forEach { item ->
     val selected = filter == item
     Box(Modifier.clip(RoundedCornerShape(50)).background(if(selected)Gold.copy(alpha=.18f) else surface).border(1.dp,if(selected)Gold else Color.White.copy(alpha=.08f),RoundedCornerShape(50)).clickable { filter=item }.padding(horizontal=15.dp,vertical=9.dp)){
      Text(item,color=if(selected)Gold else fg,fontSize=12.sp,fontWeight=if(selected)FontWeight.Bold else FontWeight.Medium)
     }
    }
   }
   Row(Modifier.fillMaxWidth().padding(bottom=9.dp),verticalAlignment=Alignment.CenterVertically){
    Text(if(filter=="گروه‌ها")"گروه‌های من" else if(filter=="خوانده‌نشده")"پیام‌های تازه" else "همه گفتگوها",color=fg,fontWeight=FontWeight.Bold,fontSize=14.sp)
    Spacer(Modifier.weight(1f))
    Text(filtered.size.toString()+" مورد",color=Color.Gray,fontSize=11.sp)
   }
   if(error!=null)Text(error,color=Color(0xFFFFB4AB),fontSize=11.sp,modifier=Modifier.padding(vertical=6.dp))
   if(loading){
    Box(Modifier.fillMaxWidth().weight(1f),contentAlignment=Alignment.Center){CircularProgressIndicator(color=Gold)}
   }else if(filtered.isEmpty()){
    Box(Modifier.fillMaxWidth().weight(1f),contentAlignment=Alignment.Center){
     Column(horizontalAlignment=Alignment.CenterHorizontally){
      PersianMedallion(diameter=76.dp,symbol=if(filter=="گروه‌ها")"𐎠" else "⌕")
      Spacer(Modifier.height(16.dp))
      Text(when(filter){"گروه‌ها"->"هنوز گروهی ساخته نشده";"خوانده‌نشده"->"پیام خوانده‌نشده‌ای نیست";else->if(search.isBlank())"هنوز گفتگویی نداری" else "نتیجه‌ای پیدا نشد"},color=fg,fontWeight=FontWeight.Bold,fontSize=16.sp)
      Text(when(filter){"گروه‌ها"->"گروه‌های گفت‌وگو پس از ایجاد اینجا نمایش داده می‌شوند";"خوانده‌نشده"->"همه پیام‌ها بررسی شده‌اند";else->if(search.isBlank())"برای شروع، مخاطبان را از دکمه پایین پیدا کن" else "عبارت دیگری را برای جستجو امتحان کن"},color=Color.Gray,fontSize=11.sp,textAlign=TextAlign.Center,modifier=Modifier.padding(top=7.dp))
     }
    }
   }else{
    LazyColumn(verticalArrangement=Arrangement.spacedBy(9.dp),contentPadding=PaddingValues(bottom=88.dp)){
     items(filtered,key={it.conversationId}){person->PersonRow(person,surface,fg){onOpen(person)}}
    }
   }
  }
 }
}
@Composable private fun PersonRow(person:Person,surface:Color,fg:Color,onClick:()->Unit){
 Row(Modifier.fillMaxWidth().shadow(2.dp,RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(surface).clickable(onClick=onClick).padding(13.dp),verticalAlignment=Alignment.CenterVertically){
  if(person.photoUrl!=null)AsyncImage(model=person.photoUrl,contentDescription="تصویر ${person.name}",modifier=Modifier.size(50.dp).clip(CircleShape)) else DimensionalAvatar(label=person.avatar,diameter=50.dp,online=false)
  Column(Modifier.weight(1f).padding(start=12.dp)){Text(person.name,color=fg,fontWeight=FontWeight.Bold);Text(person.last,color=Color.Gray,fontSize=12.sp,maxLines=1)}
  Column(horizontalAlignment=Alignment.End){Text(person.time,color=Color.Gray,fontSize=10.sp);if(person.unread>0){Spacer(Modifier.height(5.dp));Box(Modifier.size(19.dp).background(Turquoise,CircleShape),contentAlignment=Alignment.Center){Text(person.unread.toString(),color=Night,fontSize=10.sp)}}}
 }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable private fun Chat(person:Person,bg:Color,surface:Color,fg:Color,conversationId:String?,allConversations:List<Person>,back:()->Unit,manageGroup:()->Unit){
 var draft by remember{mutableStateOf("")}
 val repository=remember{FirebaseChatRepository()}
 val currentUid=remember{repository.currentUserId()}
 val context=LocalContext.current
 var liveMessages by remember(conversationId){mutableStateOf<List<Message>>(emptyList())}
 var chatError by remember(conversationId){mutableStateOf<String?>(null)}
 var selectedMessage by remember{mutableStateOf<Message?>(null)}
 var editTarget by remember{mutableStateOf<Message?>(null)}
 var editText by remember{mutableStateOf("")}
 var replyTarget by remember{mutableStateOf<Message?>(null)}
 var forwardTarget by remember{mutableStateOf<Message?>(null)}
 var blockConfirm by remember{mutableStateOf(false)}
 var showChatMenu by remember{mutableStateOf(false)}
 var uploadBusy by remember{mutableStateOf(false)}
 var recording by remember{mutableStateOf(false)}
 var recorder by remember{mutableStateOf<MediaRecorder?>(null)}
 var recordingFile by remember{mutableStateOf<File?>(null)}
 val microphonePermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->
  if(granted){
   try{
    val file=File(context.cacheDir,"pars_voice_${System.currentTimeMillis()}.m4a")
    val media=MediaRecorder().apply{setAudioSource(MediaRecorder.AudioSource.MIC);setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);setAudioEncoder(MediaRecorder.AudioEncoder.AAC);setOutputFile(file.absolutePath);prepare();start()}
    recorder=media;recordingFile=file;recording=true
   }catch(error:Exception){chatError=error.localizedMessage ?: "شروع ضبط ممکن نشد."}
  }else chatError="مجوز میکروفون داده نشد."
 }
 val beginRecording:()->Unit={
  try{
   val file=File(context.cacheDir,"pars_voice_${System.currentTimeMillis()}.m4a")
   val media=MediaRecorder().apply{setAudioSource(MediaRecorder.AudioSource.MIC);setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);setAudioEncoder(MediaRecorder.AudioEncoder.AAC);setOutputFile(file.absolutePath);prepare();start()}
   recorder=media;recordingFile=file;recording=true
  }catch(error:Exception){chatError=error.localizedMessage ?: "شروع ضبط ممکن نشد."}
 }
 val finishRecording:()->Unit={
  try{
   recorder?.stop();recorder?.release();recorder=null;recording=false
   val file=recordingFile
   if(file!=null && file.exists() && conversationId!=null){
    uploadBusy=true
    repository.uploadMedia(conversationId,Uri.fromFile(file),"audio/mp4"){result->uploadBusy=false;file.delete();result.onFailure{chatError=it.localizedMessage ?: "ارسال پیام صوتی ناموفق بود."}}
   }
  }catch(error:Exception){recorder?.release();recorder=null;recording=false;chatError="ضبط صوت خیلی کوتاه بود یا متوقف نشد."}
 }
 DisposableEffect(Unit){onDispose{recorder?.release();recorder=null}}
 val imagePicker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->
  if(uri!=null && conversationId!=null){
   uploadBusy=true
   val mime=context.contentResolver.getType(uri) ?: "image/jpeg"
   repository.uploadMedia(conversationId,uri,mime){result->uploadBusy=false;result.onFailure{chatError=it.localizedMessage ?: "بارگذاری تصویر ناموفق بود."}}
  }
 }
 val audioPicker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->
  if(uri!=null && conversationId!=null){
   uploadBusy=true
   val mime=context.contentResolver.getType(uri) ?: "audio/mpeg"
   repository.uploadMedia(conversationId,uri,mime){result->uploadBusy=false;result.onFailure{chatError=it.localizedMessage ?: "بارگذاری صوت ناموفق بود."}}
  }
 }
 DisposableEffect(conversationId){
  if(conversationId==null){onDispose{}}
  else{
   val registration=repository.observeMessages(conversationId,onUpdate={snapshot->
    val docs=snapshot?.documents.orEmpty()
    liveMessages=docs.map{doc->
     val date=doc.getTimestamp("createdAt")?.toDate()
     val time=date?.let{SimpleDateFormat("HH:mm",Locale.getDefault()).format(it)} ?: ""
     val readBy=(doc.get("readBy") as? Map<*,*>)?.mapNotNull{(k,v)->if(k is String && v is Boolean)k to v else null}?.toMap().orEmpty()
     Message(doc.getString("text").orEmpty(),doc.getString("senderId")==currentUid,time,doc.id,doc.getString("senderId").orEmpty(),
      doc.getString("type") ?: "text",doc.getString("mediaUrl"),doc.getTimestamp("editedAt")!=null,doc.getBoolean("deleted")==true,readBy,
      doc.getString("replyToId"),doc.getString("replyToText"),doc.getString("forwardedFrom"))
    }
    val unread=docs.filter{it.getString("senderId")!=currentUid && (it.get("readBy") as? Map<*,*>)?.get(currentUid)!=true}.map{it.id}.take(400)
    if(unread.isNotEmpty())repository.markRead(conversationId,unread)
   },onError={chatError=it.localizedMessage ?: "دریافت پیام‌ها ناموفق بود."})
   onDispose{registration.remove()}
  }
 }
 Scaffold(containerColor=bg,topBar={
  Row(
   Modifier.fillMaxWidth()
    .background(Brush.horizontalGradient(listOf(Color(0xFF071126),Color(0xFF1B3158),Color(0xFF0A1730))))
    .border(width=1.dp,color=Gold.copy(alpha=.28f))
    .padding(horizontal=8.dp,vertical=10.dp),
   verticalAlignment=Alignment.CenterVertically
  ){
   IconButton(back){Icon(Icons.Default.ArrowBack,null,tint=Color(0xFFF3E6C4))}
   if(person.photoUrl!=null)AsyncImage(model=person.photoUrl,contentDescription="تصویر ${person.name}",modifier=Modifier.size(43.dp).clip(CircleShape).border(1.dp,Gold,CircleShape))
   else DimensionalAvatar(label=person.avatar,diameter=43.dp,online=false)
   Column(Modifier.weight(1f).padding(start=10.dp)){
    Text(person.name,color=Color(0xFFF6EEDC),fontWeight=FontWeight.Bold,fontSize=15.sp,maxLines=1)
    Text(if(person.isGroup)"گفتگوی گروهی" else "گفتگوی خصوصی  •  PARS CHAT",color=Turquoise,fontSize=10.sp)
   }
   if(person.isGroup)IconButton(manageGroup){Icon(Icons.Default.Group,null,tint=Gold)}
   else if(person.uid.isNotBlank())IconButton({blockConfirm=true}){Icon(Icons.Default.Block,null,tint=Color(0xFFE6A0A0))}
   Box{
    IconButton({showChatMenu=true}){Icon(Icons.Default.MoreVert,null,tint=Color(0xFFF3E6C4))}
    DropdownMenu(expanded=showChatMenu,onDismissRequest={showChatMenu=false}){
     if(person.isGroup)DropdownMenuItem(text={Text("مدیریت گروه")},onClick={showChatMenu=false;manageGroup()})
     else if(person.uid.isNotBlank())DropdownMenuItem(text={Text("مسدودسازی کاربر")},onClick={showChatMenu=false;blockConfirm=true})
     DropdownMenuItem(text={Text("بازگشت به گفتگوها")},onClick={showChatMenu=false;back()})
    }
   }
  }
 },bottomBar={
  Column(
   Modifier.fillMaxWidth()
    .background(Brush.verticalGradient(listOf(Color(0xFF1A2B4C),Color(0xFF0B1731))))
    .border(width=1.dp,color=Gold.copy(alpha=.22f))
    .navigationBarsPadding()
  ){
   if(recording)Text("در حال ضبط پیام صوتی… برای ارسال، توقف را بزنید",color=Color(0xFFFFB7B7),fontSize=11.sp,modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=5.dp))
   if(replyTarget!=null)Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=4.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF263B5D)).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically){
    Column(Modifier.weight(1f)){Text("پاسخ به پیام",color=Gold,fontSize=10.sp);Text(replyTarget!!.body,color=fg,fontSize=12.sp,maxLines=1)}
    IconButton({replyTarget=null}){Icon(Icons.Default.Close,null,tint=Color.Gray)}
   }
   Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically){
    IconButton(onClick={imagePicker.launch("image/*")},enabled=!uploadBusy){Icon(Icons.Default.Image,null,tint=Turquoise)}
    IconButton(onClick={audioPicker.launch("audio/*")},enabled=!uploadBusy){Icon(Icons.Default.AttachFile,null,tint=Turquoise)}
    IconButton(onClick={if(recording)finishRecording() else if(ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)beginRecording() else microphonePermission.launch(Manifest.permission.RECORD_AUDIO)},enabled=!uploadBusy){Icon(if(recording)Icons.Default.Stop else Icons.Default.Mic,null,tint=if(recording)Color(0xFFE57373) else Turquoise)}
    Row(
     Modifier.weight(1f).clip(RoundedCornerShape(22.dp))
      .background(Color(0xFF0D1B36))
      .border(1.dp,Gold.copy(alpha=.35f),RoundedCornerShape(22.dp))
      .padding(horizontal=13.dp,vertical=5.dp),
     verticalAlignment=Alignment.CenterVertically
    ){
     BasicTextField(draft,{draft=it},Modifier.weight(1f).padding(vertical=9.dp),textStyle=TextStyle(color=Color(0xFFF5F0E4),fontSize=14.sp),decorationBox={inner->if(draft.isEmpty())Text("پیام خود را بنویسید…",color=Color(0xFF9BAAC1),fontSize=13.sp);inner()})
     Icon(Icons.Default.EmojiEmotions,null,tint=Color(0xFF9BAAC1))
    }
    IconButton(onClick={
     val body=draft.trim()
     if(body.isNotEmpty() && conversationId!=null){
      repository.sendText(conversationId,body,replyTarget?.id,replyTarget?.body){result->
       result.onSuccess{draft="";replyTarget=null}.onFailure{chatError=it.localizedMessage ?: "ارسال پیام ناموفق بود."}
      }
     }
    },enabled=draft.isNotBlank() && !uploadBusy){Box(Modifier.size(42.dp).shadow(6.dp,CircleShape).background(Brush.linearGradient(listOf(Color(0xFFF0D58E),Gold,Color(0xFFB88B43))),CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Default.Send,null,tint=Night)}}
   }
  }
 }){padding->
  LazyColumn(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0A1630),Color(0xFF101F3B),Color(0xFF0A142B)))).padding(padding).padding(horizontal=13.dp),contentPadding=PaddingValues(vertical=16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
   item{Text("امروز",Modifier.fillMaxWidth(),color=Color.Gray,textAlign=TextAlign.Center,fontSize=11.sp)}
   if(chatError!=null)item{Text(chatError!!,Modifier.fillMaxWidth(),color=Color(0xFFFFB4AB),textAlign=TextAlign.Center,fontSize=11.sp)}
   if(uploadBusy)item{LinearProgressIndicator(Modifier.fillMaxWidth(),color=Gold)}
   items(liveMessages,key={it.id}){m->
    Row(Modifier.fillMaxWidth(),horizontalArrangement=if(m.mine)Arrangement.End else Arrangement.Start){
     Column(Modifier.widthIn(max=300.dp).combinedClickable(onClick={},onLongClick={selectedMessage=m})){
      if(m.forwardedFrom!=null)Text("فوروارد شده",color=Gold,fontSize=9.sp,modifier=Modifier.padding(start=8.dp,bottom=3.dp))
      if(m.replyToText!=null)Text("↳ ${m.replyToText}",color=Turquoise,fontSize=10.sp,modifier=Modifier.padding(start=8.dp,bottom=3.dp))
      when {
       m.deleted -> ParsMessageBubble(message="این پیام حذف شد",time=m.time,outgoing=m.mine,modifier=Modifier.fillMaxWidth())
       m.type=="image" -> {if(m.mediaUrl!=null)AsyncImage(model=m.mediaUrl,contentDescription="تصویر پیام",modifier=Modifier.fillMaxWidth().heightIn(max=220.dp).clip(RoundedCornerShape(15.dp)))}
       m.type=="audio" -> {Row(Modifier.clip(RoundedCornerShape(16.dp)).background(surface).padding(14.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onClick={
        m.mediaUrl?.let{url->try{MediaPlayer().apply{setDataSource(url);setOnPreparedListener{player->player.start()};setOnCompletionListener{player->player.release()};prepareAsync()}}catch(e:Exception){chatError="پخش پیام صوتی ممکن نشد."}}
       }){Icon(Icons.Default.PlayArrow,null,tint=Gold)};Text("پیام صوتی",color=fg)}}
       else -> ParsMessageBubble(message=m.body,time=m.time,outgoing=m.mine,modifier=Modifier.fillMaxWidth())
      }

      if(m.mine && m.type=="text")Text(if(m.readBy.keys.any{it!=currentUid})"خوانده‌شده" else "ارسال‌شده",color=if(m.readBy.keys.any{it!=currentUid})Turquoise else Color.Gray,fontSize=9.sp,modifier=Modifier.align(Alignment.End).padding(end=5.dp))
      if(m.edited)Text("ویرایش‌شده",color=Color.Gray,fontSize=9.sp,modifier=Modifier.align(Alignment.End))
    }
   }
  }
 }
 if(selectedMessage!=null){
  val msg=selectedMessage!!
  AlertDialog(onDismissRequest={selectedMessage=null},title={Text("عملیات پیام")},text={Text(msg.body.take(180))},confirmButton={
   Row{
    TextButton(onClick={replyTarget=msg;selectedMessage=null}){Text("پاسخ")}
    if(msg.mine && !msg.deleted && msg.type=="text")TextButton(onClick={editTarget=msg;editText=msg.body;selectedMessage=null}){Text("ویرایش")}
    if(msg.mine && !msg.deleted)TextButton(onClick={conversationId?.let{id->repository.deleteMessage(id,msg.id){it.onFailure{e->chatError=e.localizedMessage}}};selectedMessage=null}){Text("حذف")}
    if(msg.type=="text" && !msg.deleted)TextButton(onClick={forwardTarget=msg;selectedMessage=null}){Text("فوروارد")}
   }
  },dismissButton={TextButton(onClick={selectedMessage=null}){Text("بستن")}})
 }
 if(editTarget!=null){
  AlertDialog(onDismissRequest={editTarget=null},title={Text("ویرایش پیام")},text={OutlinedTextField(editText,{editText=it},label={Text("متن پیام")})},confirmButton={
   TextButton(onClick={conversationId?.let{id->repository.editMessage(id,editTarget!!.id,editText){it.onFailure{e->chatError=e.localizedMessage}}};editTarget=null}){Text("ذخیره")}
  },dismissButton={TextButton(onClick={editTarget=null}){Text("انصراف")}})
 }
 if(forwardTarget!=null){
  AlertDialog(onDismissRequest={forwardTarget=null},title={Text("انتخاب مقصد فوروارد")},text={
   Column { allConversations.filter{it.conversationId!=conversationId}.forEach{target->
    TextButton(onClick={repository.forwardMessage(target.conversationId,forwardTarget!!.body,forwardTarget!!.id){it.onFailure{e->chatError=e.localizedMessage}};forwardTarget=null}){Text(target.name)}
   };if(allConversations.none{it.conversationId!=conversationId})Text("گفتگوی دیگری برای فوروارد وجود ندارد.") }
  },confirmButton={},dismissButton={TextButton(onClick={forwardTarget=null}){Text("بستن")}})
 }
 if(blockConfirm){
  AlertDialog(onDismissRequest={blockConfirm=false},title={Text("مسدودسازی کاربر")},text={Text("با مسدودکردن ${person.name}، شروع گفتگو از سمت این حساب محدود می‌شود.")},confirmButton={
   TextButton(onClick={repository.blockUser(person.uid){result->result.onSuccess{Toast.makeText(context,"کاربر مسدود شد",Toast.LENGTH_SHORT).show();back()}.onFailure{chatError=it.localizedMessage}};blockConfirm=false}){Text("مسدود کن")}
  },dismissButton={TextButton(onClick={blockConfirm=false}){Text("انصراف")}})
 }
 }
}
@Composable private fun CallPage(name:String,end:()->Unit){Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Night,Navy))),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.size(126.dp).background(Brush.linearGradient(listOf(Gold,Turquoise)),CircleShape),contentAlignment=Alignment.Center){Text(name.take(1),color=Night,fontSize=52.sp,fontWeight=FontWeight.Bold)};Spacer(Modifier.height(22.dp));Text(name,color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("تماس نمایشی",color=Gold,modifier=Modifier.padding(8.dp));Text("این نسخه هنوز به سرویس تماس واقعی متصل نیست.",color=Color.LightGray,fontSize=11.sp);Spacer(Modifier.height(60.dp));IconButton(end,Modifier.size(64.dp).background(Color(0xFFE14D5A),CircleShape)){Icon(Icons.Default.CallEnd,null,tint=Color.White)}}}}
@Composable private fun Page(title:String,sub:String,bg:Color,fg:Color,back:()->Unit,content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().background(bg).padding(18.dp)){Row(verticalAlignment=Alignment.CenterVertically){IconButton(back){Icon(Icons.Default.ArrowBack,null,tint=fg)};Text(title,color=fg,fontSize=24.sp,fontWeight=FontWeight.Bold)};Text(sub,color=Color.Gray,modifier=Modifier.padding(start=8.dp,bottom=16.dp));content()}}
@Composable private fun Profile(name:String,photoUrl:String?,onName:(String)->Unit,onPhoto:(Uri)->Unit,bg:Color,surface:Color,fg:Color,back:()->Unit){
 var draft by remember(name){mutableStateOf(name)}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->if(uri!=null)onPhoto(uri)}
 Page("نمایه من","اطلاعات نمایشی حساب",bg,fg,back){
  Box(Modifier.fillMaxWidth().padding(20.dp),contentAlignment=Alignment.Center){
   if(photoUrl!=null)AsyncImage(model=photoUrl,contentDescription="تصویر نمایه",modifier=Modifier.size(104.dp).clip(CircleShape))
   else Box(Modifier.size(104.dp).background(Brush.linearGradient(listOf(Gold,Turquoise)),CircleShape),contentAlignment=Alignment.Center){Text(draft.take(1),color=Night,fontSize=42.sp,fontWeight=FontWeight.Bold)}
  }
  OutlinedButton(onClick={picker.launch("image/*")},modifier=Modifier.fillMaxWidth()){Text("انتخاب تصویر نمایه")}
  OutlinedTextField(draft,{draft=it},Modifier.fillMaxWidth().padding(top=12.dp),label={Text("نام نمایشی")},singleLine=true)
  Button({if(draft.isNotBlank())onName(draft.trim())},Modifier.fillMaxWidth().padding(top=12.dp),colors=ButtonDefaults.buttonColors(containerColor=Gold)){Text("ذخیره تغییرات",color=Night,fontWeight=FontWeight.Bold)}
  Text("سازنده: بنیامین نوری دفرازی",color=Color.Gray,modifier=Modifier.padding(top=28.dp),fontSize=12.sp)
 }
}
