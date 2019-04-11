package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.Permissions;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.entities.DataResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.MetaEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountSetupResponse {
    @JsonProperty("Data")
    private DataResponseEntity data;

    @JsonProperty("Risk")
    private Object Risk;

    @JsonProperty("Links")
    private LinksEntity links;

    @JsonProperty("Meta")
    private MetaEntity meta;

    public DataResponseEntity getData() {
        return data;
    }

    public Object getRisk() {
        return Risk;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public MetaEntity getMeta() {
        return meta;
    }

    // Verifying we get all permissions from the user
    // The documentation does not specify what permissions are required for the
    // endpoints
    public boolean receivedAllReadPermissions() {
        return getData().getPermissions().equals(Permissions.ALL_READ_PERMISSIONS);
    }
}
