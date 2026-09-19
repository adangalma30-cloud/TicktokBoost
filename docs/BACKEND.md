# TickTokBoost — Backend & Cloud Foundation (v1.0.3)

## Architecture (as shipped)

```
CLIENT / UI (Compose screens)
        ↓  only through
APPLICATION SERVICES (AppState, Quests, EconomyService, AuthService)
        ↓  write-through
DATABASE MIRROR (DatabaseMirror — repository seam)
        ↓
LOCAL DATABASE (Room/SQLite — ticktokboost.db)      ← authoritative durable store
```

- **UI never touches balances directly** — every coin movement flows through
  EconomyService, which records a ledger row (user, amount, type, reason,
  timestamp, idempotent reference) in the `point_transactions` table.
- **Schema** (`data/db/Entities.kt`): users, profiles, content, boosts,
  boost_completions, point_transactions, daily_checkins, notifications.
  Balances are derived (`SUM(amount)`), never stored raw — same principle a
  server would enforce.
- **Content ownership** is enforced at the SQL level
  (`DELETE ... WHERE id = :id AND userId = :userId`).
- **Auth foundation** (`data/auth/AuthService.kt`): register/login/logout with
  PBKDF2WithHmacSHA256 password hashing (24k iterations, per-user salt).
  Passwords are never stored in plaintext. Legacy demo installs transparently
  gain an account on first login.
- The legacy SharedPreferences layer remains the live read path during
  migration; Room is kept consistent on every mutation (write-through), so the
  DB can become the single source of truth without further UI changes.

## Activating a real cloud backend (the seam is ready)

1. Create a Supabase (or Firebase) project; the SQL schema in
   `app/src/main/java/com/tiktokboost/app/data/db/Entities.kt` maps 1:1 to
   server tables.
2. Add base URL + anon key as CI secrets (never in code):
   `BACKEND_BASE_URL`, `BACKEND_ANON_KEY` → wired via `buildConfigField`.
3. Replace `DatabaseMirror` internals with remote calls (or add a sync layer);
   application services and UI stay untouched by design.
4. Server must re-validate every reward transaction using the same rules as
   EconomyService (caps, cooldowns, idempotent references) — never trust
   client-sent amounts.
