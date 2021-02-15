package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountIdEntityOther {

    private String identification;
    private String schemeName;

    public String getIdentification() {
        return identification;
    }

    public String getSchemeName() {
        return schemeName;
    }
}
