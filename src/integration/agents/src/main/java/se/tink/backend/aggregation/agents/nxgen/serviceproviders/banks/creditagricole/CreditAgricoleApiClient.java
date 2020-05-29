package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.Authorization;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.QueryParam;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AccessibilityGridResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AppCodeForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AuthenticateRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.CreateProfileRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.CreateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.CreateUserRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.CreateUserResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.DefaultAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.FindProfilesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.OtpAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.OtpSmsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.SignInResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.StrongAuthenticationForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.StrongAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.rpc.ContractsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.rpc.OperationsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CreditAgricoleApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public CreditAgricoleApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    public byte[] numberPad() {
        return client.request(Url.NUMBER_PAD).accept(MediaType.WILDCARD).get(byte[].class);
    }

    public SignInResponse signIn(String userAccountNumber, String userAccountCode) {
        return client.request(
                        Url.SIGN_IN
                                .parameter(
                                        StorageKey.REGION_ID,
                                        persistentStorage.get(StorageKey.REGION_ID))
                                .queryParam(QueryParam.USER_ACCOUNT_CODE, userAccountCode)
                                .queryParam(QueryParam.USER_ACCOUNT_NUMBER, userAccountNumber))
                .accept(MediaType.APPLICATION_JSON)
                .get(SignInResponse.class);
    }

    public DefaultResponse restoreProfile(String appCode) {
        AppCodeForm appCodeForm =
                new AppCodeForm()
                        .setUserAccountCode(
                                sessionStorage.get(StorageKey.SHUFFLED_USER_ACCOUNT_CODE))
                        .setUserAccountNumber(persistentStorage.get(StorageKey.USER_ACCOUNT_NUMBER))
                        .setAppCode(appCode);

        return client.request(
                        Url.APP_CODE
                                .parameter(
                                        StorageKey.USER_ID,
                                        persistentStorage.get(StorageKey.USER_ID))
                                .parameter(
                                        StorageKey.REGION_ID,
                                        persistentStorage.get(StorageKey.REGION_ID))
                                .parameter(
                                        StorageKey.PARTNER_ID,
                                        persistentStorage.get(StorageKey.PARTNER_ID)))
                .body(appCodeForm, MediaType.APPLICATION_FORM_URLENCODED)
                .post(DefaultResponse.class);
    }

    public StrongAuthenticationResponse strongAuthentication() {
        StrongAuthenticationForm strongAuthenticationForm =
                new StrongAuthenticationForm()
                        .setUserAccountCode(
                                sessionStorage.get(StorageKey.SHUFFLED_USER_ACCOUNT_CODE))
                        .setUserAccountNumber(persistentStorage.get(StorageKey.USER_ACCOUNT_NUMBER))
                        .setLogin(persistentStorage.get(StorageKey.LOGIN_EMAIL));

        return client.request(
                        Url.STRONG_AUTHENTICATION.parameter(
                                StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID)))
                .header(Authorization.HEADER, basicAuth())
                .body(strongAuthenticationForm, MediaType.APPLICATION_FORM_URLENCODED)
                .post(StrongAuthenticationResponse.class);
    }

    /* ACCOUNTS AND TRANSACTIONS */

    public ContractsResponse contracts() {
        return client.request(
                        Url.CONTRACTS
                                .parameter(
                                        StorageKey.USER_ID,
                                        persistentStorage.get(StorageKey.USER_ID))
                                .parameter(
                                        StorageKey.REGION_ID,
                                        persistentStorage.get(StorageKey.REGION_ID))
                                .parameter(
                                        StorageKey.PARTNER_ID,
                                        persistentStorage.get(StorageKey.PARTNER_ID)))
                .header(Authorization.HEADER, basicAuth())
                .get(ContractsResponse.class);
    }

    public OperationsResponse operations(String accountNumber) {
        return client.request(
                        Url.OPERATIONS
                                .parameter(
                                        StorageKey.USER_ID,
                                        persistentStorage.get(StorageKey.USER_ID))
                                .parameter(
                                        StorageKey.REGION_ID,
                                        persistentStorage.get(StorageKey.REGION_ID))
                                .parameter(
                                        StorageKey.PARTNER_ID,
                                        persistentStorage.get(StorageKey.PARTNER_ID))
                                .parameter(StorageKey.ACCOUNT_NUMBER, accountNumber))
                .header(Authorization.HEADER, basicAuth())
                .get(OperationsResponse.class);
    }

    /* HELPER METHODS */

    private String basicAuth() {
        return Authorization.BASIC_PREFIX
                + base64(
                        String.format(
                                "%s:%s",
                                persistentStorage.get(StorageKey.USER_ID),
                                persistentStorage.get(StorageKey.APP_CODE)));
    }

    private String base64(String string) {
        return EncodingUtils.encodeAsBase64String(string);
    }

    public AccessibilityGridResponse getAccessibilityGrid() {
        return createRequest(Url.ACCESSIBILITY_GRID).get(AccessibilityGridResponse.class);
    }

    public CreateUserResponse createUser(CreateUserRequest request) {
        return createAuthRequest().post(CreateUserResponse.class, request);
    }

    public DefaultResponse requestOtp(DefaultAuthRequest request) {
        return createAuthRequest().post(DefaultResponse.class, request);
    }

    public OtpAuthResponse sendOtpCode(OtpSmsRequest request) {
        return createAuthRequest().post(OtpAuthResponse.class, request);
    }

    public FindProfilesResponse findProfiles() {
        return createRequest(Url.FIND_PROFILES).get(FindProfilesResponse.class);
    }

    public CreateProfileResponse createProfile(CreateProfileRequest request) {
        return createRequest(Url.CREATE_PROFILE).post(CreateProfileResponse.class, request);
    }

    public AuthenticateResponse authenticate(AuthenticateRequest request) {
        return createAuthRequest().post(AuthenticateResponse.class, request);
    }

    private RequestBuilder createAuthRequest() {
        URL url =
                new URL(Url.AUTHENTICATE)
                        .parameter(
                                StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID));
        return createRequest(url);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header("Host", "ibudget.iphone.credit-agricole.fr")
                .header(
                        "User-Agent",
                        "MonBudget_iOS/20.1.0.3 iOS/13.3.1 Apple/iPhone9,3 750x1334/2.00")
                .header("Content-Type", "application/json; charset=UTF-8")
                .accept(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequest(String url) {
        return client.request(url)
                .header("Host", "ibudget.iphone.credit-agricole.fr")
                .header(
                        "User-Agent",
                        "MonBudget_iOS/20.1.0.3 iOS/13.3.1 Apple/iPhone9,3 750x1334/2.00")
                .header("Content-Type", "application/json; charset=UTF-8")
                .accept(MediaType.APPLICATION_JSON);
    }
}
