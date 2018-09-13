package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard;

import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class EuroInformationCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EuroInformationCreditCardFetcher.class);
    private final EuroInformationApiClient apiClient;
    private final SessionStorage sessionStorage;

    private EuroInformationCreditCardFetcher(EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static EuroInformationCreditCardFetcher create(EuroInformationApiClient apiClient,
            SessionStorage sessionStorage) {
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
                .filter(a ->
                        AccountTypes.CREDIT_CARD == a.getTinkTypeByTypeNumber().getTinkType()
                )
                .map(a -> {
                    CreditCardAccount.Builder<CreditCardAccount, ?> accountBuilder = (CreditCardAccount.Builder) a
                            .getAccountBuilder();
                    return accountBuilder
                            .setName(a.getAccountName())
                            //TODO: make a test for this shit based on example message
                            .setAccountNumber(getAccountNumberFromName(a))
                            .putInTemporaryStorage(EuroInformationConstants.Tags.WEB_ID, a.getWebId())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String getAccountNumberFromName(AccountDetailsEntity a) {
        return a.getAccountNameAndNumber().split(a.getAccountName())[0].toLowerCase();
    }
}
