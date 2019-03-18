package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
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
            LoanOverviewResponse loanOverview = apiClient.getLoanOverview();
            if (loanOverview == null) {
                return;
            }

            if (!loanOverview.isResultOk()) {
                return;
            }

            Optional.ofNullable(loanOverview.getProducts()).orElse(Collections.emptyList()).stream()
                    .forEach(loan -> {
                        String loanData = apiClient.getLoanDetailsPosition(loan);
                        LOG.info(BankiaConstants.Logging.LOAN.toString() + " - " + loanData);
                        loanData = apiClient.getLoanDetailsAval(loan);
                        LOG.info(BankiaConstants.Logging.LOAN.toString() + " - " + loanData);
                    });
        } catch (Exception e) {
            LOG.info("Failed to fetch loan data " + e.getMessage());
        }
    }
}
