# Skygear Chat Plugin for Android

Skygear Server is a cloud backend for making the web and mobile app development easier. [https://skygear.io](https://skygear.io)

And the Chat Plugin make Skygear group conversation possible.

The Skygear Chat Plugin for Android helps your app work with Skygear Chat Plugin.

## Getting Started

To get started, checkout [Skygear Chat Plugin](https://github.com/SkygearIO/chat), and have the Chat Plugin running by `docker-compose up -d && docker-compose scale plugin=2`.

You can also sign up Skygear Developer Portal at [https://portal.skygear.io](https://portal.skygear.io) and use `git push` the Chat Plugin to your portal cloud code endpoint (Refer detail on [https://docs.skygear.io/cloud-code/guide](https://docs.skygear.io/cloud-code/guide) or [https://github.com/skygear-demo/skygear-catapi](https://github.com/skygear-demo/skygear-catapi) for an example).

After the Chat Plugin is running, remember to initialize Chat Plugin by `curl http://<SKYGEAR ENDPOINT>/chat-plugin-init`.

## Some modifications

The sample app needs some modification before it works, update the `endpoint` and `key` at `chat_example/src/main/java/io/skygear/chatexample/MainApp.kt` to the correct value of endpoint and api key.

## Now you are ready

The sampe app now is ready, try to sign up some users, make them chat in a conversation group.

## Chat Plugin for Android

Chat plugin is located at [https://github.com/SkygearIO/chat-SDK-Android/tree/master/chat/src/main/java/io/skygear/plugins/chat](https://github.com/SkygearIO/chat-SDK-Android/tree/master/chat/src/main/java/io/skygear/plugins/chat), there are 5 containers in it:

1. chatUserContainer - on behalf of retrieveing Chat Plugin users on Skygear cloud
2. conversationContainer - on behalf of creating, querying, modifying, and delete a conversation
3. messageContainer - on behalf of querying and sending message
4. subContainer - on behalf of subscribing conversation changing
5. unreadContainer - on behalf of retrieveing total unread message count

## Support

If you believe you've found an issue with Skygear Android SDK, please feel free
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