package io.skygear.plugins.chat.ui;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.skygear.chatkit.commons.ImageLoader;
import io.skygear.chatkit.commons.models.IMessage;
import io.skygear.chatkit.messages.MessageHolders;
import io.skygear.chatkit.messages.MessagesListAdapter;
import io.skygear.chatkit.utils.DateFormatter;

public class SortedMessageListAdapter<MESSAGE extends IMessage> extends MessagesListAdapter<MESSAGE> {
    public SortedMessageListAdapter(String senderId, MessageHolders holders, ImageLoader imageLoader) {
        super(senderId, holders, imageLoader);
    }

    /**
     * Adds message to the list and sort the list by chronological order.
     *
     * @param messages messages to add.
     */
    public void merge(List<MESSAGE> messages) {
        this.merge(messages, new Comparator<MESSAGE>() {
            @Override
            public int compare(MESSAGE m1, MESSAGE m2) {
                return m2.getCreatedAt().compareTo(m1.getCreatedAt());
            }
        });
    }

    /**
     * Adds message to the list and sort the list with the comparator.
     *
     * @param messages messages to add.
     * @param comparator comparator for sorting the item list
     */
    public void merge(List<MESSAGE> messages, final Comparator<MESSAGE> comparator) {
        for (MESSAGE message : messages) {
            this.items.add(new Wrapper<>(message));
        }

        removeDateHeaders();
        Collections.sort(this.items, new Comparator<Wrapper>() {
            @Override
            public int compare(Wrapper o1, Wrapper o2) {
                // Assume all date headers are removed
                MESSAGE m1 = (MESSAGE) o1.item;
                MESSAGE m2 = (MESSAGE) o2.item;
                return comparator.compare(m1, m2);
            }
        });
        regenerateDateHeaders();
        notifyDataSetChanged();
    }

    private void removeDateHeaders() {
        final Iterator<Wrapper> each = this.items.iterator();
        while (each.hasNext()) {
            Wrapper next = each.next();
            if (next.item instanceof Date) {
                each.remove();
            }
        }
    }

    private void regenerateDateHeaders() {
        // Assume all date headers are removed
        List<Integer> indeicesToInsert = new ArrayList<>();
        SparseArray<Date> indeicesOfDate = new SparseArray<>();
        for (int i = 0; i < this.items.size(); i++) {
            MESSAGE message = (MESSAGE) this.items.get(i).item;
            if (this.items.size() > i + 1) {
                MESSAGE nextMessage = (MESSAGE) this.items.get(i + 1).item;
                if (!DateFormatter.isSameDay(message.getCreatedAt(), nextMessage.getCreatedAt())) {
                    indeicesToInsert.add(i + 1);
                    indeicesOfDate.put(i + 1, message.getCreatedAt());
                }
            } else {
                indeicesToInsert.add(i + 1);
                indeicesOfDate.put(i + 1, message.getCreatedAt());
            }
        }

        for (int i = indeicesToInsert.size() - 1; i >= 0; i--) {
            Integer index = indeicesToInsert.get(i);
            Date date = indeicesOfDate.get(index);
            if (date == null) {
                continue;
            }

            this.items.add(index, new Wrapper<>(date));
        }
    }
}
