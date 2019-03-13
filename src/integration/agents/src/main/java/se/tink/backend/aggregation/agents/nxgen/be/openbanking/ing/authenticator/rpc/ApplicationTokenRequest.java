package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class ApplicationTokenRequest {

    public String toData() {
        return Form.builder()
            .put(IngConstants.FormKeys.GRANT_TYPE, IngConstants.FormValues.CLIENT_CREDENTIALS)
            .build()
            .serialize();
    }
}
