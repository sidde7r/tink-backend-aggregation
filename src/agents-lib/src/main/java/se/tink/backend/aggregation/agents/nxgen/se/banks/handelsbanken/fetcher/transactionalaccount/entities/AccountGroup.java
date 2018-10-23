package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.entities;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.HandelsbankenSEAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class AccountGroup {

    private List<HandelsbankenSEAccount> accounts;

    public Stream<TransactionalAccount> toAccounts(HandelsbankenApiClient client) {
        return accounts.stream()
                .map(handelsbankenAccount -> {
                    TransactionsSEResponse transactionsResponse =
                            (TransactionsSEResponse)client.transactions(handelsbankenAccount);

                    // useUniqueIdWithoutClearingNumber
                    // temporary method to feature toggle what unique id to use for Handelsbanken SE
                    // this support should be removed once all clusters have been migrated to use
                    // Handelsbanken internal account number for transactional accounts and account
                    // based credit cards (allkort)
                    return handelsbankenAccount.toTransactionalAccount(transactionsResponse,
                            client.useUniqueIdWithoutClearingNumber());
                })
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public Stream<CreditCardAccount> toTinkCreditCard(HandelsbankenApiClient client) {
        return accounts.stream()
                .map(handelsbankenAccount -> {
                    TransactionsSEResponse transactionsResponse =
                            (TransactionsSEResponse)client.transactions(handelsbankenAccount);

                    // useUniqueIdWithoutClearingNumber
                    // temporary method to feature toggle what unique id to use for Handelsbanken SE
                    // this support should be removed once all clusters have been migrated to use
                    // Handelsbanken internal account number for transactional accounts and account
                    // based credit cards (allkort)
                    return handelsbankenAccount.toCreditCardAccount(transactionsResponse,
                            client.useUniqueIdWithoutClearingNumber());
                })
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public Stream<? extends HandelsbankenAccount> find(Account account) {
        return accounts.stream()
                .filter(handelsbankenAccount -> handelsbankenAccount.is(account));
    }
}
