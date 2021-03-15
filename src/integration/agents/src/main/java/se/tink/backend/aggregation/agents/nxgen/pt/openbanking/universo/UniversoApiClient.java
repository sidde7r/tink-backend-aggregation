package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import java.time.LocalDate;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo.UniversoConstants.TokenGrantTypes;
import se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo.UniversoConstants.UniversoQueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.TokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UniversoApiClient extends Xs2aDevelopersApiClient {
    public UniversoApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Xs2aDevelopersProviderConfiguration configuration,
            boolean isManual,
            String userIp,
            RandomValueGenerator randomValueGenerator) {
        super(client, persistentStorage, configuration, isManual, userIp, randomValueGenerator);
    }

    @Override
    public HttpResponse createConsent(ConsentRequest consentRequest, String psuId) {
        return createRequest(new URL(configuration.getBaseUrl() + ApiServices.CONSENT))
                .header(HeaderKeys.PSU_IP_ADDRESS, userIp)
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID())
                .body(consentRequest)
                .post(HttpResponse.class);
    }

    @Override
    protected RequestBuilder getTokenRequest(TokenForm tokenForm) {
        RequestBuilder requestBuilder =
                createRequest(
                                new URL(
                                        configuration.getBaseUrl()
                                                + UniversoConstants.ApiServices.TOKEN))
                        .body(tokenForm, MediaType.APPLICATION_FORM_URLENCODED);
        if (tokenForm.getBodyValue().contains(FormValues.AUTHORIZATION_CODE)) {
            return requestBuilder.header(
                    UniversoConstants.HeaderKeys.GRANT_TYPE, TokenGrantTypes.AUTHORIZATION);
        } else if (tokenForm.getBodyValue().contains(FormValues.REFRESH_TOKEN)) {
            return requestBuilder.header(
                    UniversoConstants.HeaderKeys.GRANT_TYPE, TokenGrantTypes.REFRESH);
        }
        return super.getTokenRequest(tokenForm);
    }

    @Override
    public GetTransactionsResponse getTransactions(
            Account account, LocalDate dateFrom, LocalDate toDate) {
        URL transactionFetchUrl =
                new URL(configuration.getBaseUrl() + ApiServices.GET_TRANSACTIONS)
                        .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier());

        return createFetchingRequest(transactionFetchUrl)
                .queryParam(QueryKeys.DATE_FROM, DATE_FORMATTER.format(dateFrom))
                .queryParam(QueryKeys.DATE_TO, DATE_FORMATTER.format(toDate))
                .queryParam(QueryKeys.BOOKING_STATUS, UniversoQueryValues.BOOKED)
                .get(GetTransactionsResponse.class);
    }
}
