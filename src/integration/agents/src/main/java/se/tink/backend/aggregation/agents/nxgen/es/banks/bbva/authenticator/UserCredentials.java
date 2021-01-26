package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator;

import lombok.Getter;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils.BbvaUtils;

@Getter
public class UserCredentials {
    private String username;
    private String password;

    public UserCredentials(Credentials credentials) {
        this.username = BbvaUtils.formatUsername(credentials.getField(CredentialKeys.USERNAME));
        this.password = credentials.getField(CredentialKeys.PASSWORD);
    }
}
