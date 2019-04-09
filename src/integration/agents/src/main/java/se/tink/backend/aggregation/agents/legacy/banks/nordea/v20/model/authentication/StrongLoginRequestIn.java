package se.tink.backend.aggregation.agents.banks.nordea.v20.model.authentication;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapSerializer;

public class StrongLoginRequestIn {

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String type;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String password;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String userId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
