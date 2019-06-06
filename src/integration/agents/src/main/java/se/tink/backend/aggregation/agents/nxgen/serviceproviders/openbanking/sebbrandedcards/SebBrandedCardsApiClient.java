package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAbstractApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class SebBrandedCardsApiClient extends SebAbstractApiClient {

    public SebBrandedCardsApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        super(client, sessionStorage);
    }

    public List<CreditCardAccount> fetchCardAccounts() {
        URL url =
                new URL(
                        configuration.getBaseUrl()
                                + SebBrandedCardsConstants.Urls.CREDIT_CARD_ACCOUNTS);

        FetchCardAccountResponse response =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SebCommonConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                        .addBearerToken(getTokenFromSession())
                        .get(FetchCardAccountResponse.class);

        return response.getTransactions();
    }

    public FetchCardAccountsTransactions fetchCardTransactions(
            String accountId, LocalDate fromDate, LocalDate toDate) {

        URL url =
                new URL(
                                configuration.getBaseUrl()
                                        + SebBrandedCardsConstants.Urls.CREDIT_CARD_TRANSACTIONS)
                        .parameter(SebCommonConstants.IdTags.ACCOUNT_ID, accountId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SebCommonConstants.DATE_FORMAT);

        FetchCardAccountsTransactions response =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SebCommonConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                        .addBearerToken(getTokenFromSession())
                        .queryParam(
                                SebCommonConstants.QueryKeys.DATE_FROM, fromDate.format(formatter))
                        .queryParam(SebCommonConstants.QueryKeys.DATE_TO, toDate.format(formatter))
                        .get(FetchCardAccountsTransactions.class);

        return response;
    }
}
