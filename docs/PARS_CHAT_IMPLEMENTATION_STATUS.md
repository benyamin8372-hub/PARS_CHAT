# PARS CHAT 1.0 — Implementation Status

Last updated: 2026-11-16

## Implemented in repository source

1. **Registration and login:** Firebase email/password authentication, validation, loading/error states, profile document creation, session restoration and sign-out.
2. **User profiles:** display-name editing, profile-photo upload to Firebase Storage, photo rendering in profile, directory and conversation list.
3. **User search:** live Firestore user directory, display-name search, current-user exclusion and local blocked-user filtering.
4. **Private chat:** deterministic two-user conversation IDs, Firestore realtime message timeline and text sending.
5. **Online/offline:** app foreground/background presence updates; the current implementation is lifecycle-based and can become stale after an abrupt process kill.
6. **Last seen:** timestamp updates when the app leaves foreground; users can hide online and last-seen details independently.
7. **Read receipts:** per-message readBy map, read marking on open, and sender-side read status.
8. **Edit/delete:** sender-authorized message edit and soft-delete.
9. **Reply/forward:** reply metadata and forward source-message metadata; forwarding currently supports text messages.
10. **Images:** system image picker, Firebase Storage upload, and in-chat image rendering.
11. **Voice messages:** in-app microphone recording, audio upload, audio-file attachment and playback.
12. **Groups:** create groups, group admin role, rename groups, add/remove members, and group chat.
13. **Notifications:** FCM token registration, Android notification service/channel, and Cloud Function message trigger.
14. **Block users:** block/unblock screen, directory filtering, private-message and media-upload checks, and push-notification suppression for blocked private chats.
15. **Admin panel:** custom-claim gate, user/conversation counts, admin-only user listing, suspend/enable callable, audit records, and Firestore/Storage denial for suspended accounts.

## Firebase source/configuration included

- `firestore.rules`: membership, private-chat block checks, group roles, message ownership, read-receipt integrity, device tokens, admin claims and suspended-account checks.
- `storage.rules`: participant-only image/audio upload, profile-photo access, size and MIME checks, private-chat block checks, and suspended-account checks.
- `functions/index.js`: FCM notifications, admin-only account status listing and suspension.
- `functions/set-admin.js`: trusted local script to grant the initial admin custom claim.
- `firebase.json` and `.firebaserc`: deployment configuration for project `parschat-3f91f`.
- `docs/FIREBASE_DEPLOYMENT.md`: deployment and first-admin setup.

## Still required before this can be called production-ready

- [ ] Deploy the Firestore rules, Storage rules and Cloud Functions to the existing Firebase project.
- [ ] Confirm Firebase Storage and Cloud Functions are enabled for the project.
- [ ] Provision the intended administrator UID using the trusted Admin SDK script.
- [ ] Test registration, two-account private chat, groups, block enforcement, media upload, FCM delivery and admin suspension against the real Firebase project.
- [ ] Run Firebase Emulator/security-rule tests and fix any denied/over-permissive cases.
- [ ] Compile the Android app, resolve any build errors, install on a real device and complete UI/device QA.
- [ ] Verify lifecycle presence behavior under force-stop, loss of connectivity and process death; a Realtime Database onDisconnect presence backend may be needed for stronger presence guarantees.
- [ ] Add pagination and operational monitoring before scaling beyond a small initial user base.

## Build policy

The GitHub Actions APK workflow remains manual. No APK build has been started. Source commits have not been compile-verified; the app must be built and tested before an APK can be described as working.
