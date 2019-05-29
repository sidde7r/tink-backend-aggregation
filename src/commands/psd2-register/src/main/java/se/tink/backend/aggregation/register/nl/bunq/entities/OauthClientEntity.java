package se.tink.backend.aggregation.register.nl.bunq.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OauthClientEntity {
    private int id;
    private String created;
    private String updated;
    private String status;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("secret")
    private String clientSecret;

    @JsonProperty("callback_url")
    private List<CallbackUrlEntity> callbackUrl;

    public int getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
