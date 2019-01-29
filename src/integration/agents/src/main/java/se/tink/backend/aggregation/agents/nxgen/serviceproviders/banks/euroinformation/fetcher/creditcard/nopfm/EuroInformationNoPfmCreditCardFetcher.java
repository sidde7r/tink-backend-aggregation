package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard.nopfm;

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
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EuroInformationNoPfmCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EuroInformationNoPfmCreditCardFetcher.class);
    private static final AggregationLogger AGGREGATION_LOGGER = new AggregationLogger(
            EuroInformationNoPfmCreditCardFetcher.class);
    private final EuroInformationApiClient apiClient;
    private final SessionStorage sessionStorage;

    private EuroInformationNoPfmCreditCardFetcher(EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static EuroInformationNoPfmCreditCardFetcher create(EuroInformationApiClient apiClient,
            SessionStorage sessionStorage) {
        return new EuroInformationNoPfmCreditCardFetcher(apiClient, sessionStorage);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        AccountSummaryResponse details = this.sessionStorage
                .get(EuroInformationConstants.Tags.ACCOUNT_LIST, AccountSummaryResponse.class)
                .orElseGet(() -> apiClient.requestAccounts());

        return details
                .getAccountDetailsList()
                .stream()
                .filter(a ->
                        AccountTypes.CREDIT_CARD == a.getTinkTypeByTypeNumber().getTinkType()
                )
                .flatMap(a -> {
                    AGGREGATION_LOGGER.infoExtraLong(SerializationUtils.serializeToString(a),
                            EuroInformationConstants.LoggingTags.creditcardLogTag);
                    // TODO: We do not have account with credit card data containing available credit
                    return Stream.<CreditCardAccount>empty();
                })
                .collect(Collectors.toList());
    }
}
