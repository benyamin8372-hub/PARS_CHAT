# PARS CHAT 1.0 — Implementation Status

Last updated: 2026-11-16

## Completed in source

- [x] Approved 15-feature scope retained as the product target.
- [x] Lapis / brushed-gold / restrained turquoise design tokens and reusable dimensional components.
- [x] Dimensional avatar and refined incoming/outgoing message bubble components.
- [x] Home screen visual rebuild: branded header, search field, all/unread/groups filters, unread counts, and empty states.
- [x] Welcome screen no longer bypasses authentication.
- [x] Firebase email/password sign-in and registration UI with basic client-side validation, loading state, and visible errors.
- [x] Authentication screen uses the shared FirebaseChatRepository.
- [x] Authenticated Firebase session is restored on app launch.
- [x] Display name is loaded from the Firestore user profile and profile edits are written to Firestore.
- [x] Sign-out action added to Settings.
- [x] Removed hard-coded online indicator and fabricated read receipts.
- [x] Removed visible call actions because no calling provider is integrated; secondary navigation is labelled Contacts.

## Not yet complete — required before release

- [x] Remove hard-coded sample users and messages from production navigation.
- [x] Add Firestore-backed user directory with live updates and self-profile exclusion.
- [x] Add Firestore conversation observation and open/create private conversations.
- [x] Connect private text-message timeline and sending to realtime Firestore listeners.
- [x] Add user search that excludes the current user. Blocked-user filtering remains pending.
- [ ] Persist and display accurate online/last-seen state with privacy controls.
- [ ] Implement message edit/delete, reply, forward, and actual read receipts.
- [ ] Implement image and voice-message upload/playback with Firebase Storage and runtime permissions.
- [ ] Implement group creation, membership, roles, and server-enforced permissions.
- [ ] Add push notifications with Firebase Cloud Messaging.
- [ ] Implement block/unblock flows and Firestore security rules.
- [ ] Implement a server-authorized admin console with audit history.
- [ ] Add loading, offline, retry, and permission-denied states across all data screens.
- [ ] Review Firestore rules and test security using Firebase Emulator or a dedicated test project.
- [ ] Run Android build, install, and device QA only after the owner approves the complete UI.

## Build policy

The GitHub Actions build remains manual. No APK build has been started as part of these design and source updates. Source commits have not been compile-verified; run the build only after the complete interface is ready and explicitly approved.
