# lametric-notify-plus

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/tycrek/lametric-notify-plus?style=flat-square)](https://github.com/tycrek/lametric-notify-plus/releases)

A better way to send Android notifications to a LaMetric Time smart clock.

I created this because I was disappointed with the lack of control you have over what notifications can be forwarded to the [LaMetric Time](https://lametric.com/time/overview). Luckily, they provide an API to send notifications to.

## Features

- [x] **Forward notifications** from ***any*** app on your device; no more overly-vague "Other" category!
- [x] **Search bar** to quickly locate apps
- [x] **Alphabetical sorting** to prevent headaches
- [x] **Small size**: the APK file is less than two megabytes

#### Coming soon

- [ ] Set custom icons
- [ ] Filter list to show only what has been enabled
- [ ] Cloud notifications

## Download

See the [Releases](https://github.com/tycrek/lametric-notify-plus/releases) page for downloads.

## Usage

1. Download & install the APK file (or build it yourself in Android Studio/your preferred method)
2. Select apps to forward notifications from. No need to tap Save as toggling the switches automatically saves them.
3. Enter the IP address and API key for your LaMetric device and tap Save.
4. Make sure you're on the same network as your LaMetric. You should start receiving notifications from the app.

**Please note**: The app sends notifications to the clock over HTTP. If you are concerned about sensitive data, do not enable that app within LaMetric Notify Plus until I have updated it to work with HTTPS.

## Permissions

1. **Notification Access**: required to read incoming notifications from your phone/tablet
2. **Location access**: not required. Used to automatically get WiFi SSID. SSID can be set manually if Location is denied.
