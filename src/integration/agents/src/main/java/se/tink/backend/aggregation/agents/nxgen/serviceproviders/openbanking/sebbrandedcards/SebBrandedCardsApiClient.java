package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAbstractApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SebBrandedCardsApiClient extends SebAbstractApiClient {

    private String brandId;

    public SebBrandedCardsApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        super(client, sessionStorage);
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    @Override
    public RequestBuilder getAuthorizeUrl() {
        return client.request(new URL(SebBrandedCardsConstants.Urls.AUTH))
                .queryParam(SebBrandedCardsConstants.QueryKey.BRAND_ID, this.brandId);
    }

    @Override
    public OAuth2Token getToken(TokenRequest request) {
        return client.request(
                        new URL(
                                SebCommonConstants.Urls.BASE_URL
                                        + SebBrandedCardsConstants.Urls.TOKEN))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public List<CreditCardAccount> fetchCardAccounts() {
        return createRequestInSession(
                        new URL(
                                SebCommonConstants.Urls.BASE_URL
                                        + SebBrandedCardsConstants.Urls.CREDIT_CARD_ACCOUNTS))
                .get(FetchCardAccountResponse.class)
                .getTransactions();
    }

    public FetchCardAccountsTransactions fetchCardTransactions(
            String accountId, LocalDate fromDate, LocalDate toDate) {

        URL url =
                new URL(
                                SebCommonConstants.Urls.BASE_URL
                                        + SebBrandedCardsConstants.Urls.CREDIT_CARD_TRANSACTIONS)
                        .parameter(SebCommonConstants.IdTags.ACCOUNT_ID, accountId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SebCommonConstants.DATE_FORMAT);

        return createRequestInSession(url)
                .queryParam(SebCommonConstants.QueryKeys.DATE_FROM, fromDate.format(formatter))
                .queryParam(SebCommonConstants.QueryKeys.DATE_TO, toDate.format(formatter))
                .queryParam(
                        SebCommonConstants.QueryKeys.BOOKING_STATUS,
                        SebCommonConstants.QueryValues.PENDING_AND_BOOKED_TRANSACTIONS)
                .get(FetchCardAccountsTransactions.class);
    }
}
