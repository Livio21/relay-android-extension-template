# Relay Android extension template

This is a standalone, separately installed Android **source extension**. Relay binds only to its explicit exported service; it never loads this APK's code into Relay's process.

## Before building

1. Replace `example.relay.source` everywhere with your stable package and extension ID.
2. Pass the lowercase SHA-256 digest of the Relay APK certificate as `-PrelayHostCertificateSha256=…` when building. A debug Relay build and a release Relay build generally have different values.
3. Set the same extension package name and APK certificate digest in the signed Relay repository catalog's `androidPackageName` and `androidSigningCertificateSha256` fields.
4. Build with JDK 17: `../gradlew -p relay-android-extension-template :app:assembleDebug` when this template is beside Relay, or use your own Gradle wrapper.

## Publishing a repository

Run `scripts/create-signing-key.sh` once, place the generated public key in `repository.json` and Relay's trusted repository form, then run `scripts/sign-index.sh` after every `index.json` change. Commit `index.json` and `index.json.sig`; never commit `keys/`.

The committed catalog is a debug-only test release. Replace its APK URL, size, APK certificate digest, and artifact digest after producing your own signed release APK.

## Binder contract

Relay discovers exactly one exported service in the extension package with action `dev.relay.music.EXTENSION`, verifies the APK certificate, then binds using that concrete component. Requests use `IBinder.FIRST_CALL_TRANSACTION`; the request and reply are UTF-8 JSON strings no larger than 64 KiB.

The service must check `Binder.getCallingUid()` on **every** request and verify both the caller package and certificate before reading or answering a request. It must not accept implicit binding, expose storage paths, or return permanent stream URLs. The included `browse` method intentionally returns no tracks.

The sample includes the legacy signing branch needed for Android API 23–27.
