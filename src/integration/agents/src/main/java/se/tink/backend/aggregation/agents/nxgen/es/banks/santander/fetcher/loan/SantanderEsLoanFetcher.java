package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanDetailsAggregate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanMovementEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class SantanderEsLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
            List<LoanEntity> loanEntities = loginResponse.getLoans();
            String userDataXml =
                    SantanderEsXmlUtils.parseJsonToXmlString(loginResponse.getUserData());
            return loanEntities.stream()
                    .map(loan -> toTinkLoanOptional(loan, userDataXml))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Failed to fetch loan details: ", e);
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
            logger.error("Could not fetch loan details: ", e);
        }
        return Optional.empty();
    }
}
