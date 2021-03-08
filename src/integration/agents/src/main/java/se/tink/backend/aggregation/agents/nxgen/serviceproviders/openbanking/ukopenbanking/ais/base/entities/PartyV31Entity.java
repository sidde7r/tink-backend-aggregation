package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class PartyV31Entity {
    private String partyId;

    private String partyNumber;

    private UkOpenBankingApiDefinitions.PartyType partyType;

    private String name;

    private String fullLegalName;

    private String emailAddress;

    private String phone;

    private String mobile;

    public String getName() {
        return Objects.nonNull(fullLegalName) ? fullLegalName : name;
    }
}
