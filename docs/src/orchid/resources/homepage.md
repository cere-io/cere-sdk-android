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

Initialize CereModule inside your custom Application class, and call init method on CereModule with appId, integrationPartnerUserId and onboarding access token. 

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
            this.cereModule.init("242", "userID", "some access token");
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

