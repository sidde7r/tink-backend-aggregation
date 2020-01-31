package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc;

import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class RefreshRequest extends AbstractForm {

    public RefreshRequest(String clientId, String refreshToken) {
        put("client_id", clientId);
        put("grant_type", "refresh_token");
        put("refresh_token", refreshToken);
    }
}
