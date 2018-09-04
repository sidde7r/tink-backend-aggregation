package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.entities.AccountGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class AccountListSEResponse extends AccountListResponse {

    private List<AccountGroup> accountGroups;

    @Override
    public Stream<TransactionalAccount> toTinkAccounts(ApplicationEntryPointResponse applicationEntryPoint) {
        return accountGroups == null ? Stream.empty() : accountGroups.stream()
                .flatMap(accountGroup -> accountGroup.toAccounts(applicationEntryPoint));
    }

    @Override
    public Stream<CreditCardAccount> toTinkCreditCard(HandelsbankenApiClient client,
            ApplicationEntryPointResponse applicationEntryPoint) {
        return accountGroups == null ? Stream.empty() : accountGroups.stream()
                .flatMap(accountGroup -> accountGroup.toTinkCreditCard(client, applicationEntryPoint));
    }

    @Override
    public Optional<? extends HandelsbankenAccount> find(Account account) {
        return accountGroups == null ? Optional.empty() : accountGroups.stream()
                .flatMap(accountGroup -> accountGroup.find(account))
                .findFirst();
    }
}
