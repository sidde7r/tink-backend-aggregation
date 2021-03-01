package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private String scaRedirect;

    @JsonIgnore
    public String getScaRedirectLink() {
        return scaRedirect;
    }
}
