package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc;

import com.google.common.base.Preconditions;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.DeviceToken;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcDevice;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.AgreementsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.InitSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.PinDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.PinLogonRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.SdcAgreementServiceConfigurationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.SendOTPRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.SignOTPRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcCustodyDetailsModel;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.CustodyContentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.CustodyOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.ListCreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.ListLoanAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SearchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SelectAgreementRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.totalkredit.TotalKreditLoanResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.Catalog;
import src.integration.nemid.NemIdSupportedLanguageCode;

// Sdc has two version of its api, set as a http header
public class SdcApiClient {

    private final TinkHttpClient client;
    private final SdcConfiguration agentConfiguration;
    private final String languageCode;
    private DeviceToken deviceToken;

    public SdcApiClient(
            TinkHttpClient client, SdcConfiguration agentConfiguration, Catalog catalog) {
        this.client = client;
        this.agentConfiguration = agentConfiguration;
        this.languageCode =
                NemIdSupportedLanguageCode.getFromCatalogOrDefault(catalog).getIsoLanguageCode();
    }

    public void setDeviceToken(DeviceToken deviceToken) {
        this.deviceToken = deviceToken;
    }

    public void initSession() {
        InitSessionRequest initSessionRequest = new InitSessionRequest();

        createApiRequest(this.agentConfiguration.getInitSessionUrl()).post(initSessionRequest);
    }

    public void initBankId(String ssn) {
        InitBankIdRequest initBankIdRequest = new InitBankIdRequest().setSsn(ssn);

        createApiRequest(this.agentConfiguration.getBankIdLoginUrl()).post(initBankIdRequest);
    }

    public AgreementsResponse pinLogon(String username, String password) {
        PinLogonRequest pinLogonRequest =
                new PinLogonRequest().setUserId(username).setPin(password);

        return createApiRequest(
                        this.agentConfiguration.getPinLogonUrl(),
                        SdcConstants.Headers.API_VERSION_3)
                .post(AgreementsResponse.class, pinLogonRequest);
    }

    public ChallengeResponse getChallenge() {
        HttpResponse response =
                createApiRequest(this.agentConfiguration.getChallengeUrl()).get(HttpResponse.class);

        return response.getBody(ChallengeResponse.class);
    }

    public HttpResponse pinDevice(SdcDevice device, String phoneNumber) {
        PinDeviceRequest request =
                new PinDeviceRequest()
                        .setPublicKey(device.getPublicKey())
                        .setDeviceId(device.getDeviceId())
                        .setDeviceName(SdcConstants.Session.MODEL)
                        .setPhoneNumber(phoneNumber);

        return createApiRequest(
                        this.agentConfiguration.getPinDeviceUrl(),
                        SdcConstants.Headers.API_VERSION_2)
                .post(HttpResponse.class, request);
    }

    public void sendOTPRequest(String transId) {
        SendOTPRequest request = new SendOTPRequest().setTransId(transId);

        createApiRequest(this.agentConfiguration.getSendOTPRequestUrl()).post(request);
    }

    public void signOTP(String transId, String otp, String pin) {
        SignOTPRequest request = new SignOTPRequest().setTransId(transId).setOtp(otp).setPin(pin);

        createApiRequest(this.agentConfiguration.getSignOTPUrl()).post(request);
    }

    public void logout() {
        createApiRequest(this.agentConfiguration.getLogoutUrl()).get(HttpResponse.class);
    }

    public AgreementsResponse fetchAgreements() {
        return createApiRequest(this.agentConfiguration.getAgreementsUrl())
                .get(AgreementsResponse.class);
    }

    public SdcAgreementServiceConfigurationResponse selectAgreement(
            SelectAgreementRequest selectAgreementRequest) {
        Preconditions.checkNotNull(selectAgreementRequest);

        HttpResponse response = internalSelectAgreement(selectAgreementRequest);

        return response.getBody(SdcAgreementServiceConfigurationResponse.class);
    }

