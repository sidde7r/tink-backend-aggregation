package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities;

import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.AccountsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CallingUserEntity extends AccountsResponse {
    private String firstName;
    private String lastName;
    private String personalIdentityNumber;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPersonalIdentityNumber() {
        return personalIdentityNumber;
    }
}
