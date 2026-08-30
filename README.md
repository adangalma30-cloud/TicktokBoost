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
| `0.0.5` | UI correction: bundled Roboto font, horizontal premium badge, responsive creator cards, spacing polish | current |

Workflow: every update creates a new branch (`0.0.1`, `0.0.2`, …) carrying only the
files needed for that version. `main` is never modified.

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
