package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UnicreditConsentResponse implements ConsentResponse {

    private String consentId;
    private String consentStatus;

    @JsonProperty("_links")
    @Getter
    private LinksEntity links;

    @Override
    public String getConsentId() {
        return Preconditions.checkNotNull(Strings.emptyToNull(consentId));
    }

    @Override
    public String getScaRedirect() {
        throw new UnsupportedOperationException();
    }
}
