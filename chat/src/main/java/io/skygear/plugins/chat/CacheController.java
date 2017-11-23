package io.skygear.plugins.chat;

class CacheController {
    private static CacheController sharedInstance;

    static CacheController getInstance() {
        if (sharedInstance == null) {
            RealmStore store = new RealmStore("SKYChatCache", false);
            sharedInstance = new CacheController(store);
        }

        return sharedInstance;
    }

    RealmStore store;

    CacheController(RealmStore store) {
        this.store = store;
    }
}
