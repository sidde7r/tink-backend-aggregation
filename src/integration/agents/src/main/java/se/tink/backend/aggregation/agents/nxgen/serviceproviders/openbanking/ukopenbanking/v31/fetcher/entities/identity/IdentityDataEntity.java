package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentityDataEntity {
    @JsonProperty("PartyId")
    private String partyId;

    @JsonProperty("PartyNumber")
    private String partyNumber;

    @JsonProperty("PartyType")
    private UkOpenBankingApiDefinitions.PartyType partyType;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("FullLegalName")
    private String fullLegalName;

    @JsonProperty("EmailAddress")
    private String emailAddress;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("Mobile")
    private String mobile;

    public String getName() {
        return Objects.nonNull(fullLegalName) ? fullLegalName : name;
    }
}
