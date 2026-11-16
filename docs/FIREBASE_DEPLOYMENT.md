# PARS CHAT — Firebase deployment checklist

This repository now contains Android client code, Firestore/Storage rules, and Cloud Functions source. These services are not active in the Firebase project until the deployment steps below succeed.

## Existing Android Firebase registration

The Android application ID remains `com.bentech.parschaatt`. The existing Firebase Android app registration and the GitHub Actions secret `GOOGLE_SERVICES_JSON` are reused. Do not register a second Android app just for these changes.

## Enable required Firebase products

In Firebase Console for project `parschat-3f91f`:

1. Authentication → Sign-in method → enable Email/Password.
2. Firestore Database → create/confirm the production database.
3. Storage → enable the default bucket.
4. Cloud Messaging → confirm the Android app is listed and FCM is available.
5. Cloud Functions → confirm the project has the billing/plan requirements needed for deployment.

## Deploy rules and functions

Install Node.js 20 and Firebase CLI on a trusted computer, then run from the repository root:

```bash
firebase login
firebase use parschat-3f91f
cd functions
npm install
cd ..
firebase deploy --only firestore:rules,storage,functions
```

The deployment publishes:
- `firestore.rules`: private conversations, groups, message mutation, receipts, blocks, device tokens, and admin-claim access.
- `storage.rules`: participant-only image/audio uploads with a 15 MB limit and block checks for private chats.
- `functions/index.js`: FCM message notifications and admin-only account suspension/listing callables.

## Provision the first administrator

Admin access is based on a Firebase Authentication custom claim. The Android app cannot grant this claim to itself.

1. In Firebase Console → Authentication, copy the intended administrator's UID.
2. On a trusted computer, create/download a service-account credential with Firebase Authentication Admin privileges. Keep it outside the repository and never send it in chat.
3. In PowerShell:

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS = "C:\secure\firebase-admin-service-account.json"
cd functions
npm install
npm run set-admin -- FIREBASE_AUTH_UID
```

Replace the path and UID with the actual local values. Sign out and sign back into PARS CHAT so the client refreshes its ID token. The Admin Panel then checks the custom claim.

## Android permissions and notifications

- Android 13+ asks the user for notification permission.
- Image upload uses the system photo picker.
- Audio file selection uses the system content picker.
- Voice recording requests microphone permission at the time of recording.
- FCM tokens are stored under each authenticated user's `devices` subcollection.

## Important operational notes

- The Cloud Functions are deployed to `europe-west1`; the Android callable client uses the same region.
- Push notifications do not appear until Functions are deployed and FCM is enabled for the project.
- Storage uploads fail until the Storage bucket is enabled and `storage.rules` are deployed.
- The rules are security-sensitive. Test with two normal accounts and one custom-claim admin before production use.
- Never commit service-account JSON, FCM server credentials, or private keys.
- The GitHub Actions APK workflow remains manual. No APK build is started by these source commits.
