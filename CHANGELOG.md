## 1.6.3 (2018-09-05)

### Bug Fixes

- Upgrade Skygear SDK to v1.6.2 for websocket issue

## 1.6.2 (2018-08-23)

### Features

- Add pagination support for get conversation API (#207)

## 1.5.1 (2018-06-05)

### Bug Fixes

- Fix record acl become public read after updating conversation


## 1.5.0 (2018-05-16)

### Features

    - API updates:
        - Update getMessages api with cache support #94, #131
        - Introduce message operation cache objects for fail operation handling #130, #131, #132
        - Fetch participants api with cache support #153, #154
        - Add `subscribeToConversation` and `subscribeToUserChannel` function #195
    - UIKit updates:
        - Support UI and Text customization support in ConversationView
        - Update UIKit with local cache

### Bug Fixes

    - API updates:
        - Update admin id option keys of chat:create_conversation SkygearIO/chat#192
    - UIKit updates:
        - Fix android 4 sending image from camera bug, grant Read & Write permission manually #162
        - Fix bubble color in android 4, should not allow user send out empty
        - Fix cannot load the image in image view in android 4 #163
        - Fail trailing space username make the app crash bug #163
        - Image rotation bug fix and refactor the message constructing in conversation view
    - Voice message bug fix #152 #158

### Other Notes

    - API updates:
        - Rename ChatUser to Participant #170
        - Update API callback, rename function `onSucc` to `onSuccess` #121
        - Support beforeMessageID parameter for query messages #140
        - ChatContainer.editMessage() accepts metadata and asset #31
    - Example updates:
        - Update chat example with api test
        - Improve admin and participant list display #164
    - Separate UI Kit Classes from chat project

