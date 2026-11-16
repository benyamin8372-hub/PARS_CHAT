const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const { getAuth } = require("firebase-admin/auth");
const { onCall, HttpsError } = require("firebase-functions/v2/https");

initializeApp();
const db = getFirestore();

exports.notifyConversationMembers = onDocumentCreated(
  { document: "conversations/{conversationId}/messages/{messageId}", region: "europe-west1" },
  async (event) => {
    const message = event.data && event.data.data();
    if (!message || !message.senderId) return;
    const conversationId = event.params.conversationId;
    const conversationSnap = await db.collection("conversations").doc(conversationId).get();
    if (!conversationSnap.exists) return;
    const conversation = conversationSnap.data();
    const members = Array.isArray(conversation.participants) ? conversation.participants : [];
    let recipients = members.filter((uid) => uid !== message.senderId);
    if (!recipients.length) return;
    if (conversation.type !== "group") {
      const allowed = [];
      for (const uid of recipients) {
        const [senderBlocked, recipientBlocked] = await Promise.all([
          db.collection("users").doc(message.senderId).collection("blocked").doc(uid).get(),
          db.collection("users").doc(uid).collection("blocked").doc(message.senderId).get()
        ]);
        if (!senderBlocked.exists && !recipientBlocked.exists) allowed.push(uid);
      }
      recipients = allowed;
    }
    if (!recipients.length) return;

    const activeRecipients = [];
    for (const uid of recipients) {
      const accountStatus = await db.collection("accountStatus").doc(uid).get();
      if (!accountStatus.exists || accountStatus.data().suspended !== true) activeRecipients.push(uid);
    }
    recipients = activeRecipients;
    if (!recipients.length) return;

    const senderSnap = await db.collection("users").doc(message.senderId).get();
    const senderName = senderSnap.exists ? (senderSnap.data().displayName || "کاربر پارس چت") : "کاربر پارس چت";
    const title = conversation.type === "group" ? (conversation.name || "گروه پارس چت") : senderName;
    const body = message.type === "image" ? "یک تصویر فرستاد"
      : message.type === "audio" ? "یک پیام صوتی فرستاد"
      : (message.text || "پیام تازه").slice(0, 120);

    const tokenDocs = await Promise.all(recipients.map((uid) =>
      db.collection("users").doc(uid).collection("devices").get()
    ));
    const tokens = tokenDocs.flatMap((snapshot) => snapshot.docs.map((doc) => doc.get("token")).filter(Boolean));
    if (!tokens.length) return;

    const response = await getMessaging().sendEachForMulticast({
      tokens,
      notification: { title, body },
      data: { conversationId, messageId: event.params.messageId, type: String(message.type || "text") },
      android: { priority: "high", notification: { channelId: "pars_chat_messages" } }
    });

    const invalid = [];
    response.responses.forEach((result, index) => {
      if (!result.success && ["messaging/registration-token-not-registered", "messaging/invalid-registration-token"].includes(result.error && result.error.code)) {
        invalid.push(tokens[index]);
      }
    });
    if (invalid.length) {
      const batch = db.batch();
      for (const token of invalid) {
        for (const uid of recipients) batch.delete(db.collection("users").doc(uid).collection("devices").doc(token));
      }
      await batch.commit();
    }
  }
);


exports.setUserDisabled = onCall({ region: "europe-west1" }, async (request) => {
  if (!request.auth || request.auth.token.admin !== true) {
    throw new HttpsError("permission-denied", "مدیر مجاز نیست.");
  }
  const adminStatus = await db.collection("accountStatus").doc(request.auth.uid).get();
  if (adminStatus.exists && adminStatus.data().suspended === true) {
    throw new HttpsError("permission-denied", "دسترسی این حساب غیرفعال شده است.");
  }
  const uid = String(request.data && request.data.uid || "");
  const disabled = request.data && request.data.disabled;
  if (!uid || typeof disabled !== "boolean" || uid === request.auth.uid) {
    throw new HttpsError("invalid-argument", "شناسه یا وضعیت حساب معتبر نیست.");
  }
  const statusRef = db.collection("accountStatus").doc(uid);
  if (disabled) {
    await statusRef.set({ suspended: true, updatedAt: new Date() }, { merge: true });
    await getAuth().updateUser(uid, { disabled: true });
    await getAuth().revokeRefreshTokens(uid);
  } else {
    await getAuth().updateUser(uid, { disabled: false });
    await statusRef.set({ suspended: false, updatedAt: new Date() }, { merge: true });
  }
  await db.collection("adminAudit").add({
    actorUid: request.auth.uid,
    targetUid: uid,
    action: disabled ? "disable-user" : "enable-user",
    createdAt: new Date()
  });
  return { success: true };
});


exports.listAdminUsers = onCall({ region: "europe-west1" }, async (request) => {
  if (!request.auth || request.auth.token.admin !== true) {
    throw new HttpsError("permission-denied", "مدیر مجاز نیست.");
  }
  const adminStatus = await db.collection("accountStatus").doc(request.auth.uid).get();
  if (adminStatus.exists && adminStatus.data().suspended === true) {
    throw new HttpsError("permission-denied", "دسترسی این حساب غیرفعال شده است.");
  }
  const result = await getAuth().listUsers(1000);
  const users = await Promise.all(result.users.map(async (user) => {
    const status = await db.collection("accountStatus").doc(user.uid).get();
    return {
      uid: user.uid,
      disabled: user.disabled === true || (status.exists && status.data().suspended === true)
    };
  }));
  return { users };
});
