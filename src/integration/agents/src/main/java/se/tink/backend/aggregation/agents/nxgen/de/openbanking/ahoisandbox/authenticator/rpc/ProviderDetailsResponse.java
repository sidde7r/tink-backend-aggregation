package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.entity.AccessDescriptionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProviderDetailsResponse {

    private String type;
    private String id;
    private String name;
    private String location;
    private AccessDescriptionEntity accessDescription;
    private Boolean supported;
    private String bankCode;
    private String bic;

    public String getId() {
        return id;
    }
}
