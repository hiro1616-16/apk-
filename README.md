# Device Monitor Android project

Features included in this project snapshot:
- Android home-screen AppWidget (resizable) showing model, SoC, CPU, RAM, battery, storage, Android version and temperature when accessible
- Settings for show/hide display fields, pastel background/text/accent color palettes and background opacity
- Foreground monitoring service with adjustable 5–60 second refresh setting, monitoring toggle and boot-resume option
- Widget tap opens the app settings
- GitHub Actions workflow builds a debug APK and publishes it as an artifact

## Build APK
Push this project to a GitHub repository, open Actions, run `Build Device Monitor APK`, then download the `DeviceMonitor-debug-apk` artifact.

## Limitations
CPU temperature and CPU usage may be unavailable or approximate on some devices. Android and launchers may throttle widget refreshes; the foreground service refresh loop does not guarantee every widget repaint will occur at the configured interval. The current color and display toggles are app-wide (per-widget-instance customization and drag ordering are not yet implemented).
