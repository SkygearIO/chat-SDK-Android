# Skygear Chat Plugin for Android

[![CI Status](https://img.shields.io/travis/SkygearIO/chat-SDK-Android.svg?style=flat)](https://travis-ci.org/SkygearIO/chat-SDK-Android)
[![License](https://img.shields.io/github/license/skygeario/chat-SDK-Android.svg)](https://bintray.com/skygeario/maven/skygear-chat-android)

## Using Skygear Chat Android SDK

Please reference to Skygear Chat Quick Start guide for how to use the Skygear Chat Android SDK:
https://docs.skygear.io/guides/chat/quick-start/android/

The Chat Plugin doc is at [https://docs.skygear.io/android/plugins/chat/reference/](https://docs.skygear.io/android/plugins/chat/reference/). Check `ChatContainer` for how things work.

## Using Skygear Chat Android UIKit

To use the UIKit comes with Skygear Android, follow these steps:

### 1. Configure Android Chat SDK as mentioned in Quick start

That meant include these in `build.gradle` of the project:

```
allprojects {
    repositories {
        jcenter()
        maven {
            url 'https://maven.google.com/'
            name 'Google'
        }
        maven { url 'https://jitpack.io' }
    }
}
```

And include these dependency in `build.gradle` of the module:

```
dependencies {
    // other dependencies
    compile 'io.skygear:skygear:+'
    compile 'io.skygear.plugins:chat:+'
    compile 'io.skygear.plugins:chat_ui:+'
}
```

And configure Skygear Application Endpoint / API Key like these:

```java
public class MyApplication extends SkygearApplication {
    @Override
    public String getSkygearEndpoint() {
        return "change me with endpoint URL";
    }

    @Override
    public String getApiKey() {
        return "change me with API Key";
    }
}
```

### 2. Add Activity in your app manifest

In your app manifest, include these lines for UIKit Activities:

```xml
<activity android:name="io.skygear.plugins.chat.ui.ConversationActivity" />
```

### 3. Start the conversation view

Currently Android UIKit only have the conversation view, you can use it after getting the
conversation object from Chat API:

```java
Intent i = new Intent(context, ConversationActivity.class);
i.putExtra(
    ConversationActivity.ConversationIntentKey,
    conversation.toJson().toString() // conversation object create from chat sdk
);
context.startActivity(i)
```

### Bare minimal sample

A bare minimal apps. assume you have done the configuration above, got SkygearApplication configured
or configured manually with your application class, Here is an MainActivity as the launcher activity
to start a Conversation View with a hard-coded user:

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Container skygear = Container.defaultContainer(this);
        skygear.getAuth().signupAnonymously(new AuthResponseHandler() {
            @Override
            public void onAuthSuccess(Record user) {
                ChatContainer chatContainer = ChatContainer.getInstance(skygear);
                chatContainer.createDirectConversation("7cd03660-f82f-4619-9fae-3b0c87fec7e9", "Chat Demo", null, new SaveCallback<Conversation>() {
                    @Override
                    public void onSucc(@Nullable Conversation conversation) {
                        Intent i = new Intent(getApplicationContext(), ConversationActivity.class);
                        i.putExtra(ConversationActivity.ConversationIntentKey, conversation.toJson().toString());
                        getApplicationContext().startActivity(i);
                    }
                    @Override
                    public void onFail(@Nullable String s) {

                    }
                });
            }

            @Override
            public void onAuthFail(Error error) {

            }
        });
    }
}

```

## Support

For implementation related questions or technical support, please find us on the [official forum](https://discuss.skygear.io) or [community chat](https://slack.skygear.io).

If you believe you've found an issue with Skygear chat Android SDK, please feel free
to [report an issue](https://github.com/SkygearIO/chat-SDK-Android/issues).

## License & Copyright

```
Copyright (c) 2015-present, Oursky Ltd.
All rights reserved.

This source code is licensed under the Apache License version 2.0
found in the LICENSE file in the root directory of this source tree.
An additional grant of patent rights can be found in the PATENTS
file in the same directory.

```
