package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.loan.rpc.SoTokenResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.agents.rpc.Credentials;

public class SparebankenSorLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(SparebankenSorLoanFetcher.class);

    private final SparebankenSorApiClient apiClient;

    public SparebankenSorLoanFetcher(SparebankenSorApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            // These requests seem to be chained with the loan fetching url, for now assuming that they are
            // necessary for accessing the loans.
            URL fetchSoTokenUrl = new URL("https://nettbank.sor.no/secesb/rest/era/ssotoken/so")
                    .queryParam("endpoint", "classic");
            SoTokenResponse soTokenResponse = apiClient.fetchSoToken(fetchSoTokenUrl);

            URL transigoLogonUrl = new URL("https://nettbank.sor.no/payment/transigo/logon/done/smartbank/json")
                    .queryParam("so", soTokenResponse.getSo());
            apiClient.transigoLogon(transigoLogonUrl);

            // Not sure if this is the correct url for fetching loans, it's where the app goes if you choose
            // loans although the current account is listed there as well. Logging to see if it will contain loans
            // for users that actually have loans.
            URL transigoAccountsUrl = new URL("https://nettbank.sor.no/payment/transigo/json/accounts");
            String transigoAccountsResponse = apiClient.transigoAccounts(transigoAccountsUrl);

            LOGGER.infoExtraLong(transigoAccountsResponse, SparebankenSorConstants.LogTags.LOAN_LOG_TAG);
        } catch (Exception e) {
            LOGGER.infoExtraLong("Failed to retrieve loans", SparebankenSorConstants.LogTags.LOAN_LOG_TAG);
        }
        return Collections.emptyList();
    }
}
