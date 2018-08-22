package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.AccountTypeEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EuroInformationCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EuroInformationCreditCardFetcher.class);
    private static final AggregationLogger AGGREGATION_LOGGER = new AggregationLogger(EuroInformationCreditCardFetcher.class);
    private final static LogTag creditCardLogTag = LogTag.from("euroinformation_creditcard_fetcher");
    private final EuroInformationApiClient apiClient;
    private final SessionStorage sessionStorage;

    private EuroInformationCreditCardFetcher(EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static EuroInformationCreditCardFetcher create(EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        return new EuroInformationCreditCardFetcher(apiClient, sessionStorage);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        AccountSummaryResponse details = this.sessionStorage
                .get(EuroInformationConstants.Tags.ACCOUNT_LIST, AccountSummaryResponse.class)
                .orElseGet(() -> apiClient.requestAccounts());

        return details
                .getAccountDetailsList()
                .stream()
                //.filter(a -> a.getTinkTypeByTypeNumber().getTinkType().equals(AccountTypes.CREDIT_CARD))
                .filter(a -> a.getTinkTypeByTypeNumber().equals(AccountTypeEnum.UNKNOWN))
                .flatMap(a -> {
                    AGGREGATION_LOGGER.infoExtraLong(SerializationUtils.serializeToString(a), creditCardLogTag);
                    // TODO: We do not have account with credit card data
                    return Stream.<CreditCardAccount>empty();
                })
                .collect(Collectors.toList());
    }
}
