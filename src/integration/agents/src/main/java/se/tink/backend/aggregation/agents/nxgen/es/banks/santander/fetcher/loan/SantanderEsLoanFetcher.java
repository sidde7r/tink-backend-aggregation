package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanDetailsAggregate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanMovementEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SantanderEsLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger LOG =
            new AggregationLogger(SantanderEsLoanFetcher.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(SantanderEsLoanFetcher.class);

    private final SantanderEsApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SantanderEsLoanFetcher(SantanderEsApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            LoginResponse loginResponse = getLoginResponse();

            String userDataXml =
                    SantanderEsXmlUtils.parseJsonToXmlString(loginResponse.getUserData());
            List<LoanEntity> loanEntities =
                    Optional.ofNullable(loginResponse.getLoans()).orElse(Collections.emptyList());
            return loanEntities
                    .stream()
                    .map(loan -> toTinkLoanOptional(loan, userDataXml))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            LOG.info("Failed to fetch loan details " + SantanderEsConstants.Tags.LOAN_ACCOUNT, e);
        }

        return Collections.emptyList();
    }

    private LoginResponse getLoginResponse() {
        String loginResponseString =
                sessionStorage
                        .get(SantanderEsConstants.Storage.LOGIN_RESPONSE, String.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SantanderEsConstants.LogMessages
                                                        .LOGIN_RESPONSE_NOT_FOUND));

        return SantanderEsXmlUtils.parseXmlStringToJson(loginResponseString, LoginResponse.class);
    }

    private Optional<LoanAccount> toTinkLoanOptional(LoanEntity loanEntity, String userDataXml) {
        try {
            LOGGER.debug(
                    SerializationUtils.serializeToString(loanEntity),
                    SantanderEsConstants.Tags.LOAN_ACCOUNT);

            // This request is necessary to get details in later stage
            // We do not use this data for now as it's not supported in our model
            LoanMovementEntity loanMovementsResponse =
                    apiClient.fetchLoanMovements(userDataXml, loanEntity);
            LOGGER.debug(
                    "Loans movement list: "
                            + SerializationUtils.serializeToString(loanMovementsResponse));

            LoanDetailsEntity loanDetailsResponse =
                    apiClient.fetchLoanDetails(userDataXml, loanEntity);
            LOG.infoExtraLong(
                    SerializationUtils.serializeToString(loanDetailsResponse),
                    SantanderEsConstants.Tags.LOAN_ACCOUNT);

            return Optional.of(
                    new LoanDetailsAggregate(loanEntity, loanDetailsResponse).toTinkLoanAccount());

        } catch (Exception e) {
            LOG.info("Could not fetch loan details " + SantanderEsConstants.Tags.LOAN_ACCOUNT, e);
        }
        return Optional.empty();
    }
}
