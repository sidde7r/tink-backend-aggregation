package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String consentId;

    @JsonProperty("_links")
    private LinksEntity links;

    private String consentStatus;

    private String statementStatus;

    private List<ScaMethodEntity> scaMethods;

    private ScaMethodEntity chosenScaMethod;

    public String getConsentId() {
        return consentId;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public String getConsentStatus() {
        return consentStatus;
    }

    public String getStatementStatus() {
        return statementStatus;
    }
}
