package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.LoanAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class BankiaLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(BankiaLoanFetcher.class);

    private final BankiaApiClient apiClient;

    public BankiaLoanFetcher(BankiaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            List<LoanAccountEntity> loans = apiClient.getLoans();

            return Optional.ofNullable(loans).orElseGet(Collections::emptyList).stream()
                    .map(this::toLoanAccount)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.warn("Failed to fetch loan data " + e.getMessage());
        }

        return Collections.emptyList();
    }

    private LoanAccount toLoanAccount(LoanAccountEntity loanAccountEntity) {
        LOG.info(BankiaConstants.Logging.LOAN.toString() + " found Loan");
        return loanAccountEntity.toTinkLoanAccount(fetchLoanDetails(loanAccountEntity));
    }

    private LoanDetailsResponse fetchLoanDetails(LoanAccountEntity loanAccountEntity) {
        LoanDetailsRequest request =
                new LoanDetailsRequest(loanAccountEntity.getProductCode())
                        .setLoanIdentifier(loanAccountEntity.getLoanIdentifier());

        return apiClient.getLoanDetails(request);
    }
}
