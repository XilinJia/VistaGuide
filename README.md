
# VistaGuide

This is a migration of the original [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor) in Java to Kotlin, to better support type and null safety.  The interface provided here is not fully compatible with the original, mainly due to the way of handling getters and setters in Kotlin.  Some properties need to be accessed directly rather than through the getters and setters.  Other than that, it works as the original library and can be used in the same way.

org.schabi.newpipe has been refactored to ac.mdiq.vista.  And of course the package renamed to VistaGuide, the group name has been refactored (from com.github.TeamNewPipe) to com.github.XilinJia, extractor.NewPipe is refactored to extractor.Vista

Up to date with version 0.24.2 of NewPipe Extractor

=================================
## Vista Guide

Vista Guide is a library for extracting things from streaming sites. It is a core component of [VoiVista](https://github.com/XilinJia/VoiVista), but could be used independently.

## Usage

Vista Guide is available at JitPack's Maven repo.

If you're using Gradle, you could add Vista Guide as a dependency with the following steps:

1. Add `maven { url 'https://jitpack.io' }` to the `repositories` in your `build.gradle`.
2. Add `implementation 'com.github.XilinJia:VistaGuide:INSERT_VERSION_HERE'` to the `dependencies` in your `build.gradle`. Replace `INSERT_VERSION_HERE` with the [latest release](https://github.com/XilinJia/VistaGuide/releases/latest).
3. If you are using tools to minimize your project, make sure to keep the files below, by e.g. adding the following lines to your proguard file:
 ```
## Rules for VistaGuide
-keep class ac.mdiq.vista.extractor.timeago.patterns.** { *; }
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.classfile.ClassFileWriter
-dontwarn org.mozilla.javascript.tools.**
```

**Note:** To use Vista Guide in Android projects with a `minSdk` below 33, [core library desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) with the `desugar_jdk_libs_nio` artifact is required.

### Testing changes

To test changes quickly you can build the library locally. A good approach would be to add something like the following to your `settings.gradle`:

```groovy
includeBuild('../VistaGuide') {
    dependencySubstitution {
        substitute module('com.github.XilinJia:VistaGuide') with project(':extractor')
    }
}
```
Another approach would be to use the local Maven repository, here's a gist of how to use it:

1. Add `mavenLocal()` in your project `repositories` list (usually as the first entry to give priority above the others).
2. It's _recommended_ that you change the `version` of this library (e.g. `LOCAL_SNAPSHOT`).
3. Run gradle's `ìnstall` task to deploy this library to your local repository (using the wrapper, present in the root of this project: `./gradlew publishToMavenLocal`)
4. Change the dependency version used in your project to match the one you chose in step 2 (`implementation 'com.github.XilinJia:VistaGuide:LOCAL_SNAPSHOT'`)

> Tip for Android Studio users: After you make changes and run the `install` task, use the menu option `File → "Sync with File System"` to refresh the library in your project.

## Supported sites

The following sites are currently supported:

- YouTube
- SoundCloud
- media.ccc.de
- PeerTube (no P2P)
- Bandcamp

## License

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](https://www.gnu.org/licenses/gpl-3.0.en.html)

Vista Guide is Free Software: You can use, study share and improve it at your
will. Specifically you can redistribute and/or modify it under the terms of the
[GNU General Public License](https://www.gnu.org/licenses/gpl.html) as
published by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

## Copyright

New files and contents in the project are copyrighted in 2024 by Xilin Jia.
