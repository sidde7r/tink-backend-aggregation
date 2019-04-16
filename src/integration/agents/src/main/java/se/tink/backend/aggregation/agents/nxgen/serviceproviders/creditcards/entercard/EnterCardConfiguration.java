package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConstants.SIGNICAT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConstants.Urls.ACCOUNT_ENDPOINT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConstants.Urls.TRANSACTIONS_ENDPOINT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConstants.Urls.USER_ENDPOINT;

import se.tink.backend.aggregation.nxgen.http.URL;

public abstract class EnterCardConfiguration {
    public abstract String getServiceHost();
    public abstract String getAuthUrl();
    protected abstract String getSignicatTemplateName();
    public abstract String getJsonVendorMime();

    String getSignicatId() {
        return String.format(SIGNICAT_ID, getSignicatTemplateName());
    }

    URL getUserUrl() {
        return new URL(getServiceHost() + USER_ENDPOINT);
    }

    URL getAccountUrl(String accountIdentifier) {
        return new URL(getServiceHost() + ACCOUNT_ENDPOINT + accountIdentifier + EnterCardConstants.SLASH);
    }

    URL getTransactionsUrl(String accountIdentifier) {
        return getAccountUrl(accountIdentifier).concat(TRANSACTIONS_ENDPOINT);
    }

}
