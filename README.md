# ðŸŽ® Ribbon Launcher

Ribbon Launcher is a fullscreen, gamepad- and touch-friendly Android launcher that displays installed games in a horizontally scrolling ribbon. Inspired by the Xperia PLAY interface and built for modern Android devices, it's lightweight, immersive, and optimized for landscape use.

---

## âœ¨ Features

- Horizontal ribbon-style UI for launching installed games
- Touch and controller (D-pad) navigation
- Immersive fullscreen mode
- Fallback to Play Store if game can't be launched
- Modern Jetpack Compose UI with Material 3
- Light/dark theme ready (OLED-friendly)
- Clean MVVM architecture
- Landscape-only layout

---

## ðŸ“¦ Package Name

```
com.retrobreeze.ribbonlauncher
```

---

## ðŸ“± Screenshots

*(To be added)*

---

## ðŸ§  Architecture

- **Jetpack Compose**: For all UI components
- **ViewModel (MVVM)**: For clean state management
- **Coil**: For loading app icons
- **Kotlin DSL**: For Gradle configuration

---

## ðŸ“ Project Structure

```
/app
  â”œâ”€â”€ MainActivity.kt
  â”œâ”€â”€ LauncherViewModel.kt
  â”œâ”€â”€ GameEntry.kt
  â”œâ”€â”€ GameCarousel.kt
  â”œâ”€â”€ ui/theme/
```

---

## ðŸ§ª Development Setup

1. Clone the repo:
   ```bash
   git clone https://github.com/your-username/ribbon-launcher.git
   cd ribbon-launcher
   ```

2. Open in Android Studio

3. Run the app on a device or emulator with landscape orientation

---

## ðŸš« Limitations (v0.1)

- No folders or categories
- No game info screen yet
- No theming options in UI
- No persistent settings

---

## ðŸ“‹ License

MIT License. See [LICENSE](LICENSE) for details.
