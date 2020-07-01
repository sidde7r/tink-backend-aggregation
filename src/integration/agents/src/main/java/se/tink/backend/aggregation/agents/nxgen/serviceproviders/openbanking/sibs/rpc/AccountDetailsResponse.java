package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountDetailsResponse {

    private AccountEntity account;
}
