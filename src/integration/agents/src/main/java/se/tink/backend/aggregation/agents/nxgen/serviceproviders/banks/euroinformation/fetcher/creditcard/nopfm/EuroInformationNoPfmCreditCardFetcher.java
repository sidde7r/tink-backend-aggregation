package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard.nopfm;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class EuroInformationNoPfmCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final EuroInformationApiClient apiClient;
    private final SessionStorage sessionStorage;

    private EuroInformationNoPfmCreditCardFetcher(
            EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static EuroInformationNoPfmCreditCardFetcher create(
            EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        return new EuroInformationNoPfmCreditCardFetcher(apiClient, sessionStorage);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        // We do not have account with credit card data containing available credit. This is
        // just to log the traffic in S3 for further implementation.
        this.sessionStorage
                .get(EuroInformationConstants.Tags.ACCOUNT_LIST, AccountSummaryResponse.class)
                .orElseGet(() -> apiClient.requestAccounts());

        return Collections.emptyList();
    }
}
