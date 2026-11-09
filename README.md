# لغت‌یار (Vocab App)

An offline Android app for learning English vocabulary from Elementary to Advanced, for Persian speakers. Lessons teach each word, exercises fix it in memory, a unit test unlocks the next unit, and FSRS spaced repetition brings words back right before they're forgotten. Progress moves between phones as one backup file.

- Product design: [docs/product-design.md](docs/product-design.md)
- Adding units: [docs/CONTENT_FORMAT.md](docs/CONTENT_FORMAT.md)
- Visual design: the **Vocab Lavender** design system and the **Vocab App Screens** canvas in Claude Design.

## Run it

1. Open this folder in Android Studio (2025.3 or newer).
2. Let Gradle sync. The project uses its bundled JDK 21; the toolchain targets Java 17.
3. Pick an emulator or a phone with USB debugging and press Run.

From a terminal:

```bash
./gradlew assembleDebug
```

```bash
./gradlew test
```

The debug build installs as `com.vocab.app.debug`, so it can sit beside a release build.

### Install on your phone (نصب روی گوشی)

**[⬇️ دانلود لغت‌یار (APK)](https://github.com/Zahra-Sarabi/vocab-app/releases/latest/download/loghatyar-debug.apk)**

1. Open the link above on the phone. It always downloads the newest release.
2. Open the downloaded file and tap **Install**.
3. The first time, Android asks to allow installs from that app (Chrome, Files, …): turn on **Allow from this source**, go back, and tap **Install** again.
4. If Play Protect warns that the app is unknown, tap **More details › Install anyway**. It warns because the app is not from Google Play.

Updating: open the link again and install over the old version; progress is kept. Before moving to another phone or uninstalling, use **Settings › پشتیبان‌گیری** to export a backup file.

**Publishing a new version:** run `./gradlew assembleDebug`, rename `app/build/outputs/apk/debug/app-debug.apk` to `loghatyar-debug.apk`, and attach it to a new release on GitHub (**Releases › Draft a new release**). The link above picks up the latest release by itself, as long as the file keeps that name.

With USB debugging on, `adb install -r app/build/outputs/apk/debug/app-debug.apk` installs it directly.

## Architecture

Clean architecture in Gradle modules. Arrows point at what a module may depend on.

```
app ──► feature/* ──► core/domain ──► core/model
 │                      ▲
 └──► core/data ────────┘──► core/database, core/datastore
      core/notifications ──► core/domain
      core/designsystem ──► core/model
```

| Module | What lives there |
| --- | --- |
| `core/model` | Plain Kotlin data: words, units, cards, settings, questions. No Android. |
| `core/domain` | The rules, with no Android: the FSRS scheduler, question generation, answer checking, pass marks and unlocking, and one use case per user action. Repository **interfaces** live here. |
| `core/data` | Implementations: content from `assets/content`, progress in Room, settings in DataStore, backup files, text-to-speech, Hilt bindings. |
| `core/database` | Room entities, DAOs and the database. Schemas are exported to `core/database/schemas`. |
| `core/datastore` | User settings in Preferences DataStore. |
| `core/designsystem` | The Lavender theme: colors and shadows from the design tokens, Vazirmatn and Nunito, the clay modifier, components and the 3D illustrations. |
| `core/notifications` | Daily reminder and streak alert with WorkManager. |
| `feature/today` | Home: goal, review, continue, topics. |
| `feature/course` | The level path and the unit detail with its steps. |
| `feature/lesson` | Learn cards, every exercise type, the unit test and the result. |
| `feature/review` | The daily spaced-repetition session. |
| `feature/progress` | Stats, activity calendar, level mastery, badges. |
| `feature/settings` | Backup export and import, daily goal, reminders, accent, theme. |
| `build-logic` | Gradle convention plugins, so every module is configured the same way. |

Libraries: Jetpack Compose with Material 3, Hilt, Room, DataStore, WorkManager, Navigation Compose with type-safe routes, kotlinx.serialization. Versions are in `gradle/libs.versions.toml`.

### Where to make common changes

| Change | Where |
| --- | --- |
| Add a unit or level | `app/src/main/assets/content/` (see the content guide) |
| Pass mark, stars, mastery, unlocking | `core/domain/.../progress/ProgressRules.kt` |
| Review scheduling | `core/domain/.../srs/Fsrs.kt` |
| A new exercise type | add a `Question` subtype in `core/model`, generate it in `QuestionGenerator`, draw it in `feature/lesson/LessonScreen.kt` |
| Colors, shadows, type | `core/designsystem/.../theme/` (keep in step with the design system tokens) |
| A new screen | a new `feature/<name>` module with the `vocab.android.feature` plugin, wired into `app/.../navigation/VocabApp.kt` |
| A database column | entity in `core/database`, bump the version, add a migration, add the field to the backup with a default |

## Data and backups

- Content ships inside the app; it is never in the database or the backup.
- Progress (cards, step scores, review log, daily activity) is in Room; settings are in DataStore.
- **Settings › پشتیبان‌گیری** writes everything to one JSON file through the system file picker (Drive, Files, Telegram…). Restoring shows a preview first and keeps a copy of the current state in `files/backups/` (last three).
- The backup format is versioned (`schemaVersion`). Add new fields with defaults so older files keep loading.
- Android Auto Backup is also on for the database and settings, as a second safety net.

## Licenses

- 3D illustrations: Microsoft Fluent Emoji, MIT — `docs/licenses/fluent-emoji-MIT.txt`
- Fonts: Vazirmatn and Nunito, SIL Open Font License — `docs/licenses/`
