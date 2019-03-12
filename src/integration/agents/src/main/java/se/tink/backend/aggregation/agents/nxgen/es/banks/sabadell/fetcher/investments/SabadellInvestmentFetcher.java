package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.DepositsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.PensionPlansResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.SavingsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.ServicingFundsAccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.ServicingFundsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.util.Collection;
import java.util.Collections;

public class SabadellInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final AggregationLogger log = new AggregationLogger(SabadellInvestmentFetcher.class);
    private final SabadellApiClient apiClient;

    public SabadellInvestmentFetcher(SabadellApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        logDeposits();
        logServicingFunds();
        logPensionPlans();
        logSavings();

        return Collections.emptyList();
    }

    private void logDeposits() {
        try {
            DepositsResponse depositsResponse = apiClient.fetchDeposits();

            if (!depositsResponse.getAccountsPositions().isEmpty()) {
                log.infoExtraLong(
                        SerializationUtils.serializeToString(depositsResponse),
                        SabadellConstants.Tags.DEPOSITS);
            }
        } catch (Exception e) {
            log.warn(
                    String.format(
                            "%s could not fetch deposits", SabadellConstants.Tags.DEPOSITS_ERROR),
                    e);
        }
    }

    private void logServicingFunds() {
        try {
            ServicingFundsResponse servicingFundsResponse = apiClient.fetchServicingFunds();

            if (!servicingFundsResponse.getAccountList().getAccounts().isEmpty()
                    || servicingFundsResponse.getFundList() != null) {
                log.infoExtraLong(
                        SerializationUtils.serializeToString(servicingFundsResponse),
                        SabadellConstants.Tags.SERVICING_FUNDS);

                servicingFundsResponse
                        .getAccountList()
                        .getAccounts()
                        .forEach(
                                account -> {
                                    String detailsResponse =
                                            apiClient.fetchServicingFundsAccountDetails(
                                                    ServicingFundsAccountDetailsRequest
                                                            .createRequestFromAccount(account));

                                    log.infoExtraLong(
                                            detailsResponse,
                                            SabadellConstants.Tags.SERVICING_FUNDS_ACCOUNT_DETAILS);
                                });
            }
        } catch (Exception e) {
            log.warn(
                    String.format(
                            "%s could not fetch servicing funds",
                            SabadellConstants.Tags.SERVICING_FUNDS_ERROR),
                    e);
        }
    }

    private void logPensionPlans() {
        try {
            PensionPlansResponse pensionPlansResponse = apiClient.fetchPensionPlans();

            if (!pensionPlansResponse.getAccounts().isEmpty()) {
                log.infoExtraLong(
                        SerializationUtils.serializeToString(pensionPlansResponse),
                        SabadellConstants.Tags.PENSION_PLANS);
            }
        } catch (Exception e) {
            log.warn(
                    String.format(
                            "%s could not fetch pension plans",
                            SabadellConstants.Tags.PENSION_PLANS_ERROR),
                    e);
        }
    }

    private void logSavings() {
        try {
            SavingsResponse savingsResponse = apiClient.fetchSavings();

            if (!savingsResponse.getSavingPlans().isEmpty()) {
                log.infoExtraLong(
                        SerializationUtils.serializeToString(savingsResponse),
                        SabadellConstants.Tags.SAVINGS);
            }
        } catch (Exception e) {
            log.warn(
                    String.format(
                            "%s could not fetch savings", SabadellConstants.Tags.SAVINGS_ERROR),
                    e);
        }
    }
}
