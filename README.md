# TasteTrip (prototype)

An Android app that matches trips to your taste.

1. **Rate** a few things you've already done: loved it, fine, or didn't like it, with an optional "why".
2. **My taste**: TasteTrip learns which traits you like (nature, adventure, food, nightlife and so on) from your ratings and the words in your "why". It then finds your **taste twins**, travellers whose ratings line up with yours.
3. **Trip**: pick a city, number of days, pace and food needs (vegan, gluten-free). TasteTrip ranks experiences by how well they fit your profile and what your taste twins thought of them. It then groups them into days by area and time of day. Tap "Not for me" on a stop and it learns from that and replans.

## Prototype limits

- Four New Zealand cities (Auckland, Rotorua, Queenstown, Wellington), about ten experiences each.
- Place names are real. The descriptions are short and general. **Every traveller and review is made up**, generated so the matching has something to work with.
- Everything runs on the phone. No account, no network, no AI calls.
- Travel times between areas, opening hours and prices aren't modelled yet.

## Build

Android Studio's JDK (17+), Android SDK 37:

```
./gradlew testDebugUnitTest assembleDebug
```

The APK is `app/build/outputs/apk/debug/app-debug.apk`.
