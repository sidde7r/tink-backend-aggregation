package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.AccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.UpcomingTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper.FortisRandomTokenGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FortisApiClient {

    private final TinkHttpClient client;
    private final FortisRandomTokenGenerator fortisRandomTokenGenerator;
    private final String csrf;
    private final String baseUrl;
    private final String distributorId;

    public FortisApiClient(
            TinkHttpClient client,
            String baseUrl,
            String distributorId,
            FortisRandomTokenGenerator fortisRandomTokenGenerator) {
        this.client = client;
        this.baseUrl = baseUrl;
        this.distributorId = distributorId;
        this.fortisRandomTokenGenerator = fortisRandomTokenGenerator;
        this.csrf = fortisRandomTokenGenerator.generateCSRF();
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

    private RequestBuilder getRequestBuilderWithCookies(String resource) {
        return client.request(getUrl(resource))
                .cookie(FortisConstants.Cookie.CSRF, csrf)
                .cookie(FortisConstants.Cookie.AXES, fortisRandomTokenGenerator.generateAxes())
                .cookie(
                        FortisConstants.Cookie.DEVICE_FEATURES,
                        FortisConstants.HeaderValues.DEVICE_FEATURES_VALUE)
                .cookie(FortisConstants.Cookie.DISTRIBUTOR_ID, distributorId)
                .cookie(FortisConstants.Cookie.EUROPOLICY, FortisConstants.Cookie.EUROPOLICY_OPTIN)
                .header(FortisConstants.Headers.CSRF, csrf)
                .header(FortisConstants.Headers.USER_AGENT, getUserAgent())
                .type(MediaType.APPLICATION_JSON_TYPE);
    }

    private URL getUrl(String resource) {
        return new URL(String.format("%s%s", baseUrl, resource));
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
}
