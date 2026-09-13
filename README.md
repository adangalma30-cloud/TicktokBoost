# TikTokBoost

**Follow-for-follow social discovery for TikTok.** Find real creators who want more
followers, follow them yourself in the TikTok app, and they follow you back — rewarded
with points and featured placement.

> **What this app is NOT:** a bot, an auto-follower, or a fake-engagement generator.
> TikTokBoost never logs into TikTok accounts, never creates accounts, and never
> performs follows/likes/views automatically. Every follow happens manually by the
> user, inside the TikTok app.

## How it works

1. A user creates a TikTokBoost profile and adds their TikTok username/link.
2. They enter the **Follow Exchange** and see other real users who want followers.
3. User A chooses to follow User B's TikTok account (opens TikTok → taps follow).
4. User B can then follow User A back.
5. A points/credit system organizes and rewards participation.

## Version branches

| Branch | Contents | Status |
| ------ | -------- | ------ |
| `main` | original baseline — kept untouched | baseline |
| `0.0.1` | Android app MVP (demo/mock data) + built APK | previous |
| `0.0.2` | Polish release: icon, splash, trust system, transactions, notifications, dark/light | previous |
| `0.0.3` | Economy hardening, anti-abuse, disputes, Premium (mock), admin dashboard, TikTok-inspired identity | previous |
| `0.0.4` | Stability: duplicate-ID crash fixed (Notifications/History), quest engine, ligature-safe typography, long-name cards | previous |
| `0.0.5` | UI correction: bundled Roboto font, horizontal premium badge, responsive creator cards, spacing polish | previous |
| `0.0.6` | Earn screen fix: quest cards rebuilt (titles/descriptions/rewards visible), rules card, scroll polish | previous |
| `0.0.7` | Launcher icon: "TB" monogram, TikTok-style cyan/pink duotone on near-black | previous |
| `0.0.8` | Brand redesign: "Motion Lock" TB emblem (4 concepts evaluated), rebuilt splash animation, single brand asset | previous |
| `0.0.9` | Performance: R8 minified + resource-shrunk APK (7.0 → 2.2 MB), success haptics | previous |
| `0.0.10` | Icon redesign: "Creator Rise" (6 concepts evaluated) — creator + ascending growth in the signature duotone | previous |
| `0.0.11` | FINAL MVP: expiration, blocking/reporting, match score + For You, setup wizard, profile pictures, referral dashboard, auth hardening | previous |
| `0.0.12` | Settings system (working theme switch + persistence), public creator profiles with photo/video content, content management | previous |
| `0.0.13` | Referral system rebuilt: persistent per-friend records, repeatable rewards (idempotent, daily-limited), permanent Invite Friends section | current |

Workflow: every update creates a new branch (`0.0.1`, `0.0.2`, …) carrying only the
files needed for that version. `main` is never modified.

## What's new in v0.0.13 (referral system + audit)

Deep functionality audit result: all MVP features are implemented and persistent
on-device via SharedPreferences/JSON (no backend by design); the referral system
was the one genuinely fake area (a single boolean + one-time quest) — now rebuilt:

- Persistent per-friend Referral records (referrer, referred user, code, status,
  createdAt/qualifiedAt/rewardedAt, reward amount, idempotent transaction id) with
  statuses INVITED / REGISTERED / QUALIFYING / QUALIFIED / REWARDED / REJECTED
- Repeatable forever: invite John → reward → invite Michael → reward again
  (verified by test)
- Idempotent claims: a referral can never pay twice (verified by test)
- Anti-abuse: max 3 rewarded referrals/day (verified by test)
- Referral UI: permanent "Invite Friends" section on Earn with live stats,
  code + copy + native share, per-friend records with claim buttons
- Signup accepts an optional referral code and rejects your own code
- Survives restart/logout (verified by test)
- Full feature-by-feature audit in [`AUDIT.md`](AUDIT.md)

## What's new in v0.0.12 (settings + creator profiles)

