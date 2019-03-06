package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class BbvaLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BbvaLoanFetcher.class);

    private final BbvaApiClient apiClient;

    public BbvaLoanFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient.fetchFinancialDashboard().getPositions().stream()
                .map(PositionEntity::getContract)
                .map(ContractEntity::getLoan)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::enrichLoanAccountWithDetails)
                .collect(Collectors.toList());
    }

    private LoanAccount enrichLoanAccountWithDetails(LoanEntity loan) {
        try {
            return apiClient.fetchLoanDetails(loan.getId()).toTinkLoanAccount(loan);
        } catch (HttpResponseException e) {
            final BbvaErrorResponse errorResponse =
                    e.getResponse().getBody(BbvaErrorResponse.class);

            switch (e.getResponse().getStatus()) {
                case 409:
                    LOGGER.warn(
                            String.format(
                                    "%s: Couldn't fetching loan details for loan %s; Error Code: %s; Message: %s",
                                    BbvaConstants.LogTags.LOAN_DETAILS,
                                    loan.getId(),
                                    errorResponse.getErrorCode(),
                                    errorResponse.getErrorMessage()));
                    return loan.toTinkLoanAccount();
                default:
                    throw e;
            }
        }
    }
}
