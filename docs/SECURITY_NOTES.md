# TickTokBoost — Security & Architecture Notes (v1.0.1)

## Single authority for rewards
Every coin mutation flows through `EconomyService` (grant/spend/hold), which enforces:
non-negative balances, duplicate-grant windows, daily earning caps, claim-once state
machines (quests, referrals, boost tasks) with idempotent transaction references
(`rewardTransactionId`), and a full ledger (`Session.transactions`) — every movement
is recorded with type, amount, status, related user and reason.

## Honest limitation: no backend (by design for the MVP)
This build is intentionally local-first: profiles, coins, trust, tasks, referrals,
content and settings persist on-device (SharedPreferences/JSON). Consequences:
- "Server-side validation / rate limiting / admin protection" (spec §8–§9) cannot
  exist yet; the central services layer is the closest single-source-of-truth
  equivalent in a single-user demo.
- The admin dashboard is a demo tool reachable from Settings — it is UI-level only.
  Before any multi-user release, a backend with authenticated APIs, server-side
  reward validation, server-side admin roles and rate limiting is REQUIRED, and
  client balances must never be trusted.
- Anti-abuse currently operates on-device (burst detection, pair cooldowns, daily
  caps, risk flags) — good UX hygiene, not real security.

## Secrets
No API keys, tokens or credentials are committed (verified by scan). CI signing
uses GitHub Actions secrets. The signing key is a shared DEMO keystore — replace
with a private release keystore before Play submission (see RELEASE_CHECKLIST.md).

## Data exposure
Leaderboard exposes display names + earned-points only. Reports/referrals store
no private data. Content uploads stay in app-private storage.
