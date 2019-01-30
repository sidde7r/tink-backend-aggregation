package se.tink.backend.aggregation.agents.creditcards.supremecard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
    private UserEntity data;
    private boolean success;

    public UserEntity getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setData(UserEntity data) {
        this.data = data;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
