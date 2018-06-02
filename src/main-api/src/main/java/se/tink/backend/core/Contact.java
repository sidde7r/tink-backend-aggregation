package se.tink.backend.core;

public class Contact {
    private String externalId;
    private String id;
    private String name;
    private ContactTypes type;
    private String userId;

    public String getExternalId() {
        return externalId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ContactTypes getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(ContactTypes type) {
        this.type = type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
