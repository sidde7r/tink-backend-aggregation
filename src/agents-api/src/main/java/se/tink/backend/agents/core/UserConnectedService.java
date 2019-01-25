package se.tink.backend.agents.core;

import io.protostuff.Tag;

public class UserConnectedService {
    @Tag(1)
    private String externalId;
    @Tag(2)
    private UserConnectedServiceStates state;
    @Tag(3)
    private UserConnectedServiceTypes type;

    public UserConnectedService() {

    }

    public UserConnectedService(UserConnectedServiceTypes type, UserConnectedServiceStates state, String externalId) {
        this.type = type;
        this.state = state;
        this.externalId = externalId;
    }

    public String getExternalId() {
        return externalId;
    }

    public UserConnectedServiceStates getState() {
        return state;
    }

    public UserConnectedServiceTypes getType() {
        return type;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setState(UserConnectedServiceStates state) {
        this.state = state;
    }

    public void setType(UserConnectedServiceTypes type) {
        this.type = type;
    }
}
