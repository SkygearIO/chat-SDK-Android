package io.skygear.plugins.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 * The Typing model for Chat Plugin.
 */
public class Typing {
    private final String userId;
    private final State state;
    private final Date time;

    /**
     * Instantiates a new Typing object.
     *
     * @param userId the user id
     * @param state  the state
     * @param time   the time
     */
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

    /**
     * Gets user id.
     *
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * Gets time.
     *
     * @return the time
     */
    public Date getTime() {
        return time;
    }

    /**
     * The Typing State.
     */
    public enum State {
        BEGIN("begin"),
        PAUSE("pause"),
        FINISHED("finished");

        private final String name;

        State(String name) {
            this.name = name;
        }

        /**
         * Gets name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Create a state from a name
         *
         * @param name the name
         * @return the state
         */
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
