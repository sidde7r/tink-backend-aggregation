package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.virginmoney;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.PartyFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.PartyV31Fetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class VirginMoneyV31Ais extends UkOpenBankingV31Ais {

    private final PartyFetcher partyFetcher;

    public VirginMoneyV31Ais(
            UkOpenBankingAisConfig aisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource,
            UkOpenBankingApiClient apiClient,
            TransactionPaginationHelper transactionPaginationHelper) {
        super(aisConfig, persistentStorage, localDateTimeSource, transactionPaginationHelper);
        this.partyFetcher = new PartyV31Fetcher(apiClient, aisConfig, persistentStorage);
    }

    @Override
    public PartyFetcher makePartyFetcher(UkOpenBankingApiClient apiClient) {
        return partyFetcher;
    }
}
