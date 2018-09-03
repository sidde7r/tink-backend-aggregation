package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.entities;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.HandelsbankenSEAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class AccountGroup {

    private List<HandelsbankenSEAccount> accounts;

    public Stream<TransactionalAccount> toAccounts(
            ApplicationEntryPointResponse applicationEntryPoint) {
        return accounts.stream()
                .map(handelsbankenAccount -> handelsbankenAccount.toTransactionalAccount(applicationEntryPoint))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public Stream<? extends HandelsbankenAccount> find(Account account) {
        return accounts.stream()
                .filter(handelsbankenAccount -> handelsbankenAccount.is(account));
    }
}
