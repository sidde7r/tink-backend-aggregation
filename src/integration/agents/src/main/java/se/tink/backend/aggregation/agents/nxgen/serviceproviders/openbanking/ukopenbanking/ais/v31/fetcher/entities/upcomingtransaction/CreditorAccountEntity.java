package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.upcomingtransaction;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@JsonObject
public class CreditorAccountEntity {
    private String schemeName;

    private String identification;

    private String name;
}
