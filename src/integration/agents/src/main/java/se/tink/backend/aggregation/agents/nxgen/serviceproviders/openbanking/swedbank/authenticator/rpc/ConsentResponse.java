package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ConsentResponse {

    private String consentId;

    @JsonProperty("_links")
    private LinksEntity links;

    private String consentStatus;

    private String statementStatus;

    public boolean isValidConsent() {
        return ConsentStatus.VALID.equalsIgnoreCase(consentStatus);
    }
}
