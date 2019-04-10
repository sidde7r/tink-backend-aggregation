package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgreementEntity {
    private String agreementId;

    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    public String getAgreementId() {
        return agreementId;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }
}
