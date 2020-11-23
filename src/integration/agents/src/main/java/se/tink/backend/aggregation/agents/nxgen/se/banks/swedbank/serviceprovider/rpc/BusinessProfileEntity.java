package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BusinessProfileEntity extends ProfileEntity {
    private String activeProfileName;

    @Override
    public String getHolderName() {
        return activeProfileName;
    }
}
