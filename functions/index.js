const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

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
    const recipients = members.filter((uid) => uid !== message.senderId);
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
