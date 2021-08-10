package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.AccountEntity;

@Getter
public class AccountDetailsResponse {

    private AccountEntity account;
}
