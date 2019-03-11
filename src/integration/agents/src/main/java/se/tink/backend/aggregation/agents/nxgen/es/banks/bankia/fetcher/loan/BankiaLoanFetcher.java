package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BankiaLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(BankiaLoanFetcher.class);

    private final BankiaApiClient apiClient;

    public BankiaLoanFetcher(BankiaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        fetchAndLogLoanData();

        return Collections.emptyList();
    }

    private void fetchAndLogLoanData() {
        try {
            String response = apiClient.getLoanOverview();
            if (Strings.isNullOrEmpty(response)) {
                return;
            }
            boolean shouldBeLogged = SerializationUtils.deserializeFromString(response, LoanOverviewResponse.class)
                    .hasProducts();
            if (shouldBeLogged) {
                LOG.info(String.format("%s - %s", BankiaConstants.Logging.LOAN, response));
            }
        } catch (Exception e) {
            LOG.info("Failed to fetch loan data " + e.getMessage());
        }
    }
}
