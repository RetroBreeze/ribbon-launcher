# Ribbon Launcher — Project Context for AI Agents

## 📱 Summary

Ribbon Launcher is a fullscreen Android launcher focused on displaying installed games in a horizontally scrolling, gamepad- and touch-friendly ribbon. Inspired by the Xperia PLAY interface, it uses Jetpack Compose and a modern architecture (MVVM) with Material 3 styling. The UI is immersive, responsive, and optimized for controller navigation.

---

## 🧠 Architecture

### UI
- Built entirely with Jetpack Compose
- Rooted in `MainActivity.kt` → `LauncherScreen()`
- `GameCarousel.kt`: Horizontally scrolling row of icons
- Game artwork loaded with Coil from app metadata
- UI adapts to both touch and D-pad navigation

### ViewModel
- `LauncherViewModel.kt` uses `AndroidViewModel`
- Loads all installed apps with `CATEGORY_GAME`
- Exposes a `mutableStateOf<List<GameEntry>>` for Compose
- No backend or persistence layer yet

### Data Model
- `GameEntry.kt`: Data class with:
  - `packageName`: `String`
  - `displayName`: `String`
  - `icon`: `Drawable`

---

## 🧭 Display Orientation

- The launcher is **landscape-only**
- All UI components and interactions are designed for landscape
- The app should **force landscape orientation** using the manifest:

```xml
<activity
    android:name=".MainActivity"
    android:screenOrientation="landscape" />
```

- Portrait mode is not supported or handled
- Padding, layout spacing, and carousel behavior are all tuned for wide screens

---

## 🎮 Input Behavior

| Input               | Action                                    |
|---------------------|-------------------------------------------|
| D-pad Left/Right or swipe | Scroll ribbon horizontally             |
| D-pad Up or swipe up      | Open game info screen (planned)        |
| A / X / Enter / Tap       | Launch selected game                   |
| Long press (planned)      | Open game menu/folder                  |

- Focus should center on one large game icon
- Neighboring icons shrink slightly
- Gamepad navigation is required
- All icons must be square and evenly padded

---

## 🎨 Theming

- Uses `MaterialTheme` with Material 3
- Supports light, dark, and OLED themes (future)
- Theme defined in `GameLauncherTheme.kt`
- Use consistent `dp` spacing and intuitive layouts
- Minimalist ribbon background (no imagery yet)

---

## 🧱 Build / Tooling

- Android SDK 26+ (target SDK 36)
- Kotlin + Kotlin DSL (`build.gradle.kts`)
- Jetpack Compose BOM
- ViewModel + Compose state (`mutableStateOf`)
- Coil for image loading
- No XML layouts — Compose only
- No Fragments, Navigation Component, or Room DB

---

## 🚫 Constraints

- ❌ No Fragments or XML views
- ❌ No Navigation Component
- ❌ No playtime tracking
- ❌ No cloud services or login
- ❌ No Room or persistence (yet)
- ✅ Clean, in-memory-only state for now

---

## 📦 Package Name

`com.retrobreeze.ribbonlauncher`

---

## 🔮 Feature Roadmap

_(Planned for future versions)_
- System top bar: clock, battery, Wi-Fi/Bluetooth status
- Game info panel (up swipe or D-pad up)
- Folders (like PlayStation XMB)
- Custom button label styles (Xbox, PS, Switch)
- Theme and wallpaper selection
- Launcher registration (set as default HOME)

---

## 🧪 Agent Guidance

- Use Jetpack Compose idioms only
- Use `@Composable` functions, `MaterialTheme`, and `Modifier` properly
- Use `viewModel()` in Compose to access `LauncherViewModel`
- Game list comes from `viewModel.games`, a `List<GameEntry>`
- Use `Coil` (`rememberAsyncImagePainter`) to show app icons from `Drawable`
- When launching games:
  - Try `PackageManager.getLaunchIntentForPackage()`
  - If null, link to Play Store with app’s package ID

---

## ✅ Completion Preferences

- Keep Composables clean, modular, and theme-aware
- Use `MaterialTheme.colorScheme` and `typography`
- Use `@Preview` for all UI components where relevant
- Always assume controller support is required
