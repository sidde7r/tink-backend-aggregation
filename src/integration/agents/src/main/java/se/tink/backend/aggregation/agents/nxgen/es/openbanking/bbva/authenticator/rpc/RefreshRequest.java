package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshRequest {

    private String refreshToken;

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String toForm() {
        return Form.builder().put(FormKeys.REFRESH_TOKEN, refreshToken).build().serialize();
    }
}