- Settings, rebuilt and real: Light / Dark / System theme that applies instantly
  app-wide and persists across restarts; Notifications toggle (actually suppresses
  new notifications); vibration/haptics preference; language selector (structure
  ready for translations); privacy (discoverable toggle, blocked users); account
  (subscription, logout, delete-account with confirmation); Help & Support (FAQ +
  mailto contact); About with version.
- Public creator profiles: opened by tapping any card in For You / Discover —
  header, bio, trust + stats, match chip, achievements, shared activity, and the
  full exchange flow; Report/Block as secondary actions.
- Creator content: photos and short videos in a responsive grid on profiles
  (captions, play overlays); video playback for your own uploads with loading and
  error states; demo creators get sample content tiles (no external scraping).
- My content management (Profile): add photo / add video via the system picker,
  edit captions, delete with confirmation, saving state.
- Tests: theme persistence, notification gate, content CRUD, creator-profile
  navigation (V12Test 4/4; full suite 20/20).

## What's new in v0.0.11 (final MVP completion)

- Exchange lifecycle completed: unconfirmed exchanges EXPIRE after 24h (config) with no
  punishment and the pairing frees up; one gentle confirmation reminder per exchange.
- Safety: long-press a creator card to Block or Report (6 reasons); blocked creators leave
  discovery and can't be exchanged with; Settings → Blocked users; reports land in the
  admin moderation queue.
- Discovery: "For You" feed ranked by the new TickTokBoost match score (shared category,
  activity, trust, completeness, availability — a recommendation, not a prediction) with
  🔥 % Match chips on cards; category filters are now free for everyone.
- Onboarding: post-signup setup wizard (avatar → TikTok handle → category → bio) with
  progress, skippable optional steps; real profile pictures via the system image picker.
- Home dashboard: pending-confirmations card, "Recommended for you" strip, three quick
  actions (Discover / Earn / Boost).
- Referrals: dashboard with Invited / Joined / Qualified / Rewarded tiles.
- Auth: password visibility toggles, email + password validation with friendly errors.
- Profile: picture picker, account risk level (Low/Medium/High) beside trust stats.
- Admin: reports queue, risk + referral metrics. Removed the coin-packs "Soon" placeholder.
- Tests: expiration, blocking, matching, reporting regressions (MvpTest 4/4).

## What's new in v0.0.10 (icon redesign)

- Six original icon concepts designed & measured at 48px (playrise, creator rise,
  double-play TB, ascending network, boost spark, ascent play). Winner:
  **"Creator Rise"** — a creator beside three ascending growth bars: the product
  story in one silhouette, and the boldest small-size presence of the set.
- One brand asset: identical geometry ships as the adaptive launcher icon
  (foreground + near-black background + monochrome), legacy PNGs (mdpi–xxxhdpi),
  and `TbEmblem.kt` — so splash, Home, Welcome, auth and Settings show the exact
  same symbol. Generator: `tools_iconconcepts.py`; concepts in `art/iconconcepts/`.
- Centering pixel-verified (512px & 48px); launcher-mask safe; monochrome-safe.
- No functional changes. versionCode 10.

## What's new in v0.0.9 (performance & polish)

- Release builds now R8-minified with resource shrinking: APK shrinks from
  7.0 MB to 2.2 MB (-69%), single dex, faster cold start. Proguard rules keep
  the enum-by-name persistence surface; bundled fonts verified present
  (AGP resource-path shortening renames them in the APK).
- Subtle success haptics at the four meaningful moments: exchange complete,
  quest claim / daily check-in, boost activation, premium activation.
- No functional changes. versionCode 9.

## What's new in v0.0.8 (brand redesign)

- Four distinct TB emblem concepts designed & evaluated (A Motion Lock, B Play Bowl,
  C Boost Ascent, D Overprint); **Concept A "Motion Lock"** selected — the T's rising
  crossbar shares its stem with the B's spine, one fused emblem in the signature
  duotone (cyan ghost ↖, pink ghost ↘, white face). Previews in `art/concepts/`.
