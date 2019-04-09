package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities.LinkListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitAuthResponseEntity {
    @JsonProperty("order_ref")
    private String orderRef;

    private String status;

    @JsonProperty("tpp_token")
    private String tppToken;

    private LinkListEntity links;

    public String getOrderRef() {
        return orderRef;
    }

    public String getStatus() {
        return status;
    }

    public String getTppToken() {
        return tppToken;
    }

    @JsonIgnore
    public Optional<LinkEntity> findLinkByName(String name) {
        return links.findLinkByName(name);
    }
}
