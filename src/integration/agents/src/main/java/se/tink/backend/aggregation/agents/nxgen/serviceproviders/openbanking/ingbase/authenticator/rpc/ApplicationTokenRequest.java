package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class ApplicationTokenRequest {

    public String toData() {
        return Form.builder()
                .put(
                        IngBaseConstants.FormKeys.GRANT_TYPE,
                        IngBaseConstants.FormValues.CLIENT_CREDENTIALS)
                .build()
                .serialize();
    }
}
