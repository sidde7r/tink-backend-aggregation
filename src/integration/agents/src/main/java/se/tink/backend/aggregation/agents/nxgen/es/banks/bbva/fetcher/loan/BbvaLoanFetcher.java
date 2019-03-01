package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BbvaLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(BbvaLoanFetcher.class);

    private final BbvaApiClient apiClient;

    public BbvaLoanFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        FetchProductsResponse productsResponse = apiClient.fetchProducts();

        // loan logging
        logMortgage(productsResponse.getMultiMortgages());
        logLoan(
                productsResponse.getRevolvingCredits(),
                BbvaConstants.Logging.LOAN_REVOLVING_CREDIT);
        logLoan(
                productsResponse.getWorkingCapitalLoansLimits(),
                BbvaConstants.Logging.LOAN_WORKING_CAPITAL);

        return Collections.emptyList();
    }

    private void logLoan(List<Object> data, LogTag logTag) {
        if (data == null || data.isEmpty()) {
            return;
        }

        try {
            LOGGER.infoExtraLong(SerializationUtils.serializeToString(data), logTag);
        } catch (Exception e) {
            LOGGER.warn(logTag.toString() + " - Failed to log loan data, " + e.getMessage());
        }
    }

    private void logMortgage(List<Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        try {
            LOGGER.infoExtraLong(
                    SerializationUtils.serializeToString(data),
                    BbvaConstants.Logging.LOAN_MULTI_MORTGAGE);
            data.forEach(
                    loanObject -> {
                        String loanAsString = SerializationUtils.serializeToString(loanObject);
                        AccountEntity account =
                                SerializationUtils.deserializeFromString(
                                        loanAsString, AccountEntity.class);
                        if (account != null && account.getId() != null) {
                            String detailsReponse = apiClient.getLoanDetails(account.getId());
                            LOGGER.infoExtraLong(
                                    detailsReponse, BbvaConstants.Logging.LOAN_MULTI_MORTGAGE);
                        }
                    });
        } catch (Exception e) {
            LOGGER.warn(
                    BbvaConstants.Logging.LOAN_MULTI_MORTGAGE.toString()
                            + " - Failed to log mortgage data, "
                            + e.getMessage());
        }
    }
}
