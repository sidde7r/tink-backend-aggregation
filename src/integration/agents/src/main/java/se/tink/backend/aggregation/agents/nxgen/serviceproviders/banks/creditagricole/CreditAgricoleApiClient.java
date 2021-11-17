package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.Authorization;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AccessibilityGridResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AuthenticateRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.CreateProfileForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.CreateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.CreateUserRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.CreateUserResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.DefaultAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.FindProfilesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.OtpAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.OtpAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.OtpAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.OtpInitRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.OtpSmsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.RestoreProfileForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.rpc.AddBeneficiaryRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.rpc.IbanValidationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.rpc.IbanValidationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.rpc.ContractsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.rpc.OperationsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.encoding.EncodingUtils;

public class CreditAgricoleApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    CreditAgricoleApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.client.disableSignatureRequestHeader();
    }

    public AccessibilityGridResponse getAccessibilityGrid() {
        return createRequest(new URL(Url.ACCESSIBILITY_GRID)).get(AccessibilityGridResponse.class);
    }

    public CreateUserResponse createUser(CreateUserRequest request) {
        return createAuthRequest().post(CreateUserResponse.class, request);
    }

    public DefaultResponse requestOtp(DefaultAuthRequest request) {
        URL url =
                new URL(Url.OTP_REQUEST)
                        .parameter(
                                StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID));
        return createRequest(url).post(DefaultResponse.class, request);
    }

    public OtpAuthResponse sendOtpCode(OtpSmsRequest request) {
        return createAuthRequest().post(OtpAuthResponse.class, request);
    }

    public FindProfilesResponse findProfiles() {
        URL url =
                new URL(Url.FIND_PROFILES)
                        .parameter(
                                StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID));
        return createRequest(url).get(FindProfilesResponse.class);
    }

    public CreateProfileResponse createProfile(CreateProfileForm request) {
        URL url =
                new URL(Url.CREATE_PROFILE)
                        .parameter(
                                StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID));

        return createRequest(url)
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .post(CreateProfileResponse.class, request);
    }

    public DefaultResponse restoreProfile(RestoreProfileForm request) {
        URL url =
                Url.RESTORE_PROFILE
                        .parameter(StorageKey.USER_ID, persistentStorage.get(StorageKey.USER_ID))
                        .parameter(
                                StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID))
                        .parameter(
                                StorageKey.PARTNER_ID,
                                persistentStorage.get(StorageKey.PARTNER_ID));

        return createRequest(url)
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .post(DefaultResponse.class);
    }

    public AuthenticateResponse authenticate(AuthenticateRequest request) {
        return createAuthRequest().post(AuthenticateResponse.class, request);
    }

    public DefaultResponse otpInit() {
        OtpInitRequest otpInitRequest =
                new OtpInitRequest(
                        Integer.parseInt(persistentStorage.get(StorageKey.USER_ID)),
                        persistentStorage.get(StorageKey.PARTNER_ID));

        URL url =
                new URL(Url.OTP_REQUEST)
                        .parameter(
                                StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID));
        return createRequest(url)
                .body(otpInitRequest, MediaType.APPLICATION_JSON_TYPE)
                .post(DefaultResponse.class);
    }

    public OtpAuthenticationResponse otpAuthenticate(String otp) {
        OtpAuthenticationRequest otpAuthenticationRequest =
                new OtpAuthenticationRequest(
                        persistentStorage.get(StorageKey.USER_ID),
                        persistentStorage.get(StorageKey.PARTNER_ID),
                        otp);
        return createAuthRequest()
                .body(otpAuthenticationRequest, MediaType.APPLICATION_JSON_TYPE)
                .post(OtpAuthenticationResponse.class);
    }

    public IbanValidationResponse validateIban(String iban) {
        IbanValidationRequest ibanValidationRequest = new IbanValidationRequest(iban);

        URL url =
                Url.VALIDATE_IBAN
                        .parameter(StorageKey.USER_ID, persistentStorage.get(StorageKey.USER_ID))
                        .parameter(
                                StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID))
                        .parameter(
                                StorageKey.PARTNER_ID,
                                persistentStorage.get(StorageKey.PARTNER_ID));
        return createRequestWithAuthHeader(url)
                .body(ibanValidationRequest, MediaType.APPLICATION_JSON_TYPE)
                .post(IbanValidationResponse.class);
    }

    public DefaultResponse addBeneficiary(String label, String iban, String bic) {
        AddBeneficiaryRequest addBeneficiaryRequest = new AddBeneficiaryRequest(label, iban, bic);

        URL url =
                Url.ADD_BENEFICIARY
                        .parameter(StorageKey.USER_ID, persistentStorage.get(StorageKey.USER_ID))
                        .parameter(
                                StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID))
                        .parameter(
                                StorageKey.PARTNER_ID,
                                persistentStorage.get(StorageKey.PARTNER_ID));
        return createRequestWithAuthHeader(url)
                .body(addBeneficiaryRequest, MediaType.APPLICATION_JSON_TYPE)
                .post(DefaultResponse.class);
    }

    public ContractsResponse contracts() {
        URL url =
                Url.CONTRACTS
                        .parameter(StorageKey.USER_ID, persistentStorage.get(StorageKey.USER_ID))
                        .parameter(
                                StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID))
                        .parameter(
                                StorageKey.PARTNER_ID,
                                persistentStorage.get(StorageKey.PARTNER_ID));

        return createRequestWithAuthHeader(url).get(ContractsResponse.class);
    }

    public OperationsResponse operations(String accountNumber) {
        URL url =
                Url.OPERATIONS
                        .parameter(StorageKey.USER_ID, persistentStorage.get(StorageKey.USER_ID))
                        .parameter(
                                StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID))
                        .parameter(
                                StorageKey.PARTNER_ID, persistentStorage.get(StorageKey.PARTNER_ID))
                        .parameter(StorageKey.ACCOUNT_NUMBER, accountNumber);

        return createRequestWithAuthHeader(url).get(OperationsResponse.class);
    }

    private RequestBuilder createAuthRequest() {
        URL url =
                new URL(Url.AUTHENTICATE)
                        .parameter(
                                StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID));
        return createRequest(url);
    }

    private RequestBuilder createRequestWithAuthHeader(URL url) {
        return createRequest(url).header(Authorization.HEADER, basicAuth());
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

    private String basicAuth() {
        String value =
                persistentStorage.get(StorageKey.USER_ID)
                        + ":"
                        + persistentStorage.get(StorageKey.PROFILE_PIN);

        return Authorization.BASIC_PREFIX + EncodingUtils.encodeAsBase64String(value);
    }
}
