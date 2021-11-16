package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.ScaMethod;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Values;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.ApprovalRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.ApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.ApproveChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.AutoLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.CompleteAppRegistrationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.CompleteAppRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.FinaliseApprovalRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.FinaliseApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.InitAppRegistrationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.InitAppRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.InitScaResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.ManualLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.PrepareApprovalRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.PrepareApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.SendTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.SendTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.UpdateAppRegistrationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ResultEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.RootModel;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionModel;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionResultEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc.Identifier;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc.SearchCriteriaDto;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc.TransactionRequestBody;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.session.entities.SessionModel;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.encoding.EncodingUtils;

public class CommerzbankApiClient {

    private final TinkHttpClient client;

    public CommerzbankApiClient(TinkHttpClient client) {
        this.client = client;
    }

    private static URL getUrl(String resource) {
        return new URL(Urls.HOST + resource);
    }

    // TODO: Stop using this method for the requests implemented originally.
    private RequestBuilder makeRequest(String resource) {
        return client.request(getUrl(resource))
                .type(MediaType.APPLICATION_JSON)
                .header(Headers.CCB_CLIENT_VERSION, Values.CCB_CLIENT_VERSION)
                .header(Headers.USER_AGENT, Values.CCB_CLIENT_VERSION);
    }

    private RequestBuilder getPostRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .header(Headers.CCB_CLIENT_VERSION, Values.CCB_CLIENT_VERSION)
                .header(Headers.USER_AGENT, Values.CCB_CLIENT_VERSION);
    }

    public LoginResponse manualLogin(String username, String password) {
        ManualLoginRequest request = ManualLoginRequest.create(username, password);

        return getPostRequest(Url.LOGIN).post(LoginResponse.class, request);
    }

    public LoginResponse autoLogin(String username, String password, String appId) {
        AutoLoginRequest request = AutoLoginRequest.create(username, password, appId);

        return getPostRequest(Url.LOGIN).post(LoginResponse.class, request);
    }

    public InitScaResponse initScaFlow() {
        return getPostRequest(Url.INIT_SCA).post(InitScaResponse.class);
    }

    public PrepareApprovalResponse prepareScaApproval(String processContextId) {
        PrepareApprovalRequest request =
                PrepareApprovalRequest.create(ScaMethod.PUSH_PHOTO_TAN, processContextId);

        return getPostRequest(Url.PREPARE_SCA).post(PrepareApprovalResponse.class, request);
    }

    public ApprovalResponse approveSca(String processContextId) {
        ApprovalRequest request = ApprovalRequest.create(processContextId);

        return getPostRequest(Url.APPROVE_SCA).post(ApprovalResponse.class, request);
    }

    public void finaliseScaApproval(String processContextId) {
        FinaliseApprovalRequest request = FinaliseApprovalRequest.create(processContextId);

        FinaliseApprovalResponse response =
                getPostRequest(Url.FINALISE_SCA).post(FinaliseApprovalResponse.class, request);

        if (!response.getStatusEntity().isLoginStatusOk()) {
            throw new IllegalStateException("Login status was not OK.");
        }
    }

    public String initAppRegistration() {
        InitAppRegistrationResponse response =
                getPostRequest(Url.INIT_APP_REGISTRATION)
                        .post(InitAppRegistrationResponse.class, new InitAppRegistrationRequest());

        return response.getAppId();
    }

    public void completeAppRegistration(String appId) {
        CompleteAppRegistrationRequest request = CompleteAppRegistrationRequest.create(appId);

        CompleteAppRegistrationResponse response =
                getPostRequest(Url.COMPLETE_APP_REGISTRATION)
                        .post(CompleteAppRegistrationResponse.class, request);

        if (response.getError() != null) {
            throw new IllegalStateException("App registration could not be completed.");
        }
    }

    public void send2FactorToken(String appId, PublicKey publicKey) {
        String b64EncodedPublickey = EncodingUtils.encodeAsBase64String(publicKey.getEncoded());

        SendTokenRequest request = SendTokenRequest.create(appId, b64EncodedPublickey);

        SendTokenResponse response =
                getPostRequest(Url.SEND_TWO_FACTOR_TOKEN).post(SendTokenResponse.class, request);

        if (!response.getStatusEntity().isStatusOk()) {
            throw new IllegalStateException("Sending of token was not successful.");
        }
    }

    public void approveChallenge(String appId, String b64EncodedSignature) {
        ApproveChallengeRequest request =
                ApproveChallengeRequest.create(appId, b64EncodedSignature);

        ApprovalResponse response =
                getPostRequest(Url.APPROVE_CHALLENGE).post(ApprovalResponse.class, request);

        if (!response.getStatusEntity().isLoginStatusOk()) {
            throw new IllegalStateException("Challenge approval was unsuccessful.");
        }
    }

    public void updateAppRegistration(String appId) {
        UpdateAppRegistrationRequest request = UpdateAppRegistrationRequest.create(appId);

        InitAppRegistrationResponse response =
                getPostRequest(Url.APP_REGISTRATION_UPDATE)
                        .post(InitAppRegistrationResponse.class, request);

        if (response.getError() != null) {
            throw new IllegalStateException("App registration update was unsuccessful.");
        }
    }

    public ResultEntity financialOverview() {
        return makeRequest(Urls.OVERVIEW).post(RootModel.class).getResult();
    }

    public SessionModel logout() {
        return makeRequest(Urls.LOGOUT).get(SessionModel.class);
    }

    public HttpResponse keepAlive() {
        return makeRequest(Urls.OVERVIEW).post(HttpResponse.class);
    }

    private String toCommerzDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(CommerzbankConstants.DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(CommerzbankConstants.TIMEZONE_CET));
        return sdf.format(date);
    }

    public TransactionResultEntity fetchAllPages(
            Date fromDate, Date toDate, String productType, String identifier, String productBranch)
            throws JsonProcessingException {

        TransactionResultEntity transactionResultEntity =
                transactionOverview(productType, identifier, fromDate, toDate, productBranch, 0);
        int page = 1;
        while (transactionResultEntity != null && transactionResultEntity.canFetchMore(page)) {
            transactionResultEntity.addAll(
                    transactionOverview(
                            productType, identifier, fromDate, toDate, productBranch, page));

            page++;
        }
        return transactionResultEntity;
    }

    private TransactionResultEntity transactionOverview(
            String productType,
            String identifier,
            Date fromdate,
            Date toDate,
            String productBranch,
            int page)
            throws JsonProcessingException {

        TransactionRequestBody transactionRequestBody =
                new TransactionRequestBody(
                        new SearchCriteriaDto(
                                toCommerzDate(fromdate),
                                toCommerzDate(toDate),
                                page,
                                Values.AMOUNT_TYPE,
                                50,
                                null),
                        new Identifier(
                                productType, Values.CURRENCY_VALUE, identifier, productBranch));
        String serialized = new ObjectMapper().writeValueAsString(transactionRequestBody);

        return makeRequest(Urls.TRANSACTIONS).post(TransactionModel.class, serialized).getResult();
    }
}
