package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class AbnAmroAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final AbnAmroApiClient apiClient;

    public AbnAmroAccountFetcher(final AbnAmroApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        AccountHolderResponse accountInfo = apiClient.fetchAccountHolder();
        String accountNumber = accountInfo.getAccountNumber();
        String holderName = accountInfo.getAccountHolderName();
        AccountBalanceResponse balanceInfo = apiClient.fetchAccountBalance();

        TransactionalAccount account =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.CHECKING)
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accountNumber)
                                        .withAccountNumber(accountNumber)
                                        .withAccountName(accountNumber)
                                        .addIdentifier(new IbanIdentifier(accountNumber))
                                        .build())
                        .withBalance(BalanceModule.of(balanceInfo.toAmount()))
                        .addHolderName(holderName)
                        .putInTemporaryStorage(StorageKey.ACCOUNT_CONSENT_ID, accountNumber)
                        .build();

        return Stream.of(account).collect(Collectors.toList());
    }
}
