package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanDetailsAggregate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanMovementEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class SantanderEsLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger logger =
            new AggregationLogger(SantanderEsLoanFetcher.class);

    private final SantanderEsApiClient apiClient;
    private final SantanderEsSessionStorage santanderEsSessionStorage;

    public SantanderEsLoanFetcher(
            final SantanderEsApiClient apiClient,
            final SantanderEsSessionStorage santanderEsSessionStorage) {
        this.apiClient = apiClient;
        this.santanderEsSessionStorage = santanderEsSessionStorage;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            LoginResponse loginResponse = santanderEsSessionStorage.getLoginResponse();

            String userDataXml =
                    SantanderEsXmlUtils.parseJsonToXmlString(loginResponse.getUserData());
            List<LoanEntity> loanEntities =
                    Optional.ofNullable(loginResponse.getLoans()).orElseGet(Collections::emptyList);
            return loanEntities.stream()
                    .map(loan -> toTinkLoanOptional(loan, userDataXml))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.info(
                    "Failed to fetch loan details " + SantanderEsConstants.Tags.LOAN_ACCOUNT, e);
        }

        return Collections.emptyList();
    }

    private Optional<LoanAccount> toTinkLoanOptional(LoanEntity loanEntity, String userDataXml) {
        try {
            // This request is necessary to get details in later stage
            // We do not use this data for now as it's not supported in our model
            LoanMovementEntity loanMovementsResponse =
                    apiClient.fetchLoanMovements(userDataXml, loanEntity);

            LoanDetailsEntity loanDetailsResponse =
                    apiClient.fetchLoanDetails(userDataXml, loanEntity);

            return Optional.of(
                    new LoanDetailsAggregate(loanEntity, loanDetailsResponse).toTinkLoanAccount());

        } catch (Exception e) {
            logger.info(
                    "Could not fetch loan details " + SantanderEsConstants.Tags.LOAN_ACCOUNT, e);
        }
        return Optional.empty();
    }
}
