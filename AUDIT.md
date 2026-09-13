# TikTokBoost — Deep Functionality Audit (v0.0.13)

Audit of the current repository state — branch `0.0.13` (tip `7852964`), which is
the latest version branch. `main` is an untouched baseline; the app lives on
version branches (`0.0.1` → `0.0.13`).

**Bottom line:** TikTokBoost is a genuine **local-first MVP** with real,
persistent logic underneath the UI — not painted screens. The economy, exchange
lifecycle, quest engine, trust, disputes, boosts, premium effects, safety
(block/report), settings and creator content are all implemented and persisted
on-device. The referral system (previously a one-time quest boolean) was the one
genuinely fake area and has been rebuilt in v0.0.13 into a persistent, repeatable
system with idempotent rewards.

Legend: **A** fully functional · **B** partially functional · **C** mock/local-only ·
**D** UI-only · **E** missing.

---

## Architecture summary

| Aspect | Finding |
| --- | --- |
| Language / UI | Kotlin + Jetpack Compose (Material 3), single-activity, Navigation Compose |
| Persistence | `Session` → `SharedPreferences` (JSON-encoded lists/maps). No DB, no network. |
| Economy | `EconomyService` — single source of truth for coin mutations; every movement writes a ledger `Transaction`. |
| Quests | `Quests` — rewards only from `READY_TO_CLAIM`, paid exactly once via the economy service. |
| State | `AppState` — Compose-observable snapshot, refreshed after every mutation. |
| Data source | `MockData` — 20 hard-coded fictional creators (no backend). |
| Tests | Robolectric: `MvpTest`(4) `QuestTest`(4) `StabilityTest`(8) `V12Test`(4) `V13Test`(5) = **25 tests**. |
| Store pack | `docs/` — DATA_SAFETY, PRIVACY_POLICY, RELEASE_CHECKLIST, STORE_LISTING. |
| Backend | **None.** No network calls exist despite the `INTERNET` permission — intentional. |

---

## Feature-by-feature matrix

| # | Feature | Status | Evidence / notes |
| --- | --- | --- | --- |
| 1 | Authentication | **C** | Demo auth: any valid-format email + ≥6-char password logs in. No server, no hashing — clearly labeled "Demo mode". |
| 2 | User registration / login | **B** | Real form validation, password visibility toggle, starter bonus, referral-code field. Works, but accounts are device-local. |
| 3 | Persistent sessions | **A/B** | `isOnboarded`/`isLoggedIn` persist and drive splash routing. **Finding:** sign-out only navigates (does not clear `isLoggedIn`), so the app re-enters as signed-in on next launch. See follow-ups. |
| 4 | User profiles | **A** | Name, handle, bio, category, profile picture; 4-step setup wizard; edit-in-place; completion score (15/25/20/20/20). |
| 5 | Creator discovery | **A** | Search + filters (For You / Recommended / New / Trusted / Most Active) + category filters; multi-factor ranking. |
| 6 | For You recommendations | **A** | `matchScore()` — category + trust + activity + completeness + recency, pure score ordering. |
| 7 | Creator matching | **A** | Match % chip; same-category bonus; down-ranks past partners/blocked. |
| 8 | Exchange creation | **A** | `completeFollow()` gate stack (blocked, restriction, cooldown, pairing, cap, duplicate) → PENDING transaction. |
| 9 | Exchange status | **A** | `TxStatus` PENDING → VERIFIED / DISPUTED / EXPIRED / COMPLETED with badges + hints. |
| 10 | Exchange expiration | **A** | Unconfirmed exchanges EXPIRE after 24h (config) with no punishment; pairing frees. |
| 11 | Confirmation workflow | **A** | Pending → counterpart confirms → coins released; demo auto-confirm (~8s) + one-shot 60-min reminder. |
| 12 | Disputes | **A** | Reasons, submit/withdraw, coins locked while DISPUTED, admin review (release/deny); demo auto-resolve. |
| 13 | Trust levels | **A** | L1 New → L5 Elite; score from exchanges + completion + age + completeness − disputes − flags; drives cooldown reduction. |
| 14 | Coin balances | **A** | Available / pending (derived from transactions) / lifetime earned / spent; never negative. |
| 15 | Coin transactions | **A** | Full ledger (`Transaction`: type/status/coins/note). |
| 16 | Daily earning limits | **A** | 25 coins/day cap in the economy service; progress bar + "held until tomorrow" handling. |
| 17 | Cooldowns | **A** | 10-min exchange cooldown, shortened by trust level & premium. |
| 18 | Quests | **A** | 5 quests with real qualifying actions; AVAILABLE/IN_PROGRESS/READY_TO_CLAIM/COMPLETED; pay once. |
| 19 | Daily check-in | **A** | Once/day, streak tracking, small bonus from day 3. |
| 20 | Follow-3-creators quest | **A** | Progress = real followed count. |
| 21 | Complete-profile quest | **A** | Progress = real profile-completeness %. |
| 22 | **Referral system** | **A** (was C) | v0.0.13 rebuild — persistent per-friend records, repeatable, idempotent. See below. |
| 23 | Share functionality | **A** | Native share sheet for app + referral link. |
| 24 | Premium | **B** | Mock purchase flow (no real payments), but the effects are real: badge, daily bonus, shorter cooldowns, extra boost limit, priority ranking. |
| 25 | Premium Pro | **B** | Same tier machinery; effects real, payments mocked. |
| 26 | Boosts | **A** | Standard/Boosted/Featured; real coin spend, 24h expiry, ranking multiplier, expiry notification. |
| 27 | Notifications / activity | **A** | In-app notification center (50-cap, unread badges) + history feed. |
| 28 | Blocking | **A** | Long-press → Block; blocked users leave discovery and can't be exchanged with; Settings → Blocked users. |
| 29 | Reporting | **A** | 6 reasons; reports land in the admin queue; resolvable. |
| 30 | Admin functionality | **B** | Real moderation queue (disputes, reports) + risk metrics + economy test controls; platform stats labeled mock. |
| 31 | Anti-abuse | **A** | Burst detection, Warning→Review→Restriction ladder (never auto-bans), repeat-pairing block, daily cap, dispute limit, referral daily-reward limit. |
| 32 | Creator content | **A** | Photos + short videos; demo creators get emoji tiles; own uploads via system picker. |
| 33 | Photo uploads | **A** | System picker → local file → profile/content grid. |
| 34 | Video uploads | **A** | System picker → local file → grid with play overlay + playback. |
| 35 | Settings | **A** | Theme, notifications, haptics, language (structure), discoverability, blocked users, subscription, logout, delete-account, help/about. |
| 36 | Theme persistence | **A** | Light/Dark/System applied app-wide and persisted (tested in `V12Test`). |

