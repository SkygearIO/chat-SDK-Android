package io.skygear.plugins.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

public class Typing {
    private final String userId;
    private final State state;
    private final Date time;

    public Typing(@NonNull final String userId, @NonNull final State state, @Nullable final Date time) {
        super();

        this.userId = userId;
        this.state = state;

        if (time == null) {
            this.time = new Date();
        } else {
            this.time = time;
        }
    }

    public String getUserId() {
        return userId;
    }

    public State getState() {
        return state;
    }

    public Date getTime() {
        return time;
    }

    public static enum State {
        BEGIN("begin"), PAUSE("pause"), FINISHED("finished");

        private final String name;

        State(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        static State fromName(String name) {
            State[] states = State.values();
            for (State eachState : states) {
                if (eachState.getName().equals(name)) {
                    return eachState;
                }
            }

            throw new IllegalArgumentException(String.format("Unknown typing state: %s", name));
        }
    }
}
