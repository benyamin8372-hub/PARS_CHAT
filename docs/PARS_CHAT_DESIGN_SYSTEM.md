# PARS CHAT — Visual Design System

Status: approved visual direction; design-first phase. Do not trigger an APK build until the owner approves the complete UI.

## Product identity
PARS CHAT is a modern Persian-first messenger with a visual identity inspired by pre-Islamic Iran, especially Achaemenid geometry and restrained Sasanian metalwork. It must feel contemporary and functional—not like a museum interface, costume theme, or ornamental poster.

Core principle: ancient Iranian identity, modern product ergonomics, controlled 3D depth.

## Visual language
- Primary canvas: deep lapis / midnight navy.
- Surfaces: layered blue-black stone with subtle material variation.
- Accent: muted brushed gold, used as fine inlays, edge highlights, separators, and selected states.
- Secondary accent: restrained stone turquoise, reserved for online state, unread indicators, and a few actionable highlights.
- Light theme: warm limestone / ivory surfaces with dark ink typography and the same restrained gold detailing.
- Avoid full-screen gold, loud gradients, excessive glow, ornamental borders around every component, and repeated historical motifs.

## Material and depth
- Use a small number of meaningful elevation layers: background, raised surface, floating control.
- 3D treatment belongs primarily to the brand emblem, selected navigation element, avatar frames, and key action controls.
- Gold veins should be thin, irregular but controlled metallic inlays—not bright yellow strokes.
- Use soft ambient highlights and short shadows; preserve text contrast and battery performance.
- Avoid continuous heavy particle effects or animations.

## Typography and RTL
- Persian is the primary language; all screens use RTL layout and correct Persian shaping.
- Use Vazirmatn as the primary typeface, with a robust system fallback.
- Establish consistent roles: display title, page title, section title, body, supporting text, timestamp, and button label.
- Use Persian digits consistently in user-facing counters and timestamps where appropriate.
- Never use decorative calligraphy for functional labels.

## Shape and iconography
- Prefer calm geometric forms: medium-radius panels, circular avatars, restrained pill filters.
- Icons use one consistent stroke/weight family; no mixing emoji with functional icons.
- Achaemenid-inspired geometry may appear as a subtle emblem or small divider motif, not as repeated wallpaper.
- Keep touch targets comfortably sized and accessible.

## Official brand mark
- The primary PARS CHAT symbol combines a stylized Faravahar with a speech bubble.
- Faravahar is the dominant historic element, rendered in pale metallic gold; the speech bubble is a secondary turquoise detail.
- The emblem sits on a midnight-lapis circular medallion with a restrained gold rim.
- Use the same mark on the welcome screen, home header, and Android launcher icon. Do not replace it with a generic chat glyph or an unrelated ancient character.

## Screen specifications

### 1. Welcome and authentication
- Brand emblem centered in a dimensional lapis medallion with a thin brushed-gold rim.
- Clear PARS CHAT wordmark and concise Persian tagline.
- Sign-in and create-account actions are visually distinct.
- Email, password, display name, password visibility, validation, loading, and error states are required.
- No bypass from welcome directly into the chat list in production.

### 2. Conversation home
- Header: wordmark, current-user greeting, search, notifications, and profile entry.
- Segmented filters: all, unread, groups.
- Conversation rows: avatar, display name, last message, time, unread badge, and delivery/read context when available.
- Floating new-chat action uses the dimensional gold treatment.
- Bottom navigation: chats, contacts, settings; active state uses a restrained gold inlay.
- Empty, loading, offline, and search-no-results states are designed explicitly.

### 3. Private conversation
- Header with avatar, name, presence/last-seen privacy-aware status, and call actions.
- Message bubbles use layered blue materials; outgoing messages receive a subtle gold edge or small accent, never a full gold fill.
- Include timestamps, sending/delivered/read states, reply preview, edited marker, and date separators.
- Composer supports text, emoji, attachment, voice-note affordance, and send.
- Long-press message actions: reply, forward, copy, edit own message, delete, react.
- Ensure keyboard, attachment sheet, recording, and empty conversation states.

### 4. Profile
- Profile identity panel with framed avatar, display name, username, and short bio.
- Editable profile fields have clear edit/save/cancel states.
- Account details and privacy shortcuts are grouped into quiet sections.
- Use a single subtle Achaemenid geometric detail behind the avatar; no ornate full-page frame.

### 5. Settings and privacy
- Group settings into Appearance, Notifications, Privacy and Security, Storage, Help, and About.
- Each row has a clear icon, title, short supporting label, and disclosure/action state.
- Theme selector previews lapis-dark and limestone-light.
- Privacy controls explain consequences before changing visibility.
- Security section includes app lock, active sessions, and two-step verification only when implemented.
- Do not display controls that have no working behavior.

### 6. Contacts and user search
- Search field, result rows, avatar, display name, username, and mutual context only when available.
- Clear states for loading, no results, blocked users, and network failure.
- New conversation action must use real user records, not bundled sample contacts.

### 7. Groups
- Group list rows show image, title, latest message, time, and unread count.
- Group details include description, members, roles, and permission controls.
- Admin-only actions are visibly distinguished and enforced by backend authorization.

### 8. Calls
- Until a real calling service is integrated, call controls must be labelled unavailable or omitted.
- Never present a simulated call screen as a working call.

### 9. Admin
- Separate protected admin area, not a normal user-facing tab.
- Show only server-backed metrics and authorized moderation actions.
- Every moderation action requires confirmation and audit logging.

## Motion
- Short, purposeful transitions (roughly 160–240 ms).
- Subtle elevation/shine response on primary controls.
- Respect Android reduced-motion/system animation settings.
- No looping decorative animation on ordinary content screens.

## Accessibility and quality gates
- Maintain readable contrast for all text on lapis and limestone surfaces.
- Support font scaling, screen readers, keyboard navigation where applicable, and minimum touch targets.
- Verify RTL alignment, mixed Persian/Latin text, long names, small screens, and empty/error/loading states.
- Keep motion and surface effects lightweight.

## Implementation gate
The visual specification is not proof that a feature is implemented. The existing repository contains sample conversation data and simulated call UI; those must be replaced or clearly gated before release. No APK build is authorized until all screen designs are implemented, reviewed, and explicitly approved.
