# Relay Android extension template

This is a standalone, separately installed Android **source extension** using Relay's Mihon-style source API. Relay verifies the APK signer against its signed repository catalog, then loads the declared `RelaySource`/`RelaySourceFactory` class from the APK.

## Before building

1. Replace `example.relay.source` everywhere with your stable package and extension ID.
2. Implement `RelaySource` or `RelaySourceFactory` and declare it in `relay.source.class` in `app/src/main/AndroidManifest.xml`.
3. Set the same extension package name and APK certificate digest in the signed Relay repository catalog's `androidPackageName` and `androidSigningCertificateSha256` fields.
4. Build Relay's source API first, then build the extension with JDK 17: `../gradlew :relay-source-api:jar` followed by `../gradlew -p relay-android-extension-template :app:assembleDebug`.

## Publishing a repository

Run `scripts/create-signing-key.sh` once, place the generated public key in `repository.json` and Relay's trusted repository form, then run `scripts/sign-index.sh` after every `index.json` change. Commit `index.json` and `index.json.sig`; never commit `keys/`.

The committed catalog is a debug-only test release. Replace its APK URL, size, APK certificate digest, and artifact digest after producing your own signed release APK.

## Source API contract

Relay reads `relay.source.api` and `relay.source.class` from the extension manifest, verifies API version 1, creates a child-first APK class loader, then instantiates the declared class. Relay's source API itself is parent-loaded so the extension must use it as a `compileOnly` dependency and must not package a second copy.

The source owns its own HTTP API requests, authentication, and site parsing. It returns normalised track records to Relay; it never writes Relay's database or controls playback directly. The included demo source exposes three short test streams so browsing, search, and playback can be checked before building a real provider.

Because trusted source code runs in Relay's process, only add repositories whose signing keys you trust. Relay keeps an APK disabled if its signer, source metadata, API version, or entry class fails validation.
