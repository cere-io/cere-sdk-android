# Repository Details
Old Url|Current URL
--- |--- |
https://github.com/cere-io/sdk-android|https://github.com/cere-io/cere-sdk-android

## Setup

Minimal supported android SDK version is KITKAT.
```
minSdkVersion 19
```

Add jitpack repository to build.gradle file.
```
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add kotlin library and cere_sdk library dependencies to your /app/build.gradle file.

```
dependencies {
    api            "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72"
    implementation "com.github.cere-io:sdk-android:0.1"
}
```

## Initialization

Initialize CereModule inside your custom Application class, and call init method on CereModule with appId, integrationPartnerUserId, authType, accessToken for all authType except AuthType.EMAIL, email for AuthType.EMAIL , password for AuthType.EMAIL.

```java
package io.cere.sdk_android_example;

import android.app.Application;
import android.util.Log;

import io.cere.cere_sdk.CereModule;
import io.cere.cere_sdk.InitStatus;

public class CustomApplication extends Application {
    private static String TAG = "CustomApplication";
    private CereModule cereModule = null;
    public void onCreate() {
        super.onCreate();
        if (CereModule.getInstance(this).getInitStatus() == InitStatus.Initialised.INSTANCE) {
            this.cereModule = CereModule.getInstance(this);
        } else {
            //you can handle other initialization statuses (Uninitialized, Initializing, InitializationError)
            this.cereModule = CereModule.getInstance(this);
            this.cereModule.setOnInitializationFinishedHandler(() -> {
                this.cereModule.sendEvent("APP_LAUNCHED_TEST", "{'locationId': 10}");
                return;
            });
            this.cereModule.setOnInitializationErrorHandler((String error) -> {
                    Log.e(TAG, error);
            });
            this.cereModule.init(InitConfig("environment", "base_url", "242", "userID", AuthType.FIREBASE, "some access token", null, null));
        }
    }
}
```

Inside your MainActivity get an singleton instance of CereModule.

```java
package io.cere.sdk_android_example;

import androidx.appcompat.app.AppCompatActivity;
import io.cere.cere_sdk.CereModule;

public class MainActivity extends AppCompatActivity {

    private CereModule cereModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.cereModule = CereModule.getInstance(this.getApplication());
    }
}
```

## Send events

Call cereModule.sendEvent to trigger your event with custom payload.

For quick integration test, you can use "APP_LAUNCHED_TEST" event, which will trigger display of "Hello world!" text inside android modal dialog.

```java
  this.cereModule.sendEvent("APP_LAUNCHED_TEST", "{}");
```

## Example application

Take a look on [Example application](https://github.com/cere-io/sdk-android-example).

## Documentation

[Documentation site](https://cere-io.github.io/cere-sdk-android/)

## Library publishing

On github project go to releases and create new release with same version as in root build.gradle
```
project.ext.set("versionName", "1.0.0")
```

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