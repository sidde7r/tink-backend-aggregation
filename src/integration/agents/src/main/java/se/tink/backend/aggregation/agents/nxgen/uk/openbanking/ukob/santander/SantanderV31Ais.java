package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.santander;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.PartyFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.AccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.CreditCardAccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.TransactionalAccountV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.santander.fetcher.SantanderPartyFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class SantanderV31Ais extends UkOpenBankingV31Ais {

    private final PartyFetcher santanderPartyFetcher;
    private final AccountMapper<CreditCardAccount> santanderCreditCardAccountMapper;

    public SantanderV31Ais(
            UkOpenBankingAisConfig aisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource,
            UkOpenBankingApiClient apiClient,
            TransactionPaginationHelper transactionPaginationHelper) {
        super(aisConfig, persistentStorage, localDateTimeSource, transactionPaginationHelper);

        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        this.santanderCreditCardAccountMapper =
                new CreditCardAccountMapper(
                        new SantanderCreditCardBalanceMapper(valueExtractor),
                        new DefaultIdentifierMapper(valueExtractor));
        this.santanderPartyFetcher =
                new SantanderPartyFetcher(apiClient, aisConfig, persistentStorage);
    }

    @Override
    public AccountFetcher<TransactionalAccount> makeTransactionalAccountFetcher(
            UkOpenBankingApiClient apiClient) {

        return new TransactionalAccountV31Fetcher(
                new AccountV31Fetcher<>(
                        apiClient, santanderPartyFetcher, defaultTransactionalAccountMapper()));
    }

    @Override
    public AccountFetcher<CreditCardAccount> makeCreditCardAccountFetcher(
            UkOpenBankingApiClient apiClient) {
        return new CreditCardAccountV31Fetcher(
                new AccountV31Fetcher<>(
                        apiClient, santanderPartyFetcher, santanderCreditCardAccountMapper));
    }

    @Override
    public PartyFetcher makePartyFetcher(UkOpenBankingApiClient apiClient) {
        return santanderPartyFetcher;
    }
}
