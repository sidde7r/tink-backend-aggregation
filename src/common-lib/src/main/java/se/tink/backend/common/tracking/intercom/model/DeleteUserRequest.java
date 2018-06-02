package se.tink.backend.common.tracking.intercom.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeleteUserRequest {
    @JsonProperty("intercom_user_id")
    private String intercomUserId;

    public String getIntercomUserId() {
        return intercomUserId;
    }

    public void setIntercomUserId(String intercomUserId) {
        this.intercomUserId = intercomUserId;
    }
}
