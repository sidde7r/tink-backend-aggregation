package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity;

import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@EqualsAndHashCode(callSuper = true)
@JsonObject
public class CbiConsentStatus extends ConsentStatus {

    // CBI offers more possible statuses for consent than your typical berlingroup implementation
    private static final String REPLACED = "replaced";
    private static final String INVALIDATED = "invalidated";
    private static final String PENDING_EXPIRED = "pendingExpired";

    public CbiConsentStatus(String value) {
        super(value);
    }

    public boolean isReplaced() {
        return is(REPLACED);
    }

    public boolean isInvalidated() {
        return is(INVALIDATED);
    }

    public boolean isPendingExpired() {
        return is(PENDING_EXPIRED);
    }

    @Override
    public boolean isFinal() {
        return super.isFinal() || isReplaced() || isInvalidated() || isPendingExpired();
    }
}
