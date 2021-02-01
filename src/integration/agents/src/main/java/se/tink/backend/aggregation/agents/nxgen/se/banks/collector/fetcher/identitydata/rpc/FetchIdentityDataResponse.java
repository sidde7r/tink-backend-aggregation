package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.identitydata.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

@JsonObject
@Getter
public class FetchIdentityDataResponse {
    private boolean isCustomer;
    private String lastName;
    private String givenName;

    public IdentityData toTinkIdentityData(String ssn) {
        return SeIdentityData.of(givenName, lastName, ssn);
    }
}
