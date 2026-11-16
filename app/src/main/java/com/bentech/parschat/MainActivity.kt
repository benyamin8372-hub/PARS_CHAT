package com.bentech.parschat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import com.bentech.parschat.ui.components.GoldRimCard
import com.bentech.parschat.ui.AuthScreen
import com.bentech.parschat.ui.UserDirectoryScreen
import com.bentech.parschat.FirebaseChatRepository
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
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
private data class Person(val name:String,val last:String,val time:String,val unread:Int,val avatar:String,val uid:String="")
private data class Message(val body:String,val mine:Boolean,val time:String)
private val contacts = listOf(
 Person("ماندانا","فردا برای دیدار آماده‌ای؟","۱۱:۴۲",2,"م"),
 Person("آریا","عکس‌های تخت جمشید عالی شدند","۱۰:۱۸",0,"آ"),
 Person("کوروش","یک پیام تازه برایت دارم","دیروز",1,"ک"),
 Person("آتوسا","سپاس از همراهی‌ات 🌿","دیروز",0,"آ"),
 Person("داریوش","جلسه را ساعت پنج بگذاریم","دوشنبه",0,"د"),
 Person("رکسانا","شب بخیر ✨","یکشنبه",0,"ر"),
 Person("بهرام","بعداً تماس می‌گیرم","شنبه",0,"ب"),
 Person("شیرین","چه خبرهای خوبی!","شنبه",0,"ش")
)
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=android.graphics.Color.rgb(9,19,47);window.navigationBarColor=android.graphics.Color.rgb(9,19,47);setContent{ParsChatApp()}}
}
@Composable private fun ParsChatApp(){
 var page by remember{mutableStateOf(if(FirebaseAuth.getInstance().currentUser!=null) "home" else "welcome")}
 var selected by remember{mutableStateOf(contacts[0])}
 var conversationId by remember{mutableStateOf<String?>(null)}
 val context= LocalContext.current
 var dark by remember{mutableStateOf(true)}
 var name by remember{mutableStateOf(FirebaseAuth.getInstance().currentUser?.displayName ?: "کاربر")}
 LaunchedEffect(Unit){
  val uid=FirebaseAuth.getInstance().currentUser?.uid
  if(uid!=null) FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener { doc -> name=doc.getString("displayName") ?: name }
 }
 var search by remember{mutableStateOf("")}
 val chats=remember{mutableStateMapOf<String,MutableList<Message>>().apply{contacts.forEach{put(it.name, mutableListOf(Message("درود! خوشحالم که اینجا هستی.",false,"۱۱:۲۰"),Message(it.last,false,it.time),Message("سپاس، به‌زودی پاسخ می‌دهم.",true,"۱۱:۳۵")))}}}
 val bg=if(dark)Night else Cream
 val surface=if(dark)Color(0xFF17264A) else Color.White
 val fg=if(dark)Color(0xFFF5F0E4) else Ink
 MaterialTheme(colorScheme=darkColorScheme(primary=Gold,secondary=Turquoise,background=bg,surface=surface,onSurface=fg)){
  when(page){
   "welcome"->Welcome{page="auth"}
   "auth"->AuthScreen(onSuccess={uid->page="home";FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener { doc -> name=doc.getString("displayName") ?: name }},onBack={page="welcome"})
   "home"->Home(bg,surface,fg,name,search,{search=it},{selected=it;conversationId=null;page="chat"},{page="profile"},{page="settings"},{page="calls"})
   "chat"->Chat(selected,bg,surface,fg,conversationId,chats[selected.name]?: mutableListOf(),{page="home"},{page="call"}){body->
    val list=chats[selected.name]?: mutableListOf<Message>().also{chats[selected.name]=it}
    list.add(Message(body,true,SimpleDateFormat("HH:mm",Locale.getDefault()).format(Date())))
   }
   "call"->CallPage(selected.name){page="chat"}
   "calls"->UserDirectoryScreen(bg,surface,fg,onBack={page="home"},onSelect={uid,displayName->FirebaseChatRepository().openConversation(uid){result->result.onSuccess{id->selected=Person(displayName,"","",0,displayName.take(1),uid);conversationId=id;page="chat"}.onFailure{error->Toast.makeText(context,error.localizedMessage ?: "گفتگو باز نشد",Toast.LENGTH_LONG).show()}}})
   "settings"->Page("تنظیمات","شخصی‌سازی تجربه پارس چت",bg,fg,{page="home"}){
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(surface).padding(16.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.DarkMode,null,tint=Gold);Text("حالت تاریک",Modifier.weight(1f).padding(start=12.dp),color=fg);Switch(dark,{dark=it})}
    Spacer(Modifier.height(16.dp));Text("درباره برنامه",color=fg,fontWeight=FontWeight.Bold);Text("PARS CHAT  •  نسخه ۱.۱.۰",color=Gold);Text("ساخته بنیامین نوری دفرازی",color=fg);Spacer(Modifier.height(24.dp));Button(onClick={FirebaseAuth.getInstance().signOut();page="welcome"},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF8D3D45))){Text("خروج از حساب",color=Color.White)}
   }
   else->Profile(name,{newName->name=newName;FirebaseAuth.getInstance().currentUser?.uid?.let{uid->FirebaseFirestore.getInstance().collection("users").document(uid).update("displayName",newName)}},bg,surface,fg){page="home"}
  }
 }
}
@Composable private fun Welcome(onGo:()->Unit){
 val inf= rememberInfiniteTransition(label="glow")
 val glow by inf.animateFloat(.25f,.8f,infiniteRepeatable(tween(1800),RepeatMode.Reverse),label="g")
 Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Night,Navy,Color(0xFF263D6A))))){
  Canvas(Modifier.fillMaxSize()){val c=Offset(size.width/2,size.height*.42f);for(i in 1..5)drawCircle(Gold.copy(alpha=glow*.1f),i*52f,c,style= androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()));for(i in 0..15){val a=Math.PI*2*i/16;drawLine(Gold.copy(alpha=.12f),Offset(c.x,c.y),Offset(c.x+kotlin.math.cos(a).toFloat()*300,c.y+kotlin.math.sin(a).toFloat()*300),1.dp.toPx())}}
  Column(Modifier.fillMaxSize().padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
   Box(Modifier.size(148.dp).shadow(24.dp,CircleShape).background(Brush.radialGradient(listOf(Color(0xFF354D79),Night)),CircleShape).border(1.dp,Gold.copy(alpha=glow),CircleShape),contentAlignment=Alignment.Center){Text("𐎠",fontSize=82.sp,color=Gold)}
   Spacer(Modifier.height(28.dp));Text("PARS CHAT",fontSize=31.sp,color=Gold,fontWeight=FontWeight.Black,letterSpacing=4.sp)
   Text("شکوهِ گذشته، پیوندِ امروز",color=Turquoise,fontSize=16.sp,modifier=Modifier.padding(top=10.dp))
   Text("پیام‌رسانی با الهام از تمدن ایران‌زمین",color=Color(0xFFD5DCEC),fontSize=13.sp,modifier=Modifier.padding(top=12.dp))
   Spacer(Modifier.height(48.dp));Button(onClick=onGo,modifier=Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=Gold)){Text("ورود به پارس چت",color=Night,fontWeight=FontWeight.Bold,fontSize=16.sp)}
   Spacer(Modifier.height(36.dp));Text("ساخته بنیامین نوری دفرازی",color=Color(0xFFB8C5DD),fontSize=12.sp)
  }
 }
}
@Composable private fun Home(bg:Color,surface:Color,fg:Color,name:String,search:String,onSearch:(String)->Unit,onOpen:(Person)->Unit,onProfile:()->Unit,onSettings:()->Unit,onCalls:()->Unit){
 var filter by remember { mutableStateOf("همه") }
 val filtered = contacts.filter { person ->
  val matchesSearch = person.name.contains(search, ignoreCase = true) || person.last.contains(search, ignoreCase = true)
  val matchesFilter = when(filter) { "خوانده‌نشده" -> person.unread > 0; "گروه‌ها" -> false; else -> true }
  matchesSearch && matchesFilter
 }
 Scaffold(containerColor=bg,topBar={
  Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Night,Color(0xFF1B3155),Night))).padding(horizontal=18.dp,vertical=14.dp),verticalAlignment=Alignment.CenterVertically){
   PersianMedallion(diameter=48.dp)
   Column(Modifier.weight(1f).padding(start=12.dp)){
    Text("PARS CHAT",color=Gold,fontWeight=FontWeight.Black,fontSize=18.sp,letterSpacing=2.sp)
    Text("درود، $name",color=Color(0xFFD7DFEA),fontSize=12.sp)
   }
   IconButton(onProfile){Icon(Icons.Default.AccountCircle,null,tint=Gold)}
   IconButton(onSettings){Icon(Icons.Default.Settings,null,tint=Gold)}
  }
 },bottomBar={
  NavigationBar(containerColor=surface,contentColor=fg){
   NavigationBarItem(selected=true,onClick={},icon={Icon(Icons.Default.ChatBubble,null)},label={Text("گفتگوها")})
   NavigationBarItem(selected=false,onClick=onCalls,icon={Icon(Icons.Default.People,null)},label={Text("مخاطبان")})
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
   if(filtered.isEmpty()){
    Box(Modifier.fillMaxWidth().weight(1f),contentAlignment=Alignment.Center){
     Column(horizontalAlignment=Alignment.CenterHorizontally){
      PersianMedallion(diameter=76.dp,symbol=if(filter=="گروه‌ها")"𐎠" else "⌕")
      Spacer(Modifier.height(16.dp))
      Text(when(filter){"گروه‌ها"->"هنوز گروهی ساخته نشده";"خوانده‌نشده"->"پیام خوانده‌نشده‌ای نیست";else->"نتیجه‌ای پیدا نشد"},color=fg,fontWeight=FontWeight.Bold,fontSize=16.sp)
      Text(when(filter){"گروه‌ها"->"گروه‌های گفت‌وگو پس از ایجاد اینجا نمایش داده می‌شوند";"خوانده‌نشده"->"همه پیام‌ها بررسی شده‌اند";else->"عبارت دیگری را برای جستجو امتحان کن"},color=Color.Gray,fontSize=11.sp,textAlign=TextAlign.Center,modifier=Modifier.padding(top=7.dp))
     }
    }
   }else{
    LazyColumn(verticalArrangement=Arrangement.spacedBy(9.dp),contentPadding=PaddingValues(bottom=88.dp)){
     items(filtered,key={it.name}){person->PersonRow(person,surface,fg){onOpen(person)}}
    }
   }
  }
 }
}
@Composable private fun PersonRow(person:Person,surface:Color,fg:Color,onClick:()->Unit){
 Row(Modifier.fillMaxWidth().shadow(2.dp,RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(surface).clickable(onClick=onClick).padding(13.dp),verticalAlignment=Alignment.CenterVertically){
  DimensionalAvatar(label=person.avatar,diameter=50.dp,online=false)
  Column(Modifier.weight(1f).padding(start=12.dp)){Text(person.name,color=fg,fontWeight=FontWeight.Bold);Text(person.last,color=Color.Gray,fontSize=12.sp,maxLines=1)}
  Column(horizontalAlignment=Alignment.End){Text(person.time,color=Color.Gray,fontSize=10.sp);if(person.unread>0){Spacer(Modifier.height(5.dp));Box(Modifier.size(19.dp).background(Turquoise,CircleShape),contentAlignment=Alignment.Center){Text(person.unread.toString(),color=Night,fontSize=10.sp)}}}
 }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun Chat(person:Person,bg:Color,surface:Color,fg:Color,conversationId:String?,messages:List<Message>,back:()->Unit,call:()->Unit,send:(String)->Unit){
 var draft by remember{mutableStateOf("")}
 val repository = remember { FirebaseChatRepository() }
 val currentUid = remember { repository.currentUserId() }
 var liveMessages by remember(conversationId) { mutableStateOf<List<Message>?>(null) }
 var chatError by remember(conversationId) { mutableStateOf<String?>(null) }
 DisposableEffect(conversationId) {
  if (conversationId == null) {
   onDispose { }
  } else {
   val registration = repository.observeMessages(conversationId, onUpdate={snapshot->
    liveMessages = snapshot?.documents.orEmpty().map { doc ->
     val date = doc.getTimestamp("createdAt")?.toDate()
     val time = date?.let { SimpleDateFormat("HH:mm",Locale.getDefault()).format(it) } ?: ""
     Message(doc.getString("text").orEmpty(),doc.getString("senderId")==currentUid,time)
    }
   },onError={exception->chatError=exception.localizedMessage ?: "دریافت پیام‌ها انجام نشد."})
   onDispose { registration.remove() }
  }
 }
 val visibleMessages = liveMessages ?: messages
 Scaffold(containerColor=bg,topBar={TopAppBar(title={Column{Text(person.name,color=fg,fontWeight=FontWeight.Bold);Text("آخرین بازدید اخیراً",color=Color.Gray,fontSize=10.sp)}},navigationIcon={IconButton(back){Icon(Icons.Default.ArrowBack,null,tint=fg)}},colors=TopAppBarDefaults.topAppBarColors(containerColor=surface))},bottomBar={Row(Modifier.fillMaxWidth().background(surface).navigationBarsPadding().padding(8.dp),verticalAlignment=Alignment.CenterVertically){IconButton({}){Icon(Icons.Default.AddCircleOutline,null,tint=Turquoise)};Row(Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(bg).padding(horizontal=13.dp,vertical=5.dp),verticalAlignment=Alignment.CenterVertically){BasicTextField(draft,{draft=it},Modifier.weight(1f).padding(vertical=9.dp),textStyle=TextStyle(color=fg),decorationBox={inner->if(draft.isEmpty())Text("پیام بنویس...",color=Color.Gray);inner()});Icon(Icons.Default.EmojiEmotions,null,tint=Color.Gray)};IconButton({if(draft.isNotBlank()){val text=draft.trim();if(conversationId!=null)repository.sendText(conversationId,text){result->result.onFailure{chatError=it.localizedMessage ?: "ارسال پیام انجام نشد."}} else send(text);draft=""}}){Box(Modifier.size(42.dp).background(Gold,CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Default.Send,null,tint=Night)}}}}){p->
 LazyColumn(Modifier.fillMaxSize().padding(p).padding(horizontal=13.dp),contentPadding=PaddingValues(vertical=16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
  item{Text("امروز",Modifier.fillMaxWidth(),color=Color.Gray,textAlign=TextAlign.Center,fontSize=11.sp)}
  if(chatError!=null)item{Text(chatError!!,Modifier.fillMaxWidth(),color=Color(0xFFFFB4AB),textAlign=TextAlign.Center,fontSize=11.sp)}
  items(visibleMessages){m->Row(Modifier.fillMaxWidth(),horizontalArrangement=if(m.mine)Arrangement.End else Arrangement.Start){ParsMessageBubble(message=m.body,time=m.time,outgoing=m.mine,modifier=Modifier.widthIn(max=300.dp))}}
 }
 }
}
@Composable private fun CallPage(name:String,end:()->Unit){Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Night,Navy))),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.size(126.dp).background(Brush.linearGradient(listOf(Gold,Turquoise)),CircleShape),contentAlignment=Alignment.Center){Text(name.take(1),color=Night,fontSize=52.sp,fontWeight=FontWeight.Bold)};Spacer(Modifier.height(22.dp));Text(name,color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("تماس نمایشی",color=Gold,modifier=Modifier.padding(8.dp));Text("این نسخه هنوز به سرویس تماس واقعی متصل نیست.",color=Color.LightGray,fontSize=11.sp);Spacer(Modifier.height(60.dp));IconButton(end,Modifier.size(64.dp).background(Color(0xFFE14D5A),CircleShape)){Icon(Icons.Default.CallEnd,null,tint=Color.White)}}}}
@Composable private fun Page(title:String,sub:String,bg:Color,fg:Color,back:()->Unit,content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().background(bg).padding(18.dp)){Row(verticalAlignment=Alignment.CenterVertically){IconButton(back){Icon(Icons.Default.ArrowBack,null,tint=fg)};Text(title,color=fg,fontSize=24.sp,fontWeight=FontWeight.Bold)};Text(sub,color=Color.Gray,modifier=Modifier.padding(start=8.dp,bottom=16.dp));content()}}
@Composable private fun Profile(name:String,onName:(String)->Unit,bg:Color,surface:Color,fg:Color,back:()->Unit){
 var draft by remember(name){mutableStateOf(name)}
 Page("نمایه من","اطلاعات نمایشی حساب",bg,fg,back){
  Box(Modifier.fillMaxWidth().padding(20.dp),contentAlignment=Alignment.Center){Box(Modifier.size(90.dp).background(Brush.linearGradient(listOf(Gold,Turquoise)),CircleShape),contentAlignment=Alignment.Center){Text(draft.take(1),color=Night,fontSize=38.sp,fontWeight=FontWeight.Bold)}}
  OutlinedTextField(draft,{draft=it},Modifier.fillMaxWidth(),label={Text("نام نمایشی")},singleLine=true)
  Button({if(draft.isNotBlank())onName(draft.trim())},Modifier.fillMaxWidth().padding(top=12.dp),colors=ButtonDefaults.buttonColors(containerColor=Gold)){Text("ذخیره تغییرات",color=Night,fontWeight=FontWeight.Bold)}
  Text("سازنده: بنیامین نوری دفرازی",color=Color.Gray,modifier=Modifier.padding(top=28.dp),fontSize=12.sp)
 }
}
