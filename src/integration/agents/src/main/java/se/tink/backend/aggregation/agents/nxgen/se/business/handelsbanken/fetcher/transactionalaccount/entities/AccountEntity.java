package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.entities.HandelsbankenSEAccount;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountEntity extends HandelsbankenSEAccount {
    public Optional<TransactionalAccount> toTinkAccount(HandelsbankenApiClient client) {
        return getTransactionalAccount(client);
    }

    private Optional<TransactionalAccount> getTransactionalAccount(HandelsbankenApiClient client) {
        final TransactionsSEResponse transactionsResponse =
                (TransactionsSEResponse) client.transactions(this);

        return toTransactionalAccount(client, transactionsResponse, null);
    }

    public boolean find(Account account) {
        return is(account);
    }
}