- ONE brand asset: `TbEmblem.kt` (Composable) mirrors `tools_tbemblem.py` geometry and
  powers the launcher vectors, splash, and every in-app LogoMark.
- Splash rebuilt (~1.95s, tap-to-skip): dark stage → dual cyan/pink glow → converging
  motion trails → emblem assembles from its own duotone layers → subtle forward boost →
  settle → wordmark + tagline. Timeline is delay-stepped (virtual-time friendly).
- Old icon fully purged (generators, assets and drawing code removed; themes aligned).
- Full suite 12/12 including launch-with-new-splash; CI green; versionCode 8.

## What's new in v0.0.7 (official launcher icon)

- New adaptive launcher icon: bold "TB" monogram (outlines extracted from the
  app's bundled Roboto Black) rendered in the TikTok-style duotone — cyan ghost
  offset up-left, pink ghost down-right, solid white face — on a near-black
  gradient with faint corner glows. Original TickTokBoost design; no TikTok assets.
- Adaptive layers: near-black gradient background + monogram foreground inside
  the 66dp safe zone; white monochrome variant for Android 13+ themed icons.
- Legacy launcher PNGs regenerated for all densities (square + round), monogram
  at 58% canvas, centered. Generated by `tools_tbicon.py`.
- App name, application ID, splash and in-app branding unchanged.

## What's new in v0.0.6 (Earn screen fix)

- Root cause of the broken quest cards: action buttons used inside the quest
  header Row demanded full row width (fillMaxWidth inside a Row), squeezing the
  weighted title/description column to ~0 width — titles/descriptions invisible,
  cards rendered as huge empty containers. Buttons now live on their own
  full-width row at the bottom of each card; the header is emoji | text | reward.
- Every quest card now shows: emoji, title, description, reward chip (+N),
  status line (streak / progress / profile % / referral state), progress bar
  when in progress, and a state-correct action (Check in / Continue /
  Claim +N / ✓ Completed).
- Emoji icon slot is fixed-width so glyph metrics can never distort the row.
- "Coin Earning Rules" is now its own bulleted card below the quests —
  no overlap, fully readable, reachable by scrolling.
- Regression tests: Earn content presence + scroll-reachability for every quest
  and the rules card (width assertions are unreliable under Robolectric with
  bundled fonts — documented in the test).

## What's new in v0.0.5 (UI/typography correction)

- Bundled Roboto font family (regular/medium/bold/black) applied to all UI text —
  rendering is now identical on every device and immune to manufacturer system
  fonts that distorted "in / ij / nj / 'n"; ligatures remain disabled app-wide.
- Premium badge rebuilt: fixed height, single-line no-wrap label — can never
  collapse into a vertical strip; renders as compact horizontal 💎 Premium / 👑 Pro.
- Creator cards rebuilt with a natural flow: avatar + name (2-line wrap) + username,
  trust & premium badges on their own full-width row, category/activity line,
  bio, stats, then side-by-side action buttons. No reserved columns, no empty gaps.
- Profile header, Home header and Boost analytics row bounded so badges keep their
  width beside long names.
- "Finish it →" replaced with the clean "Complete profile" action.
- Discovery fixtures added for extreme names and ligature-heavy text; new UI
  regression tests cover badge horizontality and the completion label.

## What's new in v0.0.4 (stability release)

- **Fixed the launch/Notifications crash**: notification & transaction IDs were built from
  millisecond timestamps; same-millisecond bursts (e.g. "confirmed + coins released")
  collided, producing duplicate LazyColumn keys and an immediate crash that persisted in
  storage. IDs are now collision-proof and stored lists are deduped on read, which also
  heals already-affected installs.
- **Fixed confirmed exchanges not releasing coins**: the anti-duplicate check matched the
  transaction being confirmed, so some verifications granted nothing.
- Quest engine: quests track real qualifying actions (check-in, exchanges x3, profile
  completion, referral qualification, native share) with AVAILABLE / IN_PROGRESS /
  READY_TO_CLAIM / COMPLETED states; rewards pay exactly once via the economy service.
- Native Android share sheet for referrals & the share quest; referral code + link screen.
- Ligature-safe typography (`liga/clig/dlig` disabled app-wide) for clean "in/ij/nj/'n" rendering.
- Creator cards: long names wrap to two lines with ellipsis, badges never overlap or stretch.
- Robolectric stability harness (9 tests): real MainActivity launch, poisoned-storage
  rendering, all tabs, John/Sarah economy flow, quest reward security.

## What's new in v0.0.3

- Harder coin economy (configurable): 20 starter coins, 5 per confirmed exchange, 25/day cap, 10-min cooldown — all in `EconomyConfig`
- Anti-farming: repeat-pairing block, burst detection, Warning → Review → Restriction ladder (never auto-bans)
- Dispute system with reasons, withdraw, admin review queue that resolves disputes
- Full coin types: available / pending / lifetime earned / lifetime spent — every movement is a ledger transaction
- Boost Visibility tiers (Standard 10 · Boosted 25 · Featured 50) with duration status
- Premium + Premium Pro (mock payments): badges, priority discovery, advanced filters, analytics, daily bonus, plan limits
- Ranking-based discovery: trust + activity + completeness + boosts + premium, freshness rotation, category filters
- Profile completion score, streaks with small rewards, cosmetic achievements
- Admin dashboard: economy analytics, dispute queue, abuse watchlist, testing controls
- New near-black + cyan/pink identity, new icon, 5-tab navigation, 4-step onboarding

## What's new in v0.0.2

- Original "Ascent Trio" app icon (adaptive + monochrome + legacy PNGs, generated by `tools_icongen.py`)
- Premium ~2s startup animation (dark stage, particles, glow, tap to skip)
- Full dark & light themes following the system setting; one design system across all screens
- Redesigned Home: brand header, coins wallet (available + pending), trust badge & progress, Find Creators / Earn Coins actions, recent activity
- Creator Discovery: search, Recommended / New / Trusted / Most Active filters, trust-rich cards
- Exchange lifecycle: completing a follow creates a PENDING transaction → counterpart confirms → VERIFIED, coins release, trust updates; disputes go under review and resolve
- Trust ladder (L1 New → L5 Elite) with score, exchanges, disputes, completion rate
- Coins hub: explanation, animated balance, release celebrations, recent earned/spent
- History with status tabs; Notifications screen with unread badges
- Skeleton loading states, polished empty states, friendly error states with retry
- Micro-animations: press feedback, staggered cards, smooth navigation transitions

## What's in v0.0.1 (mock/demo data)

- Welcome / onboarding
- Sign up / log in (demo mode — any credentials work)
- Home dashboard (points balance, daily check-in, stats, quick actions)
- **Follow Exchange** — user cards with username, avatar, TikTok profile link,
  followers requested, "Open TikTok" and "I've Followed" confirmation flow
- My Profile (edit display name + TikTok handle)
- Coins / points (balance, spend on featured & priority listing)
- Earn Coins (daily check-in, follow tasks, invite, share)
- History (follow activity log with points earned)
- Settings (version, about, reset demo data, sign out)

All users, follows and history in v0.0.1 are stored locally on-device
(SharedPreferences). No external services are connected yet.

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- Navigation Compose, single-activity
- Min SDK 24 · Target/Compile SDK 34
- Built with Gradle 8.7 / AGP 8.5.2

## Building the APK

Prerequisites: JDK 17+ and the Android SDK (platform 34, build-tools 34.0.0).
Set `sdk.dir` in a local `local.properties` (or `ANDROID_HOME`):

```bash
./gradlew assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
Pre-built release artifact: `release/TikTokBoost-v0.0.1.apk` (installable on any
Android 7.0+ device; the standard debug keystore is used, which is fine for testing).

## Roadmap (later versions)

- Backend + real accounts, real TikTok profile data
- Verification that a follow actually happened (user-confirmed, still no automation)
- Anti-abuse: report/block, trust ratings
- Real coin store (in-app purchases)
