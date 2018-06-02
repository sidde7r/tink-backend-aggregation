package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.entities.CredentialsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FirstLoginRequest {
    private CredentialsEntity credentials;
    private List<String> tokenProtocolVersions;

    private FirstLoginRequest(CredentialsEntity credentials, List<String> tokenProtocolVersions) {
        this.credentials = credentials;
        this.tokenProtocolVersions = tokenProtocolVersions;
    }

    public static FirstLoginRequest build(String evryToken) {
        CredentialsEntity credentialsEntity = CredentialsEntity.build(evryToken);
        return new FirstLoginRequest(credentialsEntity, Collections.singletonList("ATP-1.0"));
    }
}
