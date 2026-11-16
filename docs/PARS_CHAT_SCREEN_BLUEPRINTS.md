# PARS CHAT 1.0 — Screen Blueprints

This document translates the approved visual direction into implementable screen layouts and interaction states. It supplements `PARS_CHAT_DESIGN_SYSTEM.md`.

## Global shell

- Edge-to-edge lapis background with a restrained stone texture; keep texture contrast very low behind text.
- A thin brushed-gold keyline marks only major panels and selected controls.
- Use a consistent 8 dp spacing scale and 18–24 dp panel corner radii.
- Main content remains flat enough to scroll comfortably; reserve pronounced 3D depth for the brand medallion, primary action, avatar frames, and selected navigation.
- Persian RTL is the default. Back navigation, icon order, text alignment, and mixed Persian/Latin strings must be verified on-device.
- Main navigation destinations: Chats, Contacts, Settings. Calls are not a destination until a real calling provider exists.

## A. Welcome / account access

### Layout
1. Full-screen midnight-lapis field with a very subtle radial light behind the emblem.
2. Centered dimensional emblem: dark stone medallion, bevelled muted-gold rim, small Achaemenid-inspired geometry.
3. Wordmark PARS CHAT and Persian tagline.
4. Two clear actions: «ورود» and «ایجاد حساب».
5. Small creator credit at the bottom.

Current implementation: the welcome screen routes to Firebase email/password sign-in and registration. The authenticated home, directory, private conversations, and groups use Firestore listeners; user profiles and media use Firebase Storage. The app root now applies Persian RTL layout direction. Source code is present, but a successful compile and device QA are still required.

### States and behavior
- Welcome never authenticates the user or opens the conversation list by itself.
- Returning authenticated users may be routed to Chats after Firebase auth state is resolved.
- Sign-in fields: email, password, show/hide password.
- Registration fields: display name, email, password, confirm password.
- Inline validation, progress indicator, server error, offline retry, and password reset entry are required.
- Do not show sample users or fabricated success messages.

## B. Chats home

### Header
- Left/right placement follows RTL: brand wordmark and a short greeting; search and profile actions remain reachable.
- Notifications icon is shown only when notification handling is wired.
- A slim metallic separator below the header.

### Filters
- «همه»، «خوانده‌نشده»، «گروه‌ها» as compact segmented controls.
- Selected segment: muted-gold inlay and stronger text contrast.
- Unselected segments: quiet navy stone surfaces.

### Conversation row
- 52 dp avatar with a dark inset and thin gold rim.
- Display name, one-line last-message preview, and local time.
- Unread count uses a small turquoise indicator; avoid oversized badges.
- A small double-check/read indicator may appear only when the backend state is available.
- Long press opens a bottom sheet with mute, archive, pin, and block/report actions where implemented.

### States
- Loading skeleton, first-use empty state, no search results, offline state, and fetch failure.
- New-chat action opens real user search; it must not open a hard-coded demo contact.

## C. Private conversation

### Header
- Avatar, display name, presence or privacy-aware last-seen label.
- Overflow menu for profile, search in conversation, mute, and block/report.
- Audio/video call buttons are omitted until a real provider is integrated.

### Message timeline
- Date separators are centered and visually quiet.
- Incoming bubbles use a raised blue-stone surface.
- Outgoing bubbles use a slightly brighter lapis surface with a hairline gold edge.
- Timestamp and delivery/read status are small but legible.
- Edited messages show a restrained «ویرایش‌شده» label.
- Reply preview appears as a narrow inset strip inside the bubble.
- Reactions appear as compact chips below a message.

### Composer
- Rounded dark-stone input surface.
- Attachment action, text input, emoji action, and send button.
- Voice recording control appears only after microphone permission and recording behavior are implemented.
- While recording: clear elapsed time, cancel, and send controls; never imply a recording is active when it is not.
- Attachment sheet lists only supported, implemented media types.

### Message actions
- Own message: reply, forward, copy, edit, delete, react.
- Other user's message: reply, forward, copy, react, report.
- Confirm destructive actions and distinguish delete-for-me from delete-for-everyone if both semantics are supported.

## D. User search and contacts

- Search field with a clear action and immediate loading feedback.
- Result rows show avatar, display name, and username where available.
- Empty query may show recent contacts only if backed by real account data.
- Search results must exclude blocked accounts according to the product's privacy rules.
- Selecting a result opens an existing conversation or creates one through the repository.
- Handle no results, network failure, and self-profile exclusion.

## E. Profile

