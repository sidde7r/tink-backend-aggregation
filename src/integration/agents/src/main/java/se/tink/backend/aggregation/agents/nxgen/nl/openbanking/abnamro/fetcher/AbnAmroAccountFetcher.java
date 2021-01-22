package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.utils.AbnAmroUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@RequiredArgsConstructor
public class AbnAmroAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final AbnAmroApiClient apiClient;
    private final PersistentStorage persistentStorage;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        String accountId = getAccountId();
        AccountHolderResponse accountInfo = apiClient.fetchAccountHolder(accountId);
        String accountNumber = accountInfo.getAccountNumber();
        String holderName = accountInfo.getAccountHolderName();
        AccountBalanceResponse balanceInfo = apiClient.fetchAccountBalance(accountId);

        Optional<TransactionalAccount> account =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.CHECKING)
                        .withPaymentAccountFlag()
                        .withBalance(BalanceModule.of(balanceInfo.toAmount()))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accountNumber)
                                        .withAccountNumber(accountNumber)
                                        .withAccountName(accountNumber)
                                        .addIdentifier(new IbanIdentifier(accountNumber))
                                        .build())
                        .addHolderName(holderName)
                        .build();

        return account.map(Lists::newArrayList).orElseGet(Lists::newArrayList);
    }

    private String getAccountId() {
        return AbnAmroUtils.getAccountIdFromStorage(persistentStorage)
                .orElseGet(this::callApiAndStoreAccountId);
    }

    private String callApiAndStoreAccountId() {
        ConsentResponse consent = apiClient.consentRequest();
        String accountId = consent.getAccountId();
        AbnAmroUtils.putAccountIdInStorage(accountId, persistentStorage);
        return accountId;
    }
}
