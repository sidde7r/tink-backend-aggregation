package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.PartyFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.AccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.CreditCardAccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.TransactionalAccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.fetcher.HsbcPartyFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.instrumentation.FetcherInstrumentationRegistry;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class HsbcV31Ais extends UkOpenBankingV31Ais {

    private final AccountTypeMapper accountTypeMapper;
    private final PartyFetcher hsbcPartyFetcher;

    public HsbcV31Ais(
            UkOpenBankingAisConfig aisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource,
            UkOpenBankingApiClient apiClient) {
        super(aisConfig, persistentStorage, localDateTimeSource);
        this.accountTypeMapper = new AccountTypeMapper(aisConfig);
        this.hsbcPartyFetcher = new HsbcPartyFetcher(apiClient, aisConfig, persistentStorage);
    }

    @Override
    public AccountFetcher<TransactionalAccount> makeTransactionalAccountFetcher(
            UkOpenBankingApiClient apiClient, FetcherInstrumentationRegistry instrumentation) {

        return new TransactionalAccountV31Fetcher(
                new AccountV31Fetcher<>(
                        apiClient,
                        hsbcPartyFetcher,
                        accountTypeMapper,
                        defaultTransactionalAccountMapper(),
                        instrumentation));
    }

    @Override
    public AccountFetcher<CreditCardAccount> makeCreditCardAccountFetcher(
            UkOpenBankingApiClient apiClient, FetcherInstrumentationRegistry instrumentation) {

        return new CreditCardAccountV31Fetcher(
                new AccountV31Fetcher<>(
                        apiClient,
                        hsbcPartyFetcher,
                        accountTypeMapper,
                        defaultCreditCardAccountMapper(),
                        instrumentation));
    }

    @Override
    public PartyFetcher makePartyFetcher(UkOpenBankingApiClient apiClient) {
        return hsbcPartyFetcher;
    }
}
