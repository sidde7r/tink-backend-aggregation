package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.IdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.IdentityDataMapper;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.MonzoIdentityDataV31Fetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class MonzoV31Ais implements UkOpenBankingAis {

    private final UkOpenBankingV31Ais ukOpenBankingV31Ais;
    private final UkOpenBankingAisConfig aisConfig;
    private final PersistentStorage persistentStorage;

    @Override
    public AccountFetcher<TransactionalAccount> makeTransactionalAccountFetcher(
            UkOpenBankingApiClient apiClient) {
        return ukOpenBankingV31Ais.makeTransactionalAccountFetcher(apiClient);
    }

    @Override
    public TransactionPaginator<TransactionalAccount> makeAccountTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return ukOpenBankingV31Ais.makeAccountTransactionPaginatorController(apiClient);
    }

    @Override
    public Optional<UkOpenBankingUpcomingTransactionFetcher<?>> makeUpcomingTransactionFetcher(
            UkOpenBankingApiClient apiClient) {
        return ukOpenBankingV31Ais.makeUpcomingTransactionFetcher(apiClient);
    }

    @Override
    public AccountFetcher<CreditCardAccount> makeCreditCardAccountFetcher(
            UkOpenBankingApiClient apiClient) {
        return ukOpenBankingV31Ais.makeCreditCardAccountFetcher(apiClient);
    }

    @Override
    public TransactionPaginator<CreditCardAccount> makeCreditCardTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return ukOpenBankingV31Ais.makeCreditCardTransactionPaginatorController(apiClient);
    }

    @Override
    public IdentityDataFetcher makeIdentityDataFetcher(UkOpenBankingApiClient apiClient) {
        return new MonzoIdentityDataV31Fetcher(
                apiClient, aisConfig, new IdentityDataMapper(), persistentStorage);
    }
}