### Identity panel
- Large avatar inside a dimensional lapis frame with a brushed-gold rim.
- Display name, unique username if implemented, and concise bio.
- A single subtle geometric motif behind the avatar; do not frame the entire page with ornament.

### Edit mode
- Editable display name and bio.
- Photo selection and upload only when Firebase Storage is configured.
- Explicit Save and Cancel actions; show progress and errors.
- Do not imply that a local-only edit has synchronized to the account.

## F. Settings

Use grouped, compact sections with icon, title, supporting line, and trailing state:
- Appearance: dark lapis / light limestone theme.
- Notifications: message notifications and sound preferences, only when wired.
- Privacy: online visibility, last seen, profile photo visibility, blocked users.
- Security: app lock, active sessions, two-step verification, only when implemented.
- Storage: cache usage and media controls, when implemented.
- Help: report a problem and help information.
- About: app version, creator credit, licenses.

Switches must reflect persisted settings, not just transient UI state. A setting with no backend or device behavior must not be displayed as functional.

## G. Groups

### Group list
- Group avatar, title, last message, time, unread count.
- New-group flow: choose members, set group name, optional image, review, create.

### Group details
- Group image/name/description.
- Member list and role labels.
- Admin controls: edit group info, add/remove members, manage permissions.
- Enforce admin permissions in Firestore rules or trusted backend, not only by hiding UI controls.

## H. Admin console

- Separate route available only to authorized admin accounts.
- Overview metrics must come from real server-backed data.
- User lookup, report queue, moderation action, and audit history.
- Require confirmation for moderation actions and record actor, target, action, timestamp, and reason.
- Never trust a client-side boolean or hard-coded email as sufficient admin authorization.

## I. Shared component inventory

- `LapisScaffold`: page background, safe insets, and content surface.
- `GoldKeylineCard`: restrained raised surface with metallic edge.
- `PersianTopBar`: consistent title, back action, and optional trailing actions.
- `DimensionalAvatar`: image/initials with controlled rim and inset.
- `ConversationRow`: reusable list row with unread and status states.
- `MessageBubble`: incoming/outgoing variants and metadata.
- `GoldPrimaryButton` and `StoneSecondaryButton`.
- `EmptyState`, `LoadingState`, and `ErrorState`.
- `ConfirmActionSheet`: destructive-action confirmation.

## J. Acceptance checklist before APK approval

- [ ] All 15 approved 1.0 capabilities have a defined screen entry point and interaction.
- [ ] No hard-coded demo contacts or sample messages remain in production navigation.
- [ ] Authenticated and unauthenticated app flows are distinct.
- [ ] Every visible control has a real action or is clearly disabled with an explanation.
- [ ] All loading, empty, error, offline, and permission-denied states are designed.
- [ ] RTL, Persian shaping, mixed-script strings, font scaling, and small-screen layouts are checked.
- [ ] Visual hierarchy matches the approved lapis / brushed-gold / restrained-turquoise 3D direction.
- [ ] Calling UI remains absent until real calling infrastructure exists.
- [ ] Admin actions are protected by server-side authorization.
- [ ] No APK build is run until the complete interface is implemented and explicitly approved by the owner.


## Current implementation reality (2026-11-16)

The following notes supersede earlier planning statements where they conflict:

- The home screen is backed by the authenticated user's Firestore conversations; it is not intended to ship with sample contacts or local-only messages.
- The user directory, private message timeline, group creation and group management are connected to the Firebase repository in source.
- The profile currently supports display-name editing and profile-photo upload; username and bio fields are not implemented.
- Settings currently include theme, online visibility, last-seen visibility, blocked users, admin entry, and sign-out. Notification preferences, app lock, two-step verification, and storage management are not implemented and must not be presented as working controls.
- The admin screen currently shows user/conversation counts and account suspend/enable actions. A report queue and in-app audit-history viewer are not implemented.
- The current call screen is a demonstration-only placeholder and is not a real voice/video calling service. Do not present it as working calling. Calls remain outside the approved 15-feature 1.0 scope.
- Presence is lifecycle-based and may become stale after force-stop or abrupt process death.
- The source has not yet passed a recorded Gradle build or on-device acceptance test.

## Release blockers

1. Deploy and verify Firestore rules, Storage rules, and Cloud Functions in the existing Firebase project.
2. Provision the first administrator custom claim through the trusted Admin SDK script.
3. Build the APK through the manual GitHub Actions workflow after design approval.
4. Test registration, two-account private chat, group membership, block enforcement, image/audio uploads, push delivery, and admin suspension.
5. Verify RTL, text scaling, keyboard insets, notification permission, lifecycle presence, and navigation on a physical Android device.
