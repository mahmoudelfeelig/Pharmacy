# Pharmacy (Android / Kotlin / Jetpack Compose)

An Android app for a pharmacy workflow with two user roles (Patient / Pharmacist), backed by Firebase, with in-app SIP calling via an embedded Linphone SDK client.

## Features

**Patient**
- View profile (read-only)
- View nearby pharmacies on a map (static fallback markers; optional Firestore-backed list)
- Browse medications (grid)
- Add items to cart, edit/clear cart, place orders
- Call an available pharmacist (SIP)

**Pharmacist**
- View profile (read-only)
- Manage medications (add/edit/delete; upload image to Firebase Storage)
- Toggle availability for consultation calls
- Receive SIP calls inside the app (answer/decline, mute, speaker, hang up)

## Tech Stack

- Kotlin + Jetpack Compose (Material 3)
- Firebase Authentication (email/password)
- Firebase Firestore (users, medications, pharmacies, cart, orders)
- Firebase Storage (medication images)
- OSMDroid (map)
- Linphone SDK (embedded SIP stack)

## Project Structure

Multi-module setup:
- `:app` (navigation, SIP integration, call UI)
- `:core-domain` (domain models)
- `:core-data` (Firestore repositories + Firebase auth wrapper)
- `:core-ui` (theme, spacing/dimens)
- `:feature-auth` (login/register screens)
- `:feature-home` (home, meds, cart, consultation)
- `:feature-profile` (profile screen)
- `:feature-map` (map screen)

## Prerequisites

- Android Studio (Koala+ recommended)
- JDK 17
- A Firebase project (Auth + Firestore + Storage)
- Asterisk PBX in your lab network (Ubuntu VM is fine)
- Two Android devices on the same LAN as the PBX (or one device + emulator if networking supports it)

## Firebase Setup

1. Create a Firebase project.
2. Enable **Authentication → Email/Password**.
3. Enable **Cloud Firestore** and **Firebase Storage**.
4. Add an Android app in Firebase with the same application id as this project:
   - `com.example.pharmacy`
5. Download `google-services.json` and place it at:
   - `app/google-services.json`
6. Create these collections (or let the app create them as you use it):
   - `users`
   - `medications`
   - `pharmacies` (optional; app has fallback markers if empty)

**User document fields** (`users/{uid}`) used by the app:
- `email` (String)
- `displayName` (String)
- `role` (String: `patient` or `pharmacist`)
- `gender` (String)
- `online` (Boolean)
- `sipExtension` (String, recommended for pharmacists)

## SIP / Asterisk Setup (Lab)

This project uses **embedded Linphone SDK**, so you do **not** need the Linphone app installed on phones.

### 1) Create SIP users in Asterisk (PJSIP)

Edit:
- `/etc/asterisk/pjsip.conf`

Minimal example for your lab credentials (adjust codecs/context as needed):

```ini
[patient]
type=endpoint
context=internal
disallow=all
allow=ulaw,alaw
auth=auth_patient
aors=patient

[auth_patient]
type=auth
auth_type=userpass
username=patient
password=patient_pass

[patient]
type=aor
max_contacts=1
remove_existing=yes

[pharmacist]
type=endpoint
context=internal
disallow=all
allow=ulaw,alaw
auth=auth_pharmacist
aors=pharmacist

[auth_pharmacist]
type=auth
auth_type=userpass
username=pharmacist
password=pharmacist_pass

[pharmacist]
type=aor
max_contacts=1
remove_existing=yes
```

Reload:
```bash
sudo asterisk -rx "core reload"
```

### 2) Add dialplan rules (so extensions exist)

Edit:
- `/etc/asterisk/extensions.conf`

Add (or extend) an `internal` context:

```ini
[internal]
exten => pharmacist,1,Dial(PJSIP/pharmacist)
 same => n,Hangup()

exten => patient,1,Dial(PJSIP/patient)
 same => n,Hangup()
```

Reload dialplan:
```bash
sudo asterisk -rx "dialplan reload"
```

### 3) Debug/monitor registrations

```bash
sudo asterisk -rvvvvv
```

Inside the Asterisk CLI:
```text
pjsip set logger on
pjsip show contacts
pjsip show aor patient
pjsip show aor pharmacist
```

## App SIP Configuration

Update the PBX host/IP in:
- `app/src/main/java/com/example/pharmacy/sip/SipConfig.kt`

Key values:
- `SipConfig.PBX_DOMAIN` must match your PBX LAN IP (e.g., `172.20.10.4`).
- Credentials are currently mapped to:
  - `patient / patient_pass`
  - `pharmacist / pharmacist_pass`

For lab demos this is OK. For real deployments you should not hardcode credentials in the app.

## Run / Build

- Android Studio: open the project and Run `app`.
- CLI:
  - `./gradlew assembleDebug`

APK output:
- `app/build/outputs/apk/debug/app-debug.apk`

## How to Test (End-to-End)

1. Start Asterisk and confirm it listens on SIP (usually UDP 5060).
2. Install the app on **two** phones connected to the same Wi‑Fi as the PBX.
3. Create two Firebase users:
   - Patient account (role `patient`)
   - Pharmacist account (role `pharmacist`, set `sipExtension` to `pharmacist`)
4. Log in on the pharmacist phone → Consultation → toggle “Available for calls”.
5. Log in on the patient phone → Consultation → “Call pharmacist”.
6. Pharmacist phone should show the in-app call screen → Answer.

## Troubleshooting

**“registration attempt … will exceed max contacts of 1”**
- Your PBX still has a stale contact bound to the AOR.
- Fix by adding `remove_existing=yes` to the AOR and/or removing the contact:
  - `asterisk -rx "pjsip show contacts"`
  - `asterisk -rx "pjsip remove contact <contact_uri>"`

**“rejected because extension not found in context …”**
- Add the extension in `/etc/asterisk/extensions.conf` under the endpoint’s `context` and reload dialplan.

**No audio one-way**
- Ensure `RECORD_AUDIO` permission is granted on both phones.
- Check RTP reachability (firewall/Wi‑Fi isolation). Capture with Wireshark on the PBX and verify RTP flows both ways.

**Firebase login works on phone but not emulator**
- Use a Google Play emulator image, verify Play Services are up to date, and ensure the emulator has working network/time.

**Build cache path errors**
- Run: `./gradlew clean` then build again.

## Database Screenshot

See `initial database.jpg`.

## License

See `LICENSE`.
