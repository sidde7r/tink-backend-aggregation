package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class FetchAccountDetailsResponse {
    @JsonProperty("getAccountDetailsOut")
    private AccountDetailsEntity accountDetails;

    public Optional<TransactionalAccount> toTinkAccount(AccountEntity account) {
        return accountDetails.toTinkAccount(account);
    }
}
