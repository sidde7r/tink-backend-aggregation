package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.entity;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity.ScaRedirect;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UnicreditConsentLinksEntity {

    private ScaRedirect authoriseTransaction;

    public ScaRedirect getScaRedirect() {

        return Optional.ofNullable(authoriseTransaction)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_SCA_URL));
    }
}
