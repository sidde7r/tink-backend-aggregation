package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.rpc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.entities.HandelsbankenFIAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class AccountListFIResponse extends AccountListResponse {
    private List<HandelsbankenFIAccount> accounts;

    @Override
    public Stream<TransactionalAccount> toTinkAccounts(ApplicationEntryPointResponse applicationEntryPoint) {
        return accounts.stream().map(HandelsbankenFIAccount::toTinkAccount);
    }

    @Override
    public Stream<CreditCardAccount> toTinkCreditCard(HandelsbankenApiClient client,
            ApplicationEntryPointResponse applicationEntryPoint) {
        return Stream.empty();
    }

    @Override
    public Optional<? extends HandelsbankenAccount> find(Account account) {
        return accounts.stream()
                .filter(handelsbankenAccount -> handelsbankenAccount.is(account))
                .findFirst();
    }
}
