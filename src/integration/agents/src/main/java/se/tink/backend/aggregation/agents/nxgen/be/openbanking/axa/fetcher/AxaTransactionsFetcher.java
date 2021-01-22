package se.tink.backend.aggregation.agents.nxgen.be.openbanking.axa.fetcher;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionDateFromFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AxaTransactionsFetcher
        extends Xs2aDevelopersTransactionDateFromFetcher<TransactionalAccount> {

    public AxaTransactionsFetcher(
            Xs2aDevelopersApiClient apiClient, LocalDateTimeSource localDateTimeSource) {
        super(apiClient, localDateTimeSource);
    }

    @Override
    public LocalDate minimalFromDate() {
        return localDateTimeSource.now().minusYears(1).toLocalDate();
    }
}
