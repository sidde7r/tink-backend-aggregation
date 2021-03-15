package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionDateFromFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class UniversoTransactionDateFromFetcher
        extends Xs2aDevelopersTransactionDateFromFetcher<TransactionalAccount> {

    public UniversoTransactionDateFromFetcher(
            Xs2aDevelopersApiClient apiClient,
            LocalDateTimeSource localDateTimeSource,
            boolean isManual) {
        super(apiClient, localDateTimeSource, isManual);
    }

    @Override
    public LocalDate minimalFromDate() {
        return LocalDate.now().minusYears(10).plusDays(1);
    }
}
