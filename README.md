# FOSDEM Companion

Advanced native Android schedule browser application for the [FOSDEM](http://fosdem.org/) conference in Brussels, Belgium.

This is a new implementation of the [legacy FOSDEM app](https://github.com/rkallensee/fosdem-android/). The code has been rewritten from scratch and the features have been extended. It uses loaders and fragments extensively and is backward compatible up to Android 2.1 thanks to the support library.

The name FOSDEM and the gear logo are registered trademarks of FOSDEM VZW. Used with permission.

<a href="https://f-droid.org/repository/browse/?fdfilter=fosdem&fdid=me.osorio.eurobsd" target="_blank">
  <img src="https://f-droid.org/badge/get-it-on.png" height="80"/>
</a>
<a href="https://play.google.com/store/apps/details?id=me.osorio.eurobsd" target="_blank">
  <img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height="80"/>
</a>

## How to build

All dependencies are defined in ```app/build.gradle```. Import the project in Android Studio or use Gradle in command line:

```
./gradlew assembleRelease
```

The result apk file will be placed in ```app/build/outputs/apk/```.

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Used libraries

* [Android Support Library](http://developer.android.com/tools/support-library/) by The Android Open Source Project
* [ViewPagerIndicator](http://viewpagerindicator.com/) by Jake Wharton
* [PhotoView](https://github.com/chrisbanes/PhotoView) by Chris Banes

## Contributors

* Christophe Beyls
