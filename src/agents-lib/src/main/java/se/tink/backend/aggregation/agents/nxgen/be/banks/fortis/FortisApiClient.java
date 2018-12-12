package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.CheckForcedUpgradeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.*;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.*;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;
import se.tink.libraries.serialization.utils.SerializationUtils;

import javax.ws.rs.core.MediaType;
import java.util.List;

public class FortisApiClient {

    private final TinkHttpClient client;
    private final String CSRF = FortisUtils.generateCSRF();
    private static final AggregationLogger LOGGER = new AggregationLogger(FortisApiClient.class);
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
        String mozillaVersion = DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getMozillaVersion();
        String iphoneModel = DeviceProfileConfiguration.IOS_STABLE.getModelNumber();
        String iOSVersion = DeviceProfileConfiguration.IOS_STABLE.getOsVersion();
        String appleWebKit = DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getPlatform();
        String platformDetails = DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getPlatformDetails();
        String extra = String
                .format("Mobile/7D11 FAT/ APPTYPE=001/ APPVERSION=%s/OS=ios-phone", FortisConstants.APP_VERSION);

        return String.format("%s (%s; U;iOS %s; en-us) %s %s %s", mozillaVersion, iphoneModel, iOSVersion, appleWebKit,
                platformDetails, extra);
    }

    private void checkForcedUpgrade(String distributorId) {
        CheckForcedUpgradeRequest request = new CheckForcedUpgradeRequest(distributorId);
        getRequestBuilderWithCookies(FortisConstants.URLS.CHECK_FORCED_UPGRADE)
                .post(HttpResponse.class, SerializationUtils.serializeToString(request));
    }

    private RequestBuilder getRequestBuilderWithCookies(String resource) {
        return client.request(getUrl(resource))
                .cookie(FortisConstants.COOKIE.CSRF, CSRF)
                .cookie(FortisConstants.COOKIE.AXES, FortisUtils.generateAxes())
                .cookie(FortisConstants.COOKIE.DEVICE_FEATURES, FortisConstants.HEADER_VALUES.DEVICE_FEATURES_VALUE)
                .cookie(FortisConstants.COOKIE.DISTRIBUTOR_ID, distributorId)
                .cookie(FortisConstants.COOKIE.EUROPOLICY, FortisConstants.COOKIE.EUROPOLICY_OPTIN)
                .header(FortisConstants.HEADERS.CSRF, CSRF)
                .type(MediaType.APPLICATION_JSON_TYPE);
    }

    private URL getUrl(String resource) {
        return new URL(String.format("%s%s", baseUrl, resource));
    }

    public EbankingUsersResponse getEBankingUsers(EBankingUsersRequest eBankingUsersRequest) {
        // These two calls MUST be made in this order. Otherwise correct cookies will not be set!
        checkForcedUpgrade(eBankingUsersRequest.getDistributorId());
        getDistributorAuthenticationMeans();

        return getRequestBuilderWithCookies(FortisConstants.URLS.GET_E_BANKING_USERS)
                .post(EbankingUsersResponse.class, SerializationUtils.serializeToString(eBankingUsersRequest));
    }

    public AuthenticationProcessResponse createAuthenticationProcess(
            AuthenticationProcessRequest authenticationProcessRequest) {
        return getRequestBuilderWithCookies(FortisConstants.URLS.CREATE_AUTHENTICATION_PROCESS)
                .post(AuthenticationProcessResponse.class,
                        SerializationUtils.serializeToString(authenticationProcessRequest));
    }

    public AccountsResponse fetchAccounts() {
        return getRequestBuilderWithCookies(FortisConstants.URLS.GET_VIEW_ACCOUNT_LIST)
                .post(AccountsResponse.class, SerializationUtils.serializeToString(new AccountsRequest()));
    }

    public TransactionsResponse fetchTransactions(int page, String accountProductId) {
        TransactionsRequest request = new TransactionsRequest(accountProductId, page);
        return getRequestBuilderWithCookies(FortisConstants.URLS.TRANSACTIONS)
                .post(TransactionsResponse.class, SerializationUtils.serializeToString(request));
    }

    public UpcomingTransactionsResponse fetchUpcomingTransactions(int page, String accountProductId) {
        TransactionsRequest request = new TransactionsRequest(accountProductId, page);
            return getRequestBuilderWithCookies(FortisConstants.URLS.UPCOMING_TRANSACTIONS)
                    .post(UpcomingTransactionsResponse.class, SerializationUtils.serializeToString(request));

    }

    public String fetchChallenges(GenerateChallangeRequest challangeRequest) {
        List<String> challenges = getRequestBuilderWithCookies(FortisConstants.URLS.GENERATE_CHALLENGES)
                .post(ChallengeResponse.class, SerializationUtils.serializeToString(challangeRequest)).getValue()
                .getChallenges();

        if (challenges.size() > 1) {
            LOGGER.warnExtraLong(String.format("Multiple challanges: %s", challenges.toString()),
                    FortisConstants.LOGTAG.MULTIPLE_CHALLENGES);
        }

        return challenges.get(0);
    }

    public UserInfoResponse getUserInfo() {
        return getRequestBuilderWithCookies(FortisConstants.URLS.GET_USER_INFO).post(UserInfoResponse.class);
    }

    public HttpResponse authenticationRequest(String loginChallenge) {
        return getRequestBuilderWithCookies(FortisConstants.URLS.AUTHENTICATION_URL)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(HttpResponse.class, loginChallenge);
    }

    public void logout() {
        client.request(getUrl(FortisConstants.URLS.LOGOUT))
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(FortisConstants.HEADERS.CSRF, CSRF);
    }

    public void getDistributorAuthenticationMeans() {
        DistributorAuthenticationRequest request = new DistributorAuthenticationRequest("",
                FortisConstants.AUTHENTICATION_MEANS.DISTRIBUTION_CHANNEL_ID,
                FortisConstants.AUTHENTICATION_MEANS.MINIMUM_DAC_LEVEL,
                distributorId);

        getRequestBuilderWithCookies(FortisConstants.URLS.GET_DISTRIBUTOR_AUTHENTICATION_MEANS)
                .post(SerializationUtils.serializeToString(request));
    }

}