    public HttpResponse internalSelectAgreement(SelectAgreementRequest selectAgreementRequest) {
        Preconditions.checkNotNull(selectAgreementRequest);

        return createApiRequest(
                        this.agentConfiguration.getSelectAgreementUrl(),
                        SdcConstants.Headers.API_VERSION_2)
                .post(HttpResponse.class, selectAgreementRequest);
    }

    public FilterAccountsResponse filterAccounts(FilterAccountsRequest filterRequest) {
        // currently only used to see if session is still alive
        return createApiRequest(this.agentConfiguration.getFilterAccountsUrl())
                .post(FilterAccountsResponse.class, filterRequest);
    }

    public ListCreditCardsResponse listCreditCards() {
        return createApiRequest(this.agentConfiguration.getListCreditCardsUrl())
                .get(ListCreditCardsResponse.class);
    }

    public SearchTransactionsResponse searchTransactions(
            SearchTransactionsRequest searchTransactionsRequest) {
        return createApiRequest(
                        this.agentConfiguration.getSearchTransactionsUrl(),
                        SdcConstants.Headers.API_VERSION_5)
                .post(SearchTransactionsResponse.class, searchTransactionsRequest);
    }

    public SearchTransactionsResponse searchCreditCardTransactions(
            SearchTransactionsRequest searchTransactionsRequest) {
        return createApiRequest(this.agentConfiguration.getSearchCreditCardTransactionsUrl())
                .post(SearchTransactionsResponse.class, searchTransactionsRequest);
    }

    public ListLoanAccountsResponse listLoans() {
        return createApiRequest(this.agentConfiguration.getListLoansUrl())
                .get(ListLoanAccountsResponse.class);
    }

    public TotalKreditLoanResponse listTotalKreditLoans() {
        return createApiRequest(agentConfiguration.getUrlForTotalKreditLoans())
                .overrideHeader(SdcConstants.Headers.X_SDC_LOCALE, NemIdSupportedLanguageCode.DA)
                .get(TotalKreditLoanResponse.class);
    }

    public FilterAccountsResponse listCreditCardProviderAccounts() {
        return createApiRequest(this.agentConfiguration.getListCreditCardProviderAccountsUrl())
                .get(FilterAccountsResponse.class);
    }

    // this is temporary, log until we have more data, then implement this method.!!!!
    public CustodyOverviewResponse fetchCustodyOverview() {
        return createApiRequest(this.agentConfiguration.getCustodyOverviewUrl())
                .get(CustodyOverviewResponse.class);
    }

    // this is temporary, log until we have more data, then implement this method.!!!!
    public CustodyContentResponse fetchCustodyContent(SdcCustodyDetailsModel custodyDetails) {
        return createApiRequest(this.agentConfiguration.getCustodyContentUrl(custodyDetails))
                .get(CustodyContentResponse.class);
    }

    // helper for setting up a request with all headers
    private RequestBuilder createApiRequest(URL url) {
        return createApiRequest(url, SdcConstants.Headers.API_VERSION_1);
    }

    private RequestBuilder createApiRequest(URL url, String apiVersion) {
        RequestBuilder builder =
                this.client
                        .request(url)
                        .header(SdcConstants.Headers.X_SDC_API_VERSION, apiVersion)
                        .header(
                                SdcConstants.Headers.X_SDC_CLIENT_TYPE,
                                SdcConstants.Headers.CLIENT_TYPE)
                        .header(
                                SdcConstants.Headers.X_SDC_PORTLET_PATH,
                                SdcConstants.Headers.PORTLET_PATH)
                        .header(SdcConstants.Headers.X_SDC_LOCALE, languageCode)
                        .accept(MediaType.WILDCARD)
                        .type(MediaType.APPLICATION_JSON);
        if (deviceToken != null) {
            builder.header(SdcConstants.Headers.X_SDC_DEVICE_TOKEN, deviceToken.signToken());
        }

        return builder;
    }
}
