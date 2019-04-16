package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class AuthorizationResponseEntity {

    private List<LinkEntity> links;

    @JsonProperty("order_ref")
    private String orderRef;

    private String status;

    @JsonProperty("tpp_token")
    private String tppToken;

    public String getOrderRef() {
        return orderRef;
    }

    public String getTppToken() {
        return tppToken;
    }
}
