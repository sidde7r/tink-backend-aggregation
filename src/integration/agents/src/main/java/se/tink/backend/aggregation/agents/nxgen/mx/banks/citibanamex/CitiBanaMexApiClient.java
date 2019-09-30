package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex;

import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants.BaseFormRequestParams;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants.BaseFormRequestValues;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants.Header;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants.RequestParams;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants.RequestServiceIds;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.authenticator.entity.MobileSdkRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.authenticator.rpc.GetClientNameResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional.rpc.PreviousTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.session.rpc.OfferResponse;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CitiBanaMexApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public CitiBanaMexApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    private RequestBuilder getBaserequestBuiler(String resource) {
        return client.request(String.format("%s%s", Urls.BASE_URL, resource));
    }

    private RequestBuilder getRequestBuilder(String resource) {
        return getBaserequestBuiler(resource).header(HttpHeaders.USER_AGENT, Header.USER_AGENT);
    }

    // Step 1 in authentication
    public void getClientName(String clientNumber) throws LoginException {
        String serialized =
                createBaseForm(RequestServiceIds.GET_CLIENT_NAME)
                        .put(RequestParams.CLIENT_NUMBER, clientNumber)
                        .build()
                        .serialize();
        String response =
                getRequestBuilder(Urls.GET_CLIENT_NAME).body(serialized).post(String.class);
        SerializationUtils.deserializeFromString(response, GetClientNameResponse.class)
                .handleErrors();
    }

    // Step 2 in authentication
    public LoginResponse login(String password) throws LoginException {
        String serialized =
                createBaseForm(RequestServiceIds.LOGIN)
                        .put(RequestParams.PASSWORD, password)
                        .build()
                        .serialize();
        String response = getRequestBuilder(Urls.LOGIN).body(serialized).post(String.class);
        return SerializationUtils.deserializeFromString(response, LoginResponse.class)
                .handleErrors();
    }

    public AccountsResponse fetchAccounts() {
        String serialized = createBaseForm(RequestServiceIds.GET_MULTI_BALANCE).build().serialize();
        String response =
                getRequestBuilder(Urls.GET_MULTI_BALANCE).body(serialized).post(String.class);
        return SerializationUtils.deserializeFromString(response, AccountsResponse.class)
                .handleErrors();
    }

    public PreviousTransactionsResponse fetchPreviousTransactions(
            String accountId, String continuityCode) {
        String serialized =
                createBaseForm(RequestServiceIds.GET_PREVIOUS_MOVEMENTS)
                        .put(
                                RequestParams.CONTINUITY_CODE,
                                continuityCode != null ? continuityCode : "")
                        .put(RequestParams.ACCOUNT_ID, accountId)
                        .build()
                        .serialize();
        String response =
                getRequestBuilder(Urls.GET_PREVIOUS_TRANSACTIONS)
                        .body(serialized)
                        .post(String.class);
        return SerializationUtils.deserializeFromString(
                        response, PreviousTransactionsResponse.class)
                .handleErrors();
    }

    public void logout() {
        String serialized = createBaseForm(RequestServiceIds.LOGOUT).build().serialize();
        getRequestBuilder(Urls.LOGOUT).body(serialized).post(String.class);
    }

    public void keepAlive() throws SessionException {
        String serialized = createBaseForm(RequestServiceIds.GET_OFFERS_XSELL).build().serialize();
        String response =
                getRequestBuilder(Urls.GET_OFFERS_XSELL).body(serialized).post(String.class);
        SerializationUtils.deserializeFromString(response, OfferResponse.class).handleErrors();
    }

    private Form.Builder createBaseForm(String serviceId) {
        String rsaApplicationKey = sessionStorage.get(Storage.RSA_APPLICATION_KEY);
        String timestamp = sessionStorage.get(Storage.TIMESTAMP);
        String hardwareId = sessionStorage.get(Storage.HARDWARE_ID);
        String deviceId = sessionStorage.get(Storage.DEVICE_ID);

        return Form.builder()
                .put(BaseFormRequestParams.APP_ID, BaseFormRequestValues.APP_ID)
                .put(BaseFormRequestParams.APP_VERSION, BaseFormRequestValues.APP_VERSION)
                .put(BaseFormRequestParams.CHANNEL, BaseFormRequestValues.CHANNEL)
                .put(BaseFormRequestParams.LANG, BaseFormRequestValues.LANG)
                .put(BaseFormRequestParams.MOBILE_IP_ADDRES, BaseFormRequestValues.MOBILE_IP_ADDRES)
                .put(BaseFormRequestParams.PLATFORM, BaseFormRequestValues.PLATFORM)
                .put(BaseFormRequestParams.PLATFORM_VERSION, BaseFormRequestValues.PLATFORM_VERSION)
                .put(BaseFormRequestParams.DEVICE_ID, deviceId)
                .put(BaseFormRequestParams.SERVICE_ID, serviceId)
                .put(
                        BaseFormRequestParams.RSA_MOBILE_SDK,
                        SerializationUtils.serializeToString(
                                new MobileSdkRequest(rsaApplicationKey, timestamp, hardwareId)));
    }
}
