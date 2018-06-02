package se.tink.backend.core;

import java.util.List;
import java.util.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Preconditions;
import se.tink.libraries.uuid.UUIDUtils;

public class Client {
    private boolean allowed;
    private String description;
    private String id;
    private List<ClientMessage> messages;
    private SessionTypes sessionType;

    public Client() {

    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public List<ClientMessage> getMessages() {
        return messages;
    }

    public Optional<String> getMessage(String locale) {
        if (messages == null) {
            return Optional.empty();
        }

        if (locale == null) {
            return Optional.empty();
        }

        for (ClientMessage cm : messages) {
            if (locale.equals(cm.getLocale())) {
                return Optional.ofNullable(cm.getMessage());
            }
        }
        return Optional.empty();
    }

    public void validate() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "Client must have an ID.");
        Preconditions.checkArgument(UUIDUtils.isValidTinkUUID(id), "Non valid UUID format of ID.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(description), "Client must have a description.");
        Preconditions.checkNotNull(sessionType, "Client must have a session type.");
        if (allowed) {
            Preconditions.checkArgument(messages == null);
        } else {
            Preconditions.checkNotNull(messages);
        }
    }

    public void setMessages(List<ClientMessage> messages) {
        this.messages = messages;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSessionType(SessionTypes sessionType) {
        this.sessionType = sessionType;
    }

    public SessionTypes getSessionType() {
        return sessionType;
    }
}