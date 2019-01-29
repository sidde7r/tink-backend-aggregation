package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.loan;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EuroInformationLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EuroInformationLoanFetcher.class);
    private static final AggregationLogger AGGREGATION_LOGGER = new AggregationLogger(
            EuroInformationLoanFetcher.class);
    private final EuroInformationApiClient apiClient;
    private final SessionStorage sessionStorage;

    private EuroInformationLoanFetcher(EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static EuroInformationLoanFetcher create(EuroInformationApiClient apiClient,
            SessionStorage sessionStorage) {
        return new EuroInformationLoanFetcher(apiClient, sessionStorage);
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        AccountSummaryResponse details = this.sessionStorage
                .get(EuroInformationConstants.Tags.ACCOUNT_LIST, AccountSummaryResponse.class)
                .orElseGet(() -> apiClient.requestAccounts());

        return details
                .getAccountDetailsList()
                .stream()
                .filter(a ->
                        AccountTypes.LOAN == a.getTinkTypeByTypeNumber().getTinkType()
                )
                .flatMap(a -> {
                    AGGREGATION_LOGGER.infoExtraLong(SerializationUtils.serializeToString(a),
                            EuroInformationConstants.LoggingTags.loanAccountLogTag);
                    return Stream.<LoanAccount>empty();
                })
                .collect(Collectors.toList());

    }
}
