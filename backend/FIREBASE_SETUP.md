# PARS CHAT — Firebase backend foundation

This repository currently contains a local/demo Android client. The Firebase Android configuration is supplied through the GitHub Actions secret. The client integration is being implemented in `FirebaseChatRepository.kt`; live operation still requires enabling Email/Password Authentication, creating Firestore, and publishing the rules.

## Firebase project setup (required before live messaging)
1. Create a Firebase project in the Firebase console.
2. Add an Android app with package name: \`com.bentech.parschaatt\`.
3. Download \`google-services.json\` and place it at \`app/google-services.json\` locally. Do not commit this file to a public repository.
4. Enable **Authentication → Email/Password** (phone auth can be added after configuring SMS and billing requirements).
5. Create a Cloud Firestore database.
6. Publish the repository's \`firestore.rules\` rules in Firestore Rules.
7. Configure App Check before public production release.

## Data model
- \`users/{uid}\`: \`uid\`, \`displayName\`, optional \`photoUrl\`, \`createdAt\`, \`lastSeen\`.
- \`usernames/{normalizedUsername}\`: \`uid\` (unique username reservation).
- \`conversations/{conversationId}\`: \`participants[]\`, \`createdAt\`, \`updatedAt\`, optional \`lastMessage\`, \`lastMessageAt\`.
- \`conversations/{conversationId}/messages/{messageId}\`: \`senderId\`, \`text\`, \`createdAt\`, \`type: "text"\`.

## Security notes
- Firestore rules restrict user profiles to their owner.
- Conversation reads and message creation require membership in the parent conversation.
- Message sender IDs must match the authenticated UID.
- Messages are immutable from the client in this first schema.
- Never put service-account private keys, API secrets, signing keys, or real user credentials in source control.
- These rules are a starting baseline. Test them with the Firebase Emulator Suite and review them before production.

## Current limitations
No Firebase project configuration or credentials are present in this repository, so the app cannot authenticate users or exchange messages over the internet yet. The app remains in demo/local mode until the Firebase app configuration is supplied and the Android client integration is completed.
