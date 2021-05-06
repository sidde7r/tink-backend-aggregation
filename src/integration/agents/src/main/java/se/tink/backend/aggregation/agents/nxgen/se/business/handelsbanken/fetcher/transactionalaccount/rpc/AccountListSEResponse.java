package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.entities.HandelsbankenSEAccount;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@JsonObject
public class AccountListSEResponse extends AccountListResponse {
    @JsonProperty("data")
    private List<AccountEntity> accountList;

    // not used by business, used by banks
    @Override
    public Stream<TransactionalAccount> toTinkAccounts(HandelsbankenApiClient client) {
        return Stream.empty();
    }

    @Override
    public Stream<TransactionalAccount> toTinkAccounts(
            HandelsbankenApiClient client, PersistentStorage persistentStorage) {
        return accountList == null
                ? Stream.empty()
                : accountList.stream()
                        .map(a -> getTransactionalAccount(client, a, persistentStorage))
                        .filter(Optional::isPresent)
                        .map(Optional::get);
    }

    private Optional<TransactionalAccount> getTransactionalAccount(
            HandelsbankenApiClient client,
            HandelsbankenSEAccount handelsbankenAccount,
            PersistentStorage persistentStorage) {
        final TransactionsSEResponse transactionsResponse =
                (TransactionsSEResponse) client.transactions(handelsbankenAccount);

        return handelsbankenAccount.toTransactionalAccount(
                client, transactionsResponse, persistentStorage);
    }

    @Override
    public Stream<CreditCardAccount> toTinkCreditCard(HandelsbankenApiClient client) {
        return Stream.empty();
    }

    @Override
    public Optional<? extends HandelsbankenAccount> find(Account account) {
        return accountList == null
                ? Optional.empty()
                : accountList.stream().filter(a -> a.find(account)).findFirst();
    }
}
