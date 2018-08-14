# hlgServiceDroid

HoloGuideでWebSocketのサーバやGPSによる位置情報取得を実現するAndroidプラグインです。

## 環境

### Android Studio

Android Studio 3.1.3
Build #AI-173.4819257, built on June 5, 2018
JRE: 1.8.0_152-release-1024-b02 amd64
JVM: OpenJDK 64-Bit Server VM by JetBrains s.r.o
Windows 10 10.0

### 実行環境

- Nexus 7
- Android 6.0.1 Marshmallow

## 依存ライブラリ

- classes.jar (Unity)
- netty-all-4.1.23.Final.jar (<https://netty.io/downloads.html>)
- stream-1.2.1.jar (<https://github.com/aNNiMON/Lightweight-Stream-API/releases>)

を/hlgservice/libsに配置してください。

## 使用

- 使用する際はUnity側スクリプトとsupport-compat-25.3.1.aarが必要です。
- hlgTerminalで実装しているのでこちらをビルドする必要はあまりありません。