package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais.defaultCreditCardAccountMapper;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais.defaultTransactionalAccountMapper;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.IdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.AccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.CreditCardAccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.TransactionalAccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.fetcher.BarclaysPartyDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.instrumentation.FetcherInstrumentationRegistry;

@RequiredArgsConstructor
public class BarclaysV31Ais implements UkOpenBankingAis {

    private final UkOpenBankingV31Ais ukOpenBankingV31Ais;
    private final UkOpenBankingAisConfig aisConfig;

    @Override
    public AccountFetcher<TransactionalAccount> makeTransactionalAccountFetcher(
            UkOpenBankingApiClient apiClient, FetcherInstrumentationRegistry instrumentation) {

        AccountTypeMapper accountTypeMapper = new AccountTypeMapper(aisConfig);
        BarclaysPartyDataFetcher accountPartyFetcher =
                new BarclaysPartyDataFetcher(
                        accountTypeMapper,
                        UkOpenBankingV31Ais.defaultPartyDataFetcher(apiClient, aisConfig));

        return new TransactionalAccountV31Fetcher(
                new AccountV31Fetcher<>(
                        apiClient,
                        accountPartyFetcher,
                        accountTypeMapper,
                        defaultTransactionalAccountMapper(),
                        instrumentation));
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
            UkOpenBankingApiClient apiClient, FetcherInstrumentationRegistry instrumentation) {

        AccountTypeMapper accountTypeMapper = new AccountTypeMapper(aisConfig);
        BarclaysPartyDataFetcher accountPartyFetcher =
                new BarclaysPartyDataFetcher(
                        accountTypeMapper,
                        UkOpenBankingV31Ais.defaultPartyDataFetcher(apiClient, aisConfig));

        return new CreditCardAccountV31Fetcher(
                new AccountV31Fetcher<>(
                        apiClient,
                        accountPartyFetcher,
                        accountTypeMapper,
                        defaultCreditCardAccountMapper(),
                        instrumentation));
    }

    @Override
    public TransactionPaginator<CreditCardAccount> makeCreditCardTransactionPaginatorController(
            UkOpenBankingApiClient apiClient) {
        return ukOpenBankingV31Ais.makeCreditCardTransactionPaginatorController(apiClient);
    }

    @Override
    public IdentityDataFetcher makeIdentityDataFetcher(UkOpenBankingApiClient apiClient) {
        return ukOpenBankingV31Ais.makeIdentityDataFetcher(apiClient);
    }
}
