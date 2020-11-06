package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import java.lang.invoke.MethodHandles;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.CheckForcedUpgradeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.ExecuteContractUpdateRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.ExecuteContractUpdateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.PrepareContractUpdateRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.PrepareContractUpdateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.AuthenticationProcessRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.AuthenticationProcessResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.DistributorAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EBankingUsersRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EbankingUsersResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.GenerateChallangeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.UserInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.AccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.UpcomingTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FortisApiClient {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TinkHttpClient client;
    private final String CSRF = FortisUtils.generateCSRF();
    private final String baseUrl;
    private final String distributorId;

    public FortisApiClient(TinkHttpClient client, String baseUrl, String distributorId) {
        this.client = client;
        this.baseUrl = baseUrl;
        this.distributorId = distributorId;
        client.setUserAgent(getUserAgent());
    }

    public String getDistributorId() {
        return distributorId;
    }

    private String getUserAgent() {
        String mozillaVersion =
                DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getMozillaVersion();
        String iphoneModel = DeviceProfileConfiguration.IOS_STABLE.getModelNumber();
        String iOSVersion = DeviceProfileConfiguration.IOS_STABLE.getOsVersion();
        String appleWebKit =
                DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getPlatform();
        String platformDetails =
                DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getPlatformDetails();
        String extra =
                String.format(
                        "Mobile/7D11 FAT/ APPTYPE=001/ APPVERSION=%s/OS=ios-phone",
                        FortisConstants.APP_VERSION);

        return String.format(
                "%s (%s; U;iOS %s; en-us) %s %s %s",
                mozillaVersion, iphoneModel, iOSVersion, appleWebKit, platformDetails, extra);
    }

    private void checkForcedUpgrade(String distributorId) {
        CheckForcedUpgradeRequest request = new CheckForcedUpgradeRequest(distributorId);
        getRequestBuilderWithCookies(FortisConstants.Urls.CHECK_FORCED_UPGRADE)
                .post(HttpResponse.class, SerializationUtils.serializeToString(request));
    }

    private RequestBuilder getRequestBuilderWithCookies(String resource) {
        return client.request(getUrl(resource))
                .cookie(FortisConstants.Cookie.CSRF, CSRF)
                .cookie(FortisConstants.Cookie.AXES, FortisUtils.generateAxes())
                .cookie(
                        FortisConstants.Cookie.DEVICE_FEATURES,
                        FortisConstants.HeaderValues.DEVICE_FEATURES_VALUE)
                .cookie(FortisConstants.Cookie.DISTRIBUTOR_ID, distributorId)
                .cookie(FortisConstants.Cookie.EUROPOLICY, FortisConstants.Cookie.EUROPOLICY_OPTIN)
                .header(FortisConstants.Headers.CSRF, CSRF)
                .header(FortisConstants.Headers.USER_AGENT, getUserAgent())
                .type(MediaType.APPLICATION_JSON_TYPE);
    }

    private URL getUrl(String resource) {
        return new URL(String.format("%s%s", baseUrl, resource));
    }

    public EbankingUsersResponse getEBankingUsers(EBankingUsersRequest eBankingUsersRequest) {
        // These two calls MUST be made in this order. Otherwise correct cookies will not be set!
        checkForcedUpgrade(eBankingUsersRequest.getDistributorId());
        getDistributorAuthenticationMeans();

        EbankingUsersResponse response =
                getRequestBuilderWithCookies(Urls.GET_E_BANKING_USERS)
                        .post(
                                EbankingUsersResponse.class,
                                SerializationUtils.serializeToString(eBankingUsersRequest));
        response.getBusinessMessageBulk().checkError();
        return response;
    }

    public AuthenticationProcessResponse createAuthenticationProcess(
            AuthenticationProcessRequest authenticationProcessRequest) {
        AuthenticationProcessResponse response =
                getRequestBuilderWithCookies(Urls.CREATE_AUTHENTICATION_PROCESS)
                        .post(
                                AuthenticationProcessResponse.class,
                                SerializationUtils.serializeToString(authenticationProcessRequest));
        response.getBusinessMessageBulk().checkError();
        return response;
    }

    public AccountsResponse fetchAccounts() {
        AccountsResponse response =
                getRequestBuilderWithCookies(Urls.GET_VIEW_ACCOUNT_LIST)
                        .post(
                                AccountsResponse.class,
                                SerializationUtils.serializeToString(new AccountsRequest()));
        response.getBusinessMessageBulk().checkError();
        return response;
    }

    public TransactionsResponse fetchTransactions(int page, String accountProductId) {
        TransactionsRequest request = new TransactionsRequest(accountProductId, page);
        TransactionsResponse response =
                getRequestBuilderWithCookies(Urls.TRANSACTIONS)
                        .post(
                                TransactionsResponse.class,
                                SerializationUtils.serializeToString(request));
        response.getBusinessMessageBulk().checkError();
        return response;
    }

    public UpcomingTransactionsResponse fetchUpcomingTransactions(
            int page, String accountProductId) {
        TransactionsRequest request = new TransactionsRequest(accountProductId, page);
        UpcomingTransactionsResponse response =
                getRequestBuilderWithCookies(Urls.UPCOMING_TRANSACTIONS)
                        .post(
                                UpcomingTransactionsResponse.class,
                                SerializationUtils.serializeToString(request));
        response.getBusinessMessageBulk().checkError();
        return response;
    }

    public String fetchChallenges(GenerateChallangeRequest challangeRequest) {
        ChallengeResponse response =
                getRequestBuilderWithCookies(Urls.GENERATE_CHALLENGES)
                        .post(
                                ChallengeResponse.class,
                                SerializationUtils.serializeToString(challangeRequest));
        response.getBusinessMessageBulk().checkError();

        List<String> challenges = response.getValue().getChallenges();

        if (challenges.size() > 1) {
            logger.warn(
                    "tag={} Multiple challenges: {}",
                    FortisConstants.LoggingTag.MULTIPLE_CHALLENGES,
                    challenges.toString());
        }

        return challenges.get(0);
    }

    public PrepareContractUpdateResponse prepareContractUpdate(
            PrepareContractUpdateRequest contractUpdateRequest) {
        PrepareContractUpdateResponse response =
                getRequestBuilderWithCookies(Urls.PREPARE_CONTRACT_UPDATE)
                        .post(
                                PrepareContractUpdateResponse.class,
                                SerializationUtils.serializeToString(contractUpdateRequest));
        response.getBusinessMessageBulk().checkError();
        return response;
    }

    public ExecuteContractUpdateResponse executeContractUpdate(
            ExecuteContractUpdateRequest request) {
        ExecuteContractUpdateResponse response =
                getRequestBuilderWithCookies(Urls.EXECUTE_CONTRACT_UPDATE)
                        .post(
                                ExecuteContractUpdateResponse.class,
                                SerializationUtils.serializeToString(request));
        response.getBusinessMessageBulk().checkError();
        return response;
    }

    public UserInfoResponse getUserInfo() {
        return getRequestBuilderWithCookies(FortisConstants.Urls.GET_USER_INFO)
                .post(UserInfoResponse.class);
    }

    public HttpResponse authenticationRequest(String loginChallenge) {
        return getRequestBuilderWithCookies(FortisConstants.Urls.AUTHENTICATION_URL)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(HttpResponse.class, loginChallenge);
    }

    public void logout() {
        client.request(getUrl(FortisConstants.Urls.LOGOUT))
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(FortisConstants.Headers.CSRF, CSRF);
    }

    public void getDistributorAuthenticationMeans() {
        DistributorAuthenticationRequest request =
                new DistributorAuthenticationRequest(
                        "",
                        FortisConstants.AuthenticationMeans.DISTRIBUTION_CHANNEL_ID,
                        FortisConstants.AuthenticationMeans.MINIMUM_DAC_LEVEL,
                        distributorId);

        getRequestBuilderWithCookies(FortisConstants.Urls.GET_DISTRIBUTOR_AUTHENTICATION_MEANS)
                .post(SerializationUtils.serializeToString(request));
    }
}
