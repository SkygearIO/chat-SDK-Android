/*
 * Copyright 2017 Oursky Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.skygear.plugins.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.skygear.skygear.Error;
import io.skygear.skygear.Record;
import io.skygear.skygear.Reference;

import static io.skygear.plugins.chat.MessageSubscriptionCallback.EVENT_TYPE_CREATE;
import static io.skygear.plugins.chat.MessageSubscriptionCallback.EVENT_TYPE_DELETE;
import static io.skygear.plugins.chat.MessageSubscriptionCallback.EVENT_TYPE_UPDATE;

@RunWith(AndroidJUnit4.class)
public class CacheControllerTest {
    static Context context;
    static DateTimeFormatter formatterWithMS;
    CacheController cacheController;

    void setUpFixture() throws Exception {
        RealmStore store = this.cacheController.store;

        int count = 10;
        Message[] messages = new Message[count];
        for (int i = 0; i < count; i++) {
            JSONObject messageJson = new JSONObject();
            messageJson.put("_id", "message/m" + i);
            Date createdAt = new Date(i * 1000);
            messageJson.put("_created_at", formatterWithMS.print(new DateTime(createdAt)));

            Record record = Record.fromJson(messageJson);
            Reference conversationRef = new Reference("conversation", "c" + (i % 2));
            record.set("conversation", conversationRef);
            record.set("edited_at", new Date(i * 2000));
            record.set("deleted", false);

            Message message = new Message(record);
            messages[i] = message;
        }

        store.setMessages(messages);
    }

    void tearDownFixture() {
        this.cacheController.store.deleteAll();
    }

    @BeforeClass
    static public void setUpClass() {
        formatterWithMS = ISODateTimeFormat.dateTime().withZoneUTC();
        context = InstrumentationRegistry.getContext().getApplicationContext();
        Realm.init(context);
    }

    @AfterClass
    static public void tearDownClass() {
        formatterWithMS = null;
        context = null;
    }

    @Before
    public void setUp() throws Exception {
        RealmStore store = new RealmStore("test", true, false);
        this.cacheController = new CacheController(store);
        setUpFixture();
    }

    @After
    public void tearDown() {
        tearDownFixture();
        this.cacheController = null;
    }

    @Test
    public void testInitialState() {
        final boolean[] checkpoints = new boolean[] { false };
        Conversation conversation = new Conversation(new Record("conversation", "c0"));
        this.cacheController.getMessages(conversation, 100, null, null, new GetCallback<List<Message>>() {
            @Override
            public void onSuccess(@Nullable List<Message> messages) {
                Assert.assertEquals(messages.size(), 5);
                for (int i = 0; i < 5; i++) {
                    Message message = messages.get(4 - i);
                    Assert.assertEquals(message.getRecord().getCreatedAt(), new Date(i * 2000));
                    Assert.assertEquals(message.getConversationId(), "c0");
                }
                checkpoints[0] = true;
            }

            @Override
            public void onFail(@NonNull Error error) {
                Assert.fail("Should not get fail callback");
            }
        });

        Realm realm = this.cacheController.store.getRealm();
        RealmResults<MessageCacheObject> results = realm.where(MessageCacheObject.class).findAll();
        Assert.assertEquals(results.size(), 10);

        Assert.assertTrue(checkpoints[0]);
    }

    //region Message Cache

    @Test
    public void testGetMessagesUpdateCache() throws Exception {
        final boolean[] checkpoints = new boolean[] { false };
        Conversation conversation = new Conversation(new Record("conversation", "c0"));

        int count = 5;
        Message[] messages = new Message[count];
        for (int i = 0; i < count; i++) {
            JSONObject messageJson = new JSONObject();
            messageJson.put("_id", "message/m" + ((i + 3) * 2));
            Date createdAt = new Date((i + 3) * 2000);
            messageJson.put("_created_at", formatterWithMS.print(new DateTime(createdAt)));

            Record record = Record.fromJson(messageJson);
            Reference conversationRef = new Reference("conversation", "c0");
            record.set("conversation", conversationRef);
            record.set("edited_at", new Date(50000));
            record.set("body", "fetched message");
            record.set("deleted", false);

            Message message = new Message(record);
            messages[i] = message;
        }

        JSONObject messageJson = new JSONObject();
        messageJson.put("_id", "message/m0");
        Date createdAt = new Date(0);
        messageJson.put("_created_at", formatterWithMS.print(new DateTime(createdAt)));

        Record record = Record.fromJson(messageJson);
        Reference conversationRef = new Reference("conversation", "c0");
        record.set("conversation", conversationRef);
        record.set("deleted", true);

        Message deletedMessage = new Message(record);

        this.cacheController.didGetMessages(messages, new Message[]{deletedMessage});
        this.cacheController.getMessages(conversation, 100, null, null, new GetCallback<List<Message>>() {
            @Override
            public void onSuccess(@Nullable List<Message> messages) {
                Assert.assertEquals(messages.size(), 7);
                for (int i = 1; i < 8; i++) {
                    Message message = messages.get(7 - i);
                    Assert.assertEquals(message.getCreatedTime(), new Date(i * 2000));
                    if (i >= 3) {
                        Assert.assertEquals(message.getBody(), "fetched message");
                        Assert.assertEquals(message.getRecord().get("edited_at"), new Date(50000));
                    } else {
                        Assert.assertNull(message.getBody());
                        Assert.assertEquals(message.getRecord().get("edited_at"), new Date(i * 4000));
                    }
                }
                checkpoints[0] = true;
            }

            @Override
            public void onFail(@NonNull Error error) {
                Assert.fail("Should not get fail callback");
            }
        });

        Realm realm = this.cacheController.store.getRealm();
        RealmResults<MessageCacheObject> results = realm.where(MessageCacheObject.class).equalTo("conversationID", "c0").findAll();
        Assert.assertEquals(results.size(), 8);

        results = results.where().equalTo("editionDate", new Date(50000)).findAll();
        Assert.assertEquals(results.size(), 5);

        Assert.assertTrue(checkpoints[0]);
    }

    @Test
    public void testSaveMessageUpdateCache() throws JSONException {
        final boolean[] checkpoints = new boolean[] { false };
        Conversation conversation = new Conversation(new Record("conversation", "c0"));

        JSONObject messageJson = new JSONObject();
        messageJson.put("_id", "message/mm1");

        Record record = Record.fromJson(messageJson);
        Reference conversationRef = new Reference("conversation", "c0");
        record.set("conversation", conversationRef);
        record.set("body", "new message");

        final Message messageToSave = new Message(record);
        messageToSave.sendDate = new Date(50000);

        this.cacheController.didSaveMessage(messageToSave);

        Realm realm = this.cacheController.store.getRealm();
        MessageCacheObject result = realm.where(MessageCacheObject.class).equalTo("recordID", "mm1").findFirst();
        Assert.assertEquals(result.sendDate, messageToSave.sendDate);

        Assert.assertEquals(realm.where(MessageCacheObject.class).findAll().size(), 11);

        this.cacheController.getMessages(conversation, 1, null, null, new GetCallback<List<Message>>() {
            @Override
            public void onSuccess(@Nullable List<Message> messages) {
                Message message = messages.get(0);
                Assert.assertEquals(message.getId(), messageToSave.getId());
                Assert.assertEquals(message.getSendDate(), messageToSave.getSendDate());

                checkpoints[0] = true;
            }

            @Override
            public void onFail(@NonNull Error error) {
                Assert.fail("Should not get fail callback");
            }
        });

        for (boolean checkpoint : checkpoints) {
            Assert.assertTrue(checkpoint);
        }
    }

    @Test
    public void testDeleteMessageUpdateCache() throws JSONException {
        final boolean[] checkpoints = new boolean[] { false };
        Conversation conversation = new Conversation(new Record("conversation", "c0"));

        JSONObject messageJson = new JSONObject();
        messageJson.put("_id", "message/m0");
        Date createdAt = new Date(0);
        messageJson.put("_created_at", formatterWithMS.print(new DateTime(createdAt)));

        Record record = Record.fromJson(messageJson);
        Reference conversationRef = new Reference("conversation", "c0");
        record.set("conversation", conversationRef);
        record.set("deleted", true);

        Message deletedMessage = new Message(record);

        this.cacheController.didDeleteMessage(deletedMessage);
        this.cacheController.getMessages(conversation, 100, null, null, new GetCallback<List<Message>>() {
            @Override
            public void onSuccess(@Nullable List<Message> messages) {
                Assert.assertEquals(messages.size(), 4);

                checkpoints[0] = true;
            }

            @Override
            public void onFail(@NonNull Error error) {
                Assert.fail("Should not get fail callback");
            }
        });

        Realm realm = this.cacheController.store.getRealm();
        RealmResults<MessageCacheObject> results = realm.where(MessageCacheObject.class).equalTo("deleted", true).findAll();
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(realm.where(MessageCacheObject.class).findAll().size(), 10);

        Assert.assertTrue(checkpoints[0]);
    }

    @Test
    public void testDeleteNonExistedMessage() throws JSONException {
        final boolean[] checkpoints = new boolean[] { false };
        Conversation conversation = new Conversation(new Record("conversation", "c0"));

        JSONObject messageJson = new JSONObject();
        messageJson.put("_id", "message/m99");
        Date createdAt = new Date(0);
        messageJson.put("_created_at", formatterWithMS.print(new DateTime(createdAt)));

        Record record = Record.fromJson(messageJson);
        Reference conversationRef = new Reference("conversation", "c0");
        record.set("conversation", conversationRef);
        record.set("deleted", true);

        Message deletedMessage = new Message(record);

        this.cacheController.didDeleteMessage(deletedMessage);
        this.cacheController.getMessages(conversation, 100, null, null, new GetCallback<List<Message>>() {
            @Override
            public void onSuccess(@Nullable List<Message> messages) {
                Assert.assertEquals(messages.size(), 5);

                checkpoints[0] = true;
            }

            @Override
            public void onFail(@NonNull Error error) {
                Assert.fail("Should not get fail callback");
            }
        });

        Realm realm = this.cacheController.store.getRealm();
        RealmResults<MessageCacheObject> results = realm.where(MessageCacheObject.class).equalTo("deleted", true).findAll();
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(realm.where(MessageCacheObject.class).findAll().size(), 11);

        Assert.assertTrue(checkpoints[0]);
    }

    //endregion

    //region Message Operation Cache

    @Test
    public void testStartMessageOperation() throws Exception {
        Record record = new Record("message");
        record.set("conversation", new Reference("conversation", "c0"));

        Message message = new Message(record);
        MessageOperation createdOperation = this.cacheController.didStartMessageOperation(message, "c0", MessageOperation.Type.ADD);
        Assert.assertEquals(createdOperation.conversationId, "c0");
        Assert.assertEquals(createdOperation.message.getId(), record.getId());
        Assert.assertEquals(createdOperation.type, MessageOperation.Type.ADD);
        Assert.assertEquals(createdOperation.status, MessageOperation.Status.PENDING);

        Realm realm = this.cacheController.store.getRealm();
        RealmResults<MessageOperationCacheObject> results = realm.where(MessageOperationCacheObject.class).findAll();
        Assert.assertEquals(results.size(), 1);

        MessageOperation fetchedOperation = results.first().toMessageOperation();
        Assert.assertEquals(fetchedOperation.operationId, createdOperation.operationId);
        Assert.assertEquals(fetchedOperation.conversationId, "c0");
        Assert.assertEquals(fetchedOperation.message.getId(), record.getId());
        Assert.assertEquals(fetchedOperation.type, MessageOperation.Type.ADD);
        Assert.assertEquals(fetchedOperation.status, MessageOperation.Status.PENDING);
    }

    @Test
    public void testCompleteMessageOperation() throws Exception {
        Record record = new Record("message");
        record.set("conversation", new Reference("conversation", "c0"));

        Message message = new Message(record);
        MessageOperation createdOperation = this.cacheController.didStartMessageOperation(message, "c0", MessageOperation.Type.ADD);
        this.cacheController.didCompleteMessageOperation(createdOperation);

        Realm realm = this.cacheController.store.getRealm();
        RealmResults<MessageOperationCacheObject> results = realm.where(MessageOperationCacheObject.class).findAll();
        Assert.assertEquals(results.size(), 0);
    }

    @Test
    public void testCancelMessageOperation() throws Exception {
        Record record = new Record("message");
        record.set("conversation", new Reference("conversation", "c0"));

        Message message = new Message(record);
        MessageOperation createdOperation = this.cacheController.didStartMessageOperation(message, "c0", MessageOperation.Type.ADD);
        this.cacheController.didCancelMessageOperation(createdOperation);

        Realm realm = this.cacheController.store.getRealm();
        RealmResults<MessageOperationCacheObject> results = realm.where(MessageOperationCacheObject.class).findAll();
        Assert.assertEquals(results.size(), 0);
    }

    @Test
    public void testFailMessageOperation() throws Exception {
        Record record = new Record("message");
        record.set("conversation", new Reference("conversation", "c0"));

        Message message = new Message(record);
        MessageOperation createdOperation = this.cacheController.didStartMessageOperation(message, "c0", MessageOperation.Type.EDIT);
        this.cacheController.didFailMessageOperation(createdOperation, new Error("error occurred"));

        Realm realm = this.cacheController.store.getRealm();
        RealmResults<MessageOperationCacheObject> results = realm.where(MessageOperationCacheObject.class).findAll();
        Assert.assertEquals(results.size(), 1);

        MessageOperation fetchedOperation = results.first().toMessageOperation();
        Assert.assertEquals(fetchedOperation.operationId, createdOperation.operationId);
        Assert.assertEquals(fetchedOperation.type, MessageOperation.Type.EDIT);
        Assert.assertEquals(fetchedOperation.status, MessageOperation.Status.FAILED);
    }

    //endregion

    //region Subscriptions

    @Test
    public void testHandleSubscription() throws JSONException {
        Realm realm = this.cacheController.store.getRealm();
        RealmResults<MessageCacheObject> allResults = realm.where(MessageCacheObject.class).findAll();
        RealmResults<MessageCacheObject> results = realm.where(MessageCacheObject.class).equalTo("recordID", "mm1").findAll();
        Assert.assertEquals(allResults.size(), 10);
        Assert.assertEquals(results.size(), 0);

        JSONObject messageJson = new JSONObject();
        messageJson.put("_id", "message/mm1");

        Record record = Record.fromJson(messageJson);
        Reference conversationRef = new Reference("conversation", "cc0");
        record.set("conversation", conversationRef);
        record.set("edited_at", new Date(0));
        record.set("body", "new message");

        final Message message = new Message(record);
        message.sendDate = new Date(50000);

        this.cacheController.handleMessageChange(message, EVENT_TYPE_CREATE);
        Assert.assertEquals(allResults.size(), 11);
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.get(0).editionDate, new Date(0));
        Assert.assertEquals(results.get(0).deleted, false);

        message.record.set("edited_at", new Date(1000));
        this.cacheController.handleMessageChange(message, EVENT_TYPE_UPDATE);
        Assert.assertEquals(allResults.size(), 11);
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.get(0).editionDate, new Date(1000));
        Assert.assertEquals(results.get(0).deleted, false);

        message.record.set("deleted", true);
        this.cacheController.handleMessageChange(message, EVENT_TYPE_DELETE);
        Assert.assertEquals(allResults.size(), 11);
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.get(0).editionDate, new Date(1000));
        Assert.assertEquals(results.get(0).deleted, true);
    }

    @Test
    public void handleSubscriptionForNonExistedMesssage() throws JSONException {
        Realm realm = this.cacheController.store.getRealm();
        RealmResults<MessageCacheObject> allResults = realm.where(MessageCacheObject.class).findAll();
        RealmResults<MessageCacheObject> results = realm.where(MessageCacheObject.class).equalTo("recordID", "mm1").findAll();
        Assert.assertEquals(allResults.size(), 10);
        Assert.assertEquals(results.size(), 0);

        JSONObject messageJson = new JSONObject();
        messageJson.put("_id", "message/mm1");

        Record record = Record.fromJson(messageJson);
        Reference conversationRef = new Reference("conversation", "cc0");
        record.set("conversation", conversationRef);
        record.set("edited_at", new Date(0));
        record.set("body", "new message");

        final Message message = new Message(record);
        message.sendDate = new Date(50000);

        this.cacheController.handleMessageChange(message, EVENT_TYPE_UPDATE);
        Assert.assertEquals(allResults.size(), 11);
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.get(0).editionDate, new Date(0));
        Assert.assertEquals(results.get(0).deleted, false);
    }

    //endregion
}