---

## The referral system (v0.0.13)

**Before:** a single `Boolean` (`Session.referralQualified`) gated a one-time
`q_invite` quest; once claimed the quest disappeared and no record of who was
invited existed.

**After:** a persistent, repeatable system (in `Models.kt` / `Session.kt` /
`AppState.kt` / `ReferralScreen.kt` / `EarnScreen.kt`):

- **Records** — `Referral`: `id`, `referrerUserId`, `referredUserId`,
  `referredName`, `referralCode`, `status`, `createdAt`, `qualifiedAt`,
  `rewardAmount` (5 coins), `rewardTransactionId`, `rewardedAt`.
- **Statuses** — `INVITED → REGISTERED → QUALIFYING → QUALIFIED → REWARDED`,
  plus `REJECTED` (fraud).
- **Repeatable** — a permanent "Invite Friends" section lives on the Earn tab
  and never disappears; each invited friend gets their own record with a claim
  button, so John → reward → Michael → reward repeats indefinitely.
- **Idempotent** — `claimReferralReward()` refuses a `REWARDED` record, refuses
  a non-`QUALIFIED` record, and stores `rewardTransactionId`/`rewardedAt`;
  verified by `V13Test` ("can never pay twice").
- **Anti-abuse** — `REFERRAL_DAILY_REWARD_LIMIT = 3` rewarded referrals/day
  (`Session.referralRewardsToday()`); verified by `V13Test`.
- **Code entry** — sign-up accepts an optional referral code (uppercased) and
  rejects your own code.
- **UI** — code + copy + native share, live stats ("Successful referrals: N ·
  Rewards earned: +N coins"), per-friend list with statuses.

**Known MVP limitations (no backend, by design):**
- Records/balances are device-local; a friend on another device can't credit you
  yet. `AppState.simulateFriendJoining()` / `simulateFriendQualifying()` drive the
  full lifecycle in the demo.
- The referral code is derived from the TikTok handle (`referralCode()`), so it
  changes if the handle is edited. Cosmetic; see follow-ups.

---

## Persistence guarantees (restart / logout / navigation)

All critical state is written through `Session` (SharedPreferences) and survives
app restart, navigation and screen refreshes: profile, coins, transactions, trust
stats, quests, referrals, disputes, achievements, boosts, premium, blocked users,
reports, content items, settings and theme. Pending coins are *derived* from
transactions (single source of truth); balances are clamped non-negative.

## Findings / recommended follow-ups (small, optional)

1. **Sign-out does not actually end the session** — the Profile/Settings sign-out
   buttons navigate to onboarding without clearing `Session.isLoggedIn`, so a
   restart drops the user back in signed-in. Fix: call a `Session.signOut()` that
   sets `logged_in = false` (data preserved) before navigating.
2. **Referral code stability** — generate-and-persist the code once instead of
   deriving it from the handle, so it survives handle edits.
3. **Premium / coin store** — payments are mock by design; needs real IAP when a
   backend/payment provider is added.

## Still missing / intentionally out of scope (would need a backend)

- Real server-side accounts & password storage; multi-device referral credit.
- Real TikTok data sync / verified-follow checking (the app never automates
  follows, by policy).
- Real in-app payments for Premium/coins (mock flow only, clearly labeled).
- Push notifications (an in-app notification center exists).
