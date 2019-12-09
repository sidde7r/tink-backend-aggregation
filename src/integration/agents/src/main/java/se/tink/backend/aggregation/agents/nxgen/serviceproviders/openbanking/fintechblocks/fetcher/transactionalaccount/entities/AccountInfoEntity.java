package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInfoEntity {

    @JsonProperty("SchemeName")
    private String schemeName;

    @JsonProperty("Identification")
    private String identification;

    @JsonProperty("Name")
    private String name;

    public String getIdentification() {
        return identification;
    }

    public boolean isIban() {
        return schemeName.equalsIgnoreCase("IBAN");
    }

    public boolean isBban() {
        return schemeName.equalsIgnoreCase("BBAN");
    }
}
