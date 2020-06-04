package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.rpc.AddBeneficiaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.rpc.IbanValidationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.rpc.IbanValidationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.rpc.ContractsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.rpc.OperationsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CreditAgricoleApiClient {
    private static final Logger log = LoggerFactory.getLogger(CreditAgricoleApiClient.class);
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public CreditAgricoleApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.client.disableSignatureRequestHeader();
    }

    public AccessibilityGridResponse getAccessibilityGrid() {
        return createRequest(Url.ACCESSIBILITY_GRID).get(AccessibilityGridResponse.class);
    }

    public CreateUserResponse createUser(CreateUserRequest request) {
        CreateUserResponse createUserResponse =
                createAuthRequest().post(CreateUserResponse.class, request);
        if (createUserResponse.getAllErrorCodes().contains("fr.mabanque.createuser.scarequired")) {
            // Happy path! They use exception driven development...
            return createUserResponse;
        }
        if (createUserResponse.getAllErrorCodes().contains("fr.mabanque.auth.generic")) {
            log.info("[createUser] Unexpected error: {}", createUserResponse.getErrors());
            throw BankServiceError.NO_BANK_SERVICE.exception();
        }
        if (!createUserResponse.isResponseOK()) {
            log.info("[createUser] Unexpected error: {}", createUserResponse.getErrors());
            throw new IllegalStateException("[createUser] did not succeed!");
        }
        return createUserResponse;
    }

    public DefaultResponse requestOtp(DefaultAuthRequest request) throws LoginException {
        URL url =
                new URL(Url.OTP_REQUEST)
                        .parameter(
                                StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID));
        DefaultResponse defaultResponse = createRequest(url).post(DefaultResponse.class, request);

        if (defaultResponse.getAllErrorCodes().contains("BamAuthenticationRequired")) {
            throw LoginError.NOT_CUSTOMER.exception(
                    "Wrong branch used, please verify that you have chosen the correct branch.");
        }
        if (!defaultResponse.isResponseOK()) {
            log.info("[requestOtp] Unexpected error: {}", defaultResponse.getErrors());
            throw new IllegalStateException("[requestOtp] did not succeed!");
        }
        return defaultResponse;
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

    public void otpInit() {
        OtpInitRequest otpInitRequest =
                new OtpInitRequest(
                        Integer.parseInt(persistentStorage.get(StorageKey.USER_ID)),
                        persistentStorage.get(StorageKey.PARTNER_ID));

        DefaultResponse otpInitResponse =
                createRequest(
                                new URL(Url.OTP_REQUEST)
                                        .parameter(
                                                StorageKey.REGION_ID,
                                                persistentStorage.get(StorageKey.REGION_ID)))
                        .body(otpInitRequest, MediaType.APPLICATION_JSON_TYPE)
                        .post(DefaultResponse.class);
        if (!otpInitResponse.isResponseOK()) {
            log.info("[otpInit] Unknown error: {}", otpInitResponse.getErrors());
            throw new IllegalStateException("Unknown error when initializing OTP authentication");
        }
    }

    public void otpAuthenticate(String otp) throws SupplementalInfoException {
        OtpAuthenticationRequest otpAuthenticationRequest =
                new OtpAuthenticationRequest(
                        persistentStorage.get(StorageKey.USER_ID),
                        persistentStorage.get(StorageKey.PARTNER_ID),
                        otp);
        OtpAuthenticationResponse otpAuthenticationResponse =
                createAuthRequest()
                        .body(otpAuthenticationRequest, MediaType.APPLICATION_JSON_TYPE)
                        .post(OtpAuthenticationResponse.class);
        if (!otpAuthenticationResponse.isResponseOK()) {
            log.info(
                    "[otpAuthenticate]: Unknown error (probably invalid code): {}",
                    otpAuthenticationResponse.getErrors());
            throw new SupplementalInfoException(SupplementalInfoError.NO_VALID_CODE);
        }
    }

    public IbanValidationResponse validateIban(String iban) {
        IbanValidationRequest ibanValidationRequest = new IbanValidationRequest(iban);
        IbanValidationResponse ibanValidationResponse =
                createRequest(
                                Url.VALIDATE_IBAN
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
                        .body(ibanValidationRequest, MediaType.APPLICATION_JSON_TYPE)
                        .post(IbanValidationResponse.class);
        if (!ibanValidationResponse.isResponseOK()) {
            log.info("[ValidateIban] Unknown error: {}", ibanValidationResponse.getErrors());
            throw new IllegalStateException(
                    "Validate Iban failed; probably supplied with bad iban");
        }
        return ibanValidationResponse;
    }

    public void addBeneficiary(String label, String iban, String bic) {
        AddBeneficiaryRequest addBeneficiaryRequest = new AddBeneficiaryRequest(label, iban, bic);
        AddBeneficiaryResponse addBeneficiaryResponse =
                createRequest(
                                Url.ADD_BENEFICIARY
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
                        .body(addBeneficiaryRequest, MediaType.APPLICATION_JSON_TYPE)
                        .post(AddBeneficiaryResponse.class);
        // TODO: differentiate if the error is adding an account that is already trusted.
        if (!addBeneficiaryResponse.isResponseOK()) {
            log.info("[addBeneficiary] Unknown error: {}", addBeneficiaryResponse.getErrors());
            throw new IllegalStateException("addBeneficiary: something unexpected went wrong.");
        }
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
                                persistentStorage.get(StorageKey.PROFILE_PIN)));
    }

    private String base64(String string) {
        return EncodingUtils.encodeAsBase64String(string);
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
