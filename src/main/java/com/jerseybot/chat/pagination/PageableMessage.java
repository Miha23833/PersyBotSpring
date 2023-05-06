package com.jerseybot.chat.pagination;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.commons.collections4.list.CursorableLinkedList;

public class PageableMessage {
    private final CursorableLinkedList<MessageCreateData> pages;
    private final CursorableLinkedList.Cursor<MessageCreateData> cursor;

    private final Long messageId;

    private MessageCreateData current;

    private PageableMessage(Long messageId, CursorableLinkedList<MessageCreateData> data) {
        this.messageId = messageId;
        this.pages = data;

        this.cursor = this.pages.cursor(0);
        this.current = this.cursor.next();
    }

    public boolean hasNext() {
        return this.cursor.hasNext();
    }

    public MessageCreateData next() {
        this.current = this.cursor.next();
        return this.current;
    }

    public boolean hasPrev() {
        return this.cursor.hasPrevious();
    }

    public MessageCreateData prev() {
        current = this.cursor.previous();
        return this.current;
    }

    public MessageCreateData getCurrent() {
        return this.current;
    }

    public Long getMessageId() {
        return messageId;
    }

    private CursorableLinkedList<MessageCreateData> getPages() {
        return this.pages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final CursorableLinkedList<MessageCreateData> messages;

        private Builder() {
            this.messages = new CursorableLinkedList<>();
        }

        public Builder addMessage(MessageCreateData MessageCreateData) {
            this.messages.add(MessageCreateData);
            return this;
        }

        public MessageCreateData get(int index) {
            if (index >= messages.size()) {
                throw new IndexOutOfBoundsException(String.format("Size is %s, but index was: %s", messages.size(), index));
            }
            return messages.get(index);
        }

        public int size() {
            return this.messages.size();
        }

        public PageableMessage build(Long messageId) {
            return new PageableMessage(messageId, messages);
        }
    }

}
