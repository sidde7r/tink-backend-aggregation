package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.rpc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.entities.HandelsbankenFIAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AccountListFIResponse extends AccountListResponse {
    private List<HandelsbankenFIAccount> accounts;

    @Override
    public Stream<TransactionalAccount> toTinkAccounts(HandelsbankenApiClient client) {
        return accounts.stream()
                .map(HandelsbankenFIAccount::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    @Override
    public Stream<CreditCardAccount> toTinkCreditCard(HandelsbankenApiClient client) {
        return Stream.empty();
    }

    @Override
    public Optional<? extends HandelsbankenAccount> find(Account account) {
        return accounts.stream()
                .filter(handelsbankenAccount -> handelsbankenAccount.is(account))
                .findFirst();
    }
}
