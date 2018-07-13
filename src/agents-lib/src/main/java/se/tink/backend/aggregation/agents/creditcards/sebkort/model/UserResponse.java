package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse extends BaseResponse {
    protected UserEntity body;

    public UserEntity getBody() {
        return body;
    }

    public void setBody(UserEntity body) {
        this.body = body;
    }
}
