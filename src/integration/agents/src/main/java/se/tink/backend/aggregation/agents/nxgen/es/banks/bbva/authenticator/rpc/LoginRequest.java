package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.PostParameter;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class LoginRequest extends AbstractForm {
    public LoginRequest(String origin, String type, String username, String password) {
        this.put(PostParameter.ORIGEN_KEY, origin);
        this.put(PostParameter.EAI_TIPOCP_KEY, type);
        this.put(PostParameter.EAI_USER_KEY, PostParameter.EAI_USER_VALUE_PREFIX);
        this.put(PostParameter.EAI_PASSWORD_KEY, password);
    }

    public LoginRequest(String username, String password) {
        this.put(PostParameter.ORIGEN_KEY, PostParameter.ORIGEN_VALUE);
        this.put(PostParameter.EAI_TIPOCP_KEY, PostParameter.EAI_TIPOCP_VALUE);
        this.put(PostParameter.EAI_USER_KEY, PostParameter.EAI_USER_VALUE_PREFIX + username);
        this.put(PostParameter.EAI_PASSWORD_KEY, password);
    }
}
