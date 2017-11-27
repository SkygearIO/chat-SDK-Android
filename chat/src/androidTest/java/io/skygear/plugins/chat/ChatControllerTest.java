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
import io.skygear.skygear.Error;
import io.skygear.skygear.Record;
import io.skygear.skygear.Reference;

@RunWith(AndroidJUnit4.class)
public class ChatControllerTest {
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
        RealmStore store = new RealmStore("test", true);
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
            public void onSucc(@Nullable List<Message> messages) {
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

        Assert.assertTrue(checkpoints[0]);
    }

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
            public void onSucc(@Nullable List<Message> messages) {
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

        Assert.assertTrue(checkpoints[0]);
    }
}
