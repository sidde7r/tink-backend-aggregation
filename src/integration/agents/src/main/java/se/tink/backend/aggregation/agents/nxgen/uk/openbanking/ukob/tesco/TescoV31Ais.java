package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.tesco;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.PartyFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.tesco.fetcher.TescoPartyFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class TescoV31Ais extends UkOpenBankingV31Ais {

    private final PartyFetcher tescoPartyFetcher;

    public TescoV31Ais(
            UkOpenBankingAisConfig aisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource,
            UkOpenBankingApiClient apiClient,
            TransactionPaginationHelper transactionPaginationHelper) {
        super(aisConfig, persistentStorage, localDateTimeSource, transactionPaginationHelper);
        this.tescoPartyFetcher = new TescoPartyFetcher(apiClient, aisConfig, persistentStorage);
    }

    @Override
    public PartyFetcher makePartyFetcher(UkOpenBankingApiClient apiClient) {
        return tescoPartyFetcher;
    }
}
