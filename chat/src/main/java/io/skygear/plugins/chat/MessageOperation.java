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


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

import io.skygear.skygear.Asset;
import io.skygear.skygear.Record;
import io.skygear.skygear.Reference;
import io.skygear.skygear.Error;

/**
 * The Message Operation model for Chat Plugin.
 */
public class MessageOperation {
    final Message message;

    /**
     * The operation identifier.
     */
    String operationId;

    /**
     * The conversation identifier.
     */
    String conversationId;

    /**
     * The message operation type.
     */
    Type type;

    /**
     * The message operation status.
     */
    Status status;

    /**
     * The error related to this operation if the operation has failed.
     */
    Error error;

    /**
     * Transient fields that are not saved to the server
     */
    Date sendDate;

    /**
     * Instantiates a new Message Operation.
     */
    public MessageOperation(@NonNull String operationId,
                            @NonNull Message message,
                            @NonNull String conversationId,
                            @NonNull Type type,
                            @NonNull Status status,
                            @NonNull Date sendDate,
                            @Nullable Error error) {
        this.operationId = operationId;
        this.message = message;
        this.conversationId = conversationId;
        this.type = type;
        this.status = status;
        this.sendDate = sendDate;
        this.error = error;
    }

    /**
     * Instantiates a new Message Operation for a pending message.
     */
    public MessageOperation(@NonNull Message message,
                            @NonNull String conversationId,
                            @NonNull Type type) {
        this.operationId = UUID.randomUUID().toString();
        this.message = message;
        this.conversationId = conversationId;
        this.type = type;
        this.status = Status.PENDING;
        this.sendDate = new Date();
        this.error = null;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    @NonNull
    public String getId() {
        return this.operationId;
    }

    /**
     * Gets conversation id.
     *
     * @return the conversation id
     */
    @NonNull
    public String getConversationId() {
        return this.conversationId;
    }


    /**
     * Gets status.
     *
     * @return the status
     */
    @Nullable
    public Status getStatus() {
        return this.status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    @Nullable
    protected void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    @Nullable
    public Type getType() {
        return this.type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    @Nullable
    protected void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets message send date.
     *
     * @return message send date
     */
    public Date getSendDate() {
        return this.sendDate;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public Message getMessage() {
        return this.message;
    }

    /**
     * Gets error.
     *
     * @return the error
     */
    public Error getError() {
        return this.error;
    }

    /**
     * The Message Operation Status.
     */
    public enum Status {
        PENDING("pending"),
        FAILED("failed"),
        SUCCESS("success");

        private final String name;

        /**
         * Gets name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        Status(String name) {
            this.name = name;
        }

        /**
         * Creates a status from a name
         *
         * @param name the name
         * @return the status
         */
        @Nullable
        static Status fromName(String name) {
            Status status = null;
            for (Status eachStatus : Status.values()) {
                if (eachStatus.getName().equals(name)) {
                    status = eachStatus;
                    break;
                }
            }
            return status;
        }
    }

    /**
     * The Message Operation Type.
     */
    public enum Type {
        ADD("add"),
        EDIT("edit"),
        DELETE("delete");

        private final String name;

        /**
         * Gets name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        Type(String name) {
            this.name = name;
        }

        /**
         * Creates a type from a name
         *
         * @param name the name
         * @return the type
         */
        @Nullable
        static Type fromName(String name) {
            Type type = null;
            for (Type eachType : Type.values()) {
                if (eachType.getName().equals(name)) {
                    type = eachType;
                    break;
                }
            }
            return type;
        }
    }
}
