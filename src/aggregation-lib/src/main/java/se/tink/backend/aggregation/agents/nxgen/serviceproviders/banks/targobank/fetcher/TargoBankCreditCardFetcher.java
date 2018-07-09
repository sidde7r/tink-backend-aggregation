package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.entities.AccountTypeEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class TargoBankCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TargoBankCreditCardFetcher.class);
    private static final AggregationLogger AGGREGATION_LOGGER = new AggregationLogger(TargoBankCreditCardFetcher.class);
    private final static LogTag creditCardLogTag = LogTag.from("targobank_creditcard_fetcher");
    private final TargoBankApiClient apiClient;
    private final SessionStorage sessionStorage;

    private TargoBankCreditCardFetcher(TargoBankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static TargoBankCreditCardFetcher create(TargoBankApiClient apiClient, SessionStorage sessionStorage) {
        return new TargoBankCreditCardFetcher(apiClient, sessionStorage);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        AccountSummaryResponse details = this.sessionStorage
                .get(TargoBankConstants.Tags.ACCOUNT_LIST, AccountSummaryResponse.class)
                .orElse(apiClient.requestAccounts());

        AGGREGATION_LOGGER.infoExtraLong(details.toString(), creditCardLogTag);
        return details
                .getAccountDetailsList()
                .stream()
                .filter(a -> a.getTinkTypeByTypeNumber().getTinkType().equals(AccountTypes.CREDIT_CARD))
                .flatMap(a -> {
                    // TODO: We do not have account with credit card data
                    return Stream.<CreditCardAccount>empty();
                })
                .collect(Collectors.toList());
    }
}
