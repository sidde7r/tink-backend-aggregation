package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.InvalidateReasonEntity;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class InvalidateTokenRequest extends AbstractForm {
    public InvalidateTokenRequest withAccessToken(String accessToken) {
        this.put("access_token", accessToken);
        return this;
    }

    public InvalidateTokenRequest withInvalidateReason(InvalidateReasonEntity invalidateReason) {
        this.put("invalidate_reason", invalidateReason.toString());
        return this;
    }
}
