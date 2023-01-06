# Repository Details
Old Url|Current URL
--- |--- |
https://github.com/cere-io/sdk-android|https://github.com/cere-io/cere-sdk-android

## Release notes
### vNext
*
### v1.2.1
* Extended URL parameters `native.html` URL
### v1.2.0
* Updated `native.html` URL
### v1.1.0
* Add optional onboarding token parameter to init method
### v1.0.0
* First release

## Building documentation
For building documentation we use [orchid](https://orchid.run/)
Using standard approach of publishing to gh-pages branch.

To check documentation locally before publishing use:
```
./gradlew :docs:orchidRun
./gradlew :docs:orchidServe

```

Before running orchid deploy configure your githubToken.

```
export githubToken=slfgkhlkhslkdfhlsd
```

```
./gradlew :docs:orchidDeploy -PorchidEnvironment=prod
```

[Documentation site](https://cere-io.github.io/sdk-android/)

## Library publishing

See [dcendents](https://github.com/dcendents/android-maven-gradle-plugin)

On github project go to releases and create new release with same version as in root build.gradle
```
project.ext.set("versionName", "1.0.0")
```
