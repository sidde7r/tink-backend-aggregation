package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionTypeDetailsEntity {

    @JsonProperty("remote_name")
    private String remoteName;
    @JsonProperty("remote_iban")
    private String remoteIban;
    @JsonProperty("remote_bic")
    private String remoteBic;
    @JsonProperty("remote_eref")
    private String remoteEref;

    public String getRemoteName() {
        return remoteName;
    }

    public String getRemoteIban() {
        return remoteIban;
    }

    public String getRemoteBic() {
        return remoteBic;
    }

    public String getRemoteEref() {
        return remoteEref;
    }
}
