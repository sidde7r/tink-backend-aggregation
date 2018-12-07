package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc;

import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.AuthMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.AuthResponseTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.AuthScopeEntity;

public class PendingAuthCodeRequest extends StringKeyStringValueIgnoreCaseMultivaluedMap {
    public PendingAuthCodeRequest withClientId(String clientId) {
        this.putSingle("client_id", clientId);
        return this;
    }

    public PendingAuthCodeRequest withResponseType(AuthResponseTypeEntity responseType) {
        this.putSingle("response_type", responseType.toString());
        return this;
    }

    public PendingAuthCodeRequest withRedirectUri(String redirectUri) {
        this.putSingle("redirect_uri", redirectUri);
        return this;
    }

    public PendingAuthCodeRequest withScope(AuthScopeEntity scope) {
        this.putSingle("scope", scope.toString());
        return this;
    }

    public PendingAuthCodeRequest withState(String state) {
        this.putSingle("state", state);
        return this;
    }

    public PendingAuthCodeRequest withAuthMethod(AuthMethodEntity authMethod) {
        this.putSingle("auth_method", authMethod.toString());
        return this;
    }

    public PendingAuthCodeRequest withUserId(String userId) {
        this.putSingle("user_id", userId);
        return this;
    }
}
