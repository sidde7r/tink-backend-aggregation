package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.Utils;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterRequest {

    private final List<String> redirect_uris;
    private final String token_endpoint_auth_method;
    private final List<String> grant_types;
    private final String client_name;
    private final List<String> contacts;
    private final String provider_legal_id;
    private final String context;
    private final String scope;

    public RegisterRequest(
            List<String> redirect_uris,
            String token_endpoint_auth_method,
            List<String> grant_types,
            String client_name,
            List<String> contacts,
            String provider_legal_id,
            String context,
            String scopes) {
        this.redirect_uris = redirect_uris;
        this.token_endpoint_auth_method = token_endpoint_auth_method;
        this.grant_types = grant_types;
        this.client_name = client_name;
        this.contacts = contacts;
        this.provider_legal_id = provider_legal_id;
        this.context = context;
        this.scope = scopes;
    }
}
