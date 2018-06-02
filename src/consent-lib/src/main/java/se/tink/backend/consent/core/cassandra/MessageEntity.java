package se.tink.backend.consent.core.cassandra;

import java.util.List;

public class MessageEntity {
    private String message;
    private List<LinkEntity> links;

    public MessageEntity() {
        
    }

    public MessageEntity(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<LinkEntity> getLinks() {
        return links;
    }

    public void setLinks(List<LinkEntity> links) {
        this.links = links;
    }
}
