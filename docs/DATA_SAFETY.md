# TickTokBoost — Google Play Data Safety Form Answers

Mapped to the app's actual behavior (v0.0.13): **local-only storage, no
network calls to our servers, no SDKs, no accounts.**

## Q: Does your app collect or share any of the required user data types?
**→ No**

(All data — profile, coins, content, settings — is stored exclusively in the
app's private local storage. "Collect" per Play's definition means data
transmitted off the device; TickTokBoost transmits nothing.)

## Q: Is all of the user data collected by your app encrypted in transit?
**→ N/A** (nothing is transmitted)

## Q: Do you provide a way for users to request that their data is deleted?
**→ N/A** (nothing is collected; local data is fully deletable via
Settings → Delete account or by uninstalling)

## Independent security review / data deletion URL
Not required (no data collected). Privacy policy URL: host
`docs/PRIVACY_POLICY.md` at a public URL before submitting (e.g. GitHub Pages
on the repo, or your website).

## Permissions declared by the app

| Permission | Play form handling |
|---|---|
| `INTERNET` | Required only to open external links (creator TikTok profiles) in the user's browser/TikTok app. No server communication. Declare as normal use; no sensitive-permission declaration needed. |
| Media picking | Done via the system photo/video pickers — no storage permission is requested. |

## Content rating questionnaire (suggested answers)

- User-generated content shared with other users: **Yes** (creator profiles, photos/videos)
- Mature themes / violence / gambling / etc.: **No** to all
- Social features (users interact): **Yes**
- Expected rating: **Teen** (PEGI 13 / ESRB Teen equivalent)
