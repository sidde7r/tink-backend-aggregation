package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.AccountsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CallingUserEntity extends AccountsResponse {
    private String firstName;
    private String lastName;
    private String personalIdentityNumber;
}
