package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProviderEntity {

    private String type;
    private String id;
    private String name;
    private String location;
    private Object accessDescription;
    private Boolean supported;
    private String bankCode;
    private String bic;

    public String getId() {
        return id;
    }
}
