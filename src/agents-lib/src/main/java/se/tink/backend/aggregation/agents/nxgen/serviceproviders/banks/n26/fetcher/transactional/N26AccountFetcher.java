package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.transactional;

import java.util.Arrays;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class N26AccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final N26ApiClient n26ApiClient;
    private static final Logger logger = LoggerFactory.getLogger(N26ApiClient.class);

    public N26AccountFetcher(N26ApiClient n26ApiClient){
        this.n26ApiClient = n26ApiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        fetchAndLogSavingsAccounts();
        return Arrays.asList(n26ApiClient.fetchAccounts().toTransactionalAccount());
    }

    private void fetchAndLogSavingsAccounts(){
        try {
            String result = n26ApiClient.testFetchSavingsAccounts();
            logger.info(result, N26Constants.Logging.SAVINGS_ACCOUNT_LOGGING);
        } catch (Exception e){
            logger.warn(e.toString(), "Error occurred during savings account fetching:", N26Constants.Logging.SAVINGS_ACCOUNT_LOGGING);
        }
    }
}
