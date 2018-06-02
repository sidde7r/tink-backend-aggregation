package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AgreementEntity {
    private String agreementId;
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    public String getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public void setLinks(
            HashMap<String, LinkEntity> links) {
        this.links = links;
    }
}
