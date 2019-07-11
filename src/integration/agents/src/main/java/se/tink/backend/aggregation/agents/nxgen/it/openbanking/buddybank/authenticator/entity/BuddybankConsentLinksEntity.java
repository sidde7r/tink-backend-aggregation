package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.entity;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity.ScaRedirect;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BuddybankConsentLinksEntity {

    private ScaRedirect status;

    public ScaRedirect getScaRedirect() {

        return Optional.ofNullable(status)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_SCA_URL));
    }
}
