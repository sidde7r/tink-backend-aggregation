package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl;

import java.util.Collections;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.rpc.DeviceConfigurationRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.rpc.DeviceConfigurationResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.AccountGroupEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.rpc.AccessSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.rpc.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.rpc.BaseMobileRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.storage.LclPersistentStorage;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LclApiClient {
    private static final AggregationLogger log = new AggregationLogger(LclApiClient.class);

    private final TinkHttpClient client;
    private final LclPersistentStorage lclPersistentStorage;

    public LclApiClient(TinkHttpClient client, LclPersistentStorage lclPersistentStorage) {
        this.client = client;
        this.lclPersistentStorage = lclPersistentStorage;
    }

    public void configureDevice() {
        DeviceConfigurationRequest request = DeviceConfigurationRequest.build();

        DeviceConfigurationResponse response =
                getDeviceConfigRequest().post(DeviceConfigurationResponse.class, request);

        if (!response.isResponse()) {
            throw new IllegalStateException("Expected response to be true");
        }
    }

    public String getXorKey() {
        return getPostFormRequest(LclConstants.Urls.SET_IDENTITY).post(String.class);
    }

    public LoginResponse login(String username, String bpiMetaData, String xorPin) {
        LoginRequest body = LoginRequest.create(username, bpiMetaData, xorPin);

        return postFormAndGetJsonResponse(
                getPostFormRequest(LclConstants.Urls.LOGIN),
                body.getBodyValue(),
                LoginResponse.class);
    }

    public boolean isAlive() {
        HttpResponse httpResponse =
                client.request(LclConstants.Urls.KEEP_ALIVE)
                        .queryParam(
                                LclConstants.AuthenticationValuePairs.AUDIENCE.getKey(),
                                LclConstants.AuthenticationValuePairs.AUDIENCE.getValue())
                        .get(HttpResponse.class);

        return httpResponse.getRedirects().size() == 0;
    }

    public AccountDetailsEntity getAccountDetails(
            String agency, String accountNumber, String cleLetter) {
        BaseMobileRequest detailListRequest = BaseMobileRequest.create();

        // We have to make this request in order to not get a redirect when fetching the account
        // details for a
        // specific account. The returned value is not needed.
        client.request(
                getPostFormRequest(LclConstants.Urls.ACCOUNT_DETAILS_LIST)
                        .post(String.class, detailListRequest.getBodyValue()));

        AccountDetailsRequest detailsRequest =
                AccountDetailsRequest.create(agency, accountNumber, cleLetter);

        // Fetching the account details for a specific account
        AccountDetailsResponse detailsResponse =
                postFormAndGetJsonResponse(
                        getPostFormRequest(LclConstants.Urls.ACCOUNT_DETAILS),
                        detailsRequest.getBodyValue(),
                        AccountDetailsResponse.class);

        return detailsResponse.getAccountDetails();
    }

    public Optional<AccountGroupEntity> getCheckingAccountGroup() {
        BaseMobileRequest body = BaseMobileRequest.create();

        AccessSummaryResponse response =
                postFormAndGetJsonResponse(
                        getPostFormRequest(LclConstants.Urls.ACCESS_SUMMARY),
                        body.getBodyValue(),
                        AccessSummaryResponse.class);

        return Optional.ofNullable(response.getAccountGroupList()).orElse(Collections.emptyList())
                .stream()
                .filter(AccountGroupEntity::isCheckingAccountGroup)
                .findFirst();
    }

    public TransactionsResponse getTransactions(AccountDetailsEntity accountDetailsEntity) {
        TransactionsRequest body = TransactionsRequest.create(accountDetailsEntity);

        return postFormAndGetJsonResponse(
                getPostFormRequest(LclConstants.Urls.TRANSACTIONS),
                body.getBodyValue(),
                TransactionsResponse.class);
    }

    private RequestBuilder getPostFormRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.WILDCARD);
    }

    private RequestBuilder getDeviceConfigRequest() {
        RequestBuilder builder =
                client.request(LclConstants.Urls.DEVICE_CONFIGURATION)
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON)
                        .header(LclConstants.DeviceConfiguration.USER_AGENT, client.getUserAgent())
                        .header(
                                LclConstants.HeaderValuePairs.X_AP_NETWORK.getKey(),
                                LclConstants.HeaderValuePairs.X_AP_NETWORK.getValue())
                        .header(
                                LclConstants.HeaderValuePairs.X_AP_SCREEN.getKey(),
                                LclConstants.HeaderValuePairs.X_AP_SCREEN.getValue())
                        .header(
                                LclConstants.HeaderValuePairs.X_AP_OS.getKey(),
                                LclConstants.HeaderValuePairs.X_AP_OS.getValue())
                        .header(
                                LclConstants.HeaderValuePairs.X_AP_SDK_VERSION.getKey(),
                                LclConstants.HeaderValuePairs.X_AP_SDK_VERSION.getValue())
                        .header(
                                LclConstants.HeaderValuePairs.X_AP_APP_VERSION.getKey(),
                                LclConstants.HeaderValuePairs.X_AP_APP_VERSION.getValue())
                        .header(
                                LclConstants.HeaderValuePairs.X_AP_KEY.getKey(),
                                LclConstants.HeaderValuePairs.X_AP_KEY.getValue())
                        .header(LclConstants.Headers.X_AP_REALTIME, System.currentTimeMillis());

        String deviceId = lclPersistentStorage.getDeviceId();

        if (!Strings.isNullOrEmpty(deviceId)) {
            builder.header(LclConstants.Headers.X_AP_DEVICEUUID, deviceId);
        }

        return builder;
    }

    /**
     * LCL returns JSON data with a text/html header which leads to parsing errors. This method
     * posts the given form request and returns the response as a JSON object.
     */
    private <T> T postFormAndGetJsonResponse(
            RequestBuilder postFormRequest, String body, Class<T> responseClass) {
        String responseString = postFormRequest.post(String.class, body);
        return SerializationUtils.deserializeFromString(responseString, responseClass);
    }
}
