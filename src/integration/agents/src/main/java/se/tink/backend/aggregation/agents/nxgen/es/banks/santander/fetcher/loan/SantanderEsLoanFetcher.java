package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsXmlUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SantanderEsLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger LOG = new AggregationLogger(SantanderEsLoanFetcher.class);

    private final SantanderEsApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SantanderEsLoanFetcher(
            SantanderEsApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            LoginResponse loginResponse = getLoginResponse();

            String userDataXml = SantanderEsXmlUtils.parseJsonToXmlString(loginResponse.getUserData());
            Optional.ofNullable(loginResponse.getLoans()).orElse(Collections.emptyList())
                    .forEach(loan -> logLoanDetails(loan, userDataXml));
        } catch (Exception e) {
            LOG.info("Failed to fetch loan details " + SantanderEsConstants.Tags.LOAN_ACCOUNT, e);
        }

        return Collections.emptyList();
    }

    private LoginResponse getLoginResponse() {
        String loginResponseString = sessionStorage.get(SantanderEsConstants.Storage.LOGIN_RESPONSE, String.class)
                .orElseThrow(() -> new IllegalStateException(
                        SantanderEsConstants.LogMessages.LOGIN_RESPONSE_NOT_FOUND));

        return SantanderEsXmlUtils.parseXmlStringToJson(loginResponseString, LoginResponse.class);
    }

    private void logLoanDetails(LoanEntity loanEntity, String userDataXml) {
        try {
            LOG.infoExtraLong(SerializationUtils.serializeToString(loanEntity), SantanderEsConstants.Tags.LOAN_ACCOUNT);
            String loanDetailsResponse = apiClient.fetchLoanDetails(userDataXml, loanEntity);
            LOG.infoExtraLong(loanDetailsResponse, SantanderEsConstants.Tags.LOAN_ACCOUNT);
        } catch (Exception e) {
            LOG.info("Could not fetch loan details " + SantanderEsConstants.Tags.LOAN_ACCOUNT, e);
        }
    }
}
