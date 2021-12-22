package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.PartyFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.AccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.CreditCardAccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.TransactionalAccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.fetcher.BarclaysPartyFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.instrumentation.FetcherInstrumentationRegistry;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BarclaysV31Ais extends UkOpenBankingV31Ais {

    private final PartyFetcher barclaysPartyFetcher;

    public BarclaysV31Ais(
            UkOpenBankingAisConfig aisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource,
            UkOpenBankingApiClient apiClient,
            TransactionPaginationHelper transactionPaginationHelper) {
        super(aisConfig, persistentStorage, localDateTimeSource, transactionPaginationHelper);
        this.barclaysPartyFetcher =
                new BarclaysPartyFetcher(apiClient, aisConfig, persistentStorage);
    }

    @Override
    public AccountFetcher<TransactionalAccount> makeTransactionalAccountFetcher(
            UkOpenBankingApiClient apiClient, FetcherInstrumentationRegistry instrumentation) {

        return new TransactionalAccountV31Fetcher(
                new AccountV31Fetcher<>(
                        apiClient,
                        barclaysPartyFetcher,
                        defaultTransactionalAccountMapper(),
                        instrumentation));
    }

    @Override
    public AccountFetcher<CreditCardAccount> makeCreditCardAccountFetcher(
            UkOpenBankingApiClient apiClient, FetcherInstrumentationRegistry instrumentation) {

        return new CreditCardAccountV31Fetcher(
                new AccountV31Fetcher<>(
                        apiClient,
                        barclaysPartyFetcher,
                        defaultCreditCardAccountMapper(),
                        instrumentation));
    }

    @Override
    public PartyFetcher makePartyFetcher(UkOpenBankingApiClient apiClient) {
        return barclaysPartyFetcher;
    }
}
