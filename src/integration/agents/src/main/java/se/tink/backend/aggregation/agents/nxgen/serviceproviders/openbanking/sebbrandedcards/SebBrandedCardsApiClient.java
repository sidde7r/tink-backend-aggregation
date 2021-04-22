package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards;

import java.time.LocalDate;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsConstants.Urls;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SebBrandedCardsApiClient extends SebBaseApiClient {

    private String brandId;

    public SebBrandedCardsApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            String brandId,
            boolean isManualRefresh) {
        super(client, persistentStorage, isManualRefresh);
        this.brandId = brandId;
    }

    @Override
    public RequestBuilder getAuthorizeUrl() {
        return client.request(new URL(SebBrandedCardsConstants.Urls.AUTH))
                .queryParam(SebBrandedCardsConstants.QueryKey.BRAND_ID, brandId)
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId());
    }

    @Override
    public AuthorizeResponse getAuthorization(String clientId, String redirectUri) {
        return null;
    }

    @Override
    public AuthorizeResponse postAuthorization(String requestForm) {
        return null;
    }

    @Override
    public OAuth2Token getToken(TokenRequest request) {
        return client.request(new URL(SebCommonConstants.Urls.BASE_URL).concat(Urls.TOKEN))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public FetchCardAccountResponse fetchCardAccounts() {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebBrandedCardsConstants.Urls.CREDIT_CARD_ACCOUNTS))
                .get(FetchCardAccountResponse.class);
    }

    @Override
    public FetchCardAccountsTransactions fetchCardTransactions(
            String accountId, LocalDate fromDate, LocalDate toDate) {

        URL url =
                new URL(SebCommonConstants.Urls.BASE_URL)
                        .concat(SebBrandedCardsConstants.Urls.CREDIT_CARD_TRANSACTIONS)
                        .parameter(SebCommonConstants.IdTags.ACCOUNT_ID, accountId);

        return buildCardTransactionsFetch(url, fromDate, toDate)
                .get(FetchCardAccountsTransactions.class);
    }
}
