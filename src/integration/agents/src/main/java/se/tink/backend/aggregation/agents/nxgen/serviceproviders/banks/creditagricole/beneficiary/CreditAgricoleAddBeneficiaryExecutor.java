package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AccessibilityGridResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AuthenticateRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.OtpAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.rpc.IbanValidationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.utils.CreditAgricoleAuthUtil;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;
import se.tink.libraries.payment.rpc.Beneficiary;

public class CreditAgricoleAddBeneficiaryExecutor implements CreateBeneficiaryExecutor {
    private final CreditAgricoleApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final PersistentStorage persistentStorage;
    private CreateBeneficiaryResponse createBeneficiaryResponse;

    public CreditAgricoleAddBeneficiaryExecutor(
            CreditAgricoleApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public CreateBeneficiaryResponse createBeneficiary(
            CreateBeneficiaryRequest createBeneficiaryRequest) throws BeneficiaryException {
        createBeneficiaryResponse =
                new CreateBeneficiaryResponse(createBeneficiaryRequest.getBeneficiary());
        createBeneficiaryResponse.getBeneficiary().setStatus(CreateBeneficiaryStatus.INITIATED);

        return createBeneficiaryResponse;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse sign(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryException, AuthenticationException {
        switch (createBeneficiaryMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                return init(createBeneficiaryMultiStepRequest);
            case CreditAgricoleConstants.Step.AUTHORIZE:
                return authorized(createBeneficiaryMultiStepRequest);
            case CreditAgricoleConstants.Step.ADD_BENEFICIARY:
                return addBeneficiary(createBeneficiaryMultiStepRequest);
            default:
                throw new IllegalStateException(
                        String.format(
                                "Unknown step %s", createBeneficiaryMultiStepRequest.getStep()));
        }
    }

    private CreateBeneficiaryMultiStepResponse init(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryAuthorizationException {
        switch (createBeneficiaryMultiStepRequest.getBeneficiary().getStatus()) {
            case INITIATED:
                return new CreateBeneficiaryMultiStepResponse(
                        createBeneficiaryMultiStepRequest,
                        CreditAgricoleConstants.Step.AUTHORIZE,
                        new ArrayList<>());
            case REJECTED:
                throw new BeneficiaryAuthorizationException(
                        "Request to add beneficiary was rejected.",
                        new IllegalStateException("Beneficiary rejected."));
            default:
                throw new IllegalStateException(
                        String.format(
                                "Unknown status %s",
                                createBeneficiaryMultiStepRequest.getBeneficiary().getStatus()));
        }
    }

    private CreateBeneficiaryMultiStepResponse authorized(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryException {

        initBeneficiaryAuthentication();

        DefaultResponse defaultResponse = apiClient.otpInit();
        if (!defaultResponse.isResponseOK()) {
            throw new BeneficiaryException("Unknown error: " + defaultResponse.getErrorString());
        }

        String beneficiaryOtp = getBeneficiaryOtp();

        OtpAuthenticationResponse otpAuthenticationResponse =
                apiClient.otpAuthenticate(beneficiaryOtp);
        if (!otpAuthenticationResponse.isResponseOK()) {
            return new CreateBeneficiaryMultiStepResponse(
                    createBeneficiaryMultiStepRequest,
                    CreditAgricoleConstants.Step.AUTHORIZE,
                    new ArrayList<>());
        }
        this.createBeneficiaryResponse.getBeneficiary().setStatus(CreateBeneficiaryStatus.SIGNED);
        return new CreateBeneficiaryMultiStepResponse(
                createBeneficiaryMultiStepRequest,
                CreditAgricoleConstants.Step.ADD_BENEFICIARY,
                new ArrayList<>());
    }

    private CreateBeneficiaryMultiStepResponse addBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryException {
        Beneficiary beneficiary =
                createBeneficiaryMultiStepRequest.getBeneficiary().getBeneficiary();
        IbanValidationResponse ibanValidationResponse =
                apiClient.validateIban(beneficiary.getAccountNumber());
        if (!ibanValidationResponse.isResponseOK()) {
            throw new BeneficiaryException(
                    "Validate Iban failed; probably supplied with bad iban: "
                            + ibanValidationResponse.getErrorString());
        }

        // TODO: differentiate if the error is adding an account that is already trusted.
        DefaultResponse addBeneficiaryResponse =
                apiClient.addBeneficiary(
                        beneficiary.getName(),
                        beneficiary.getAccountNumber(),
                        ibanValidationResponse.getBic());
        if (!addBeneficiaryResponse.isResponseOK()) {
            throw new BeneficiaryException(
                    "addBeneficiary failed: " + addBeneficiaryResponse.getErrorString());
        }

        CreateBeneficiaryMultiStepResponse createBeneficiaryMultiStepResponse =
                new CreateBeneficiaryMultiStepResponse(
                        createBeneficiaryMultiStepRequest,
                        AuthenticationStepConstants.STEP_FINALIZE,
                        new ArrayList<>());

        createBeneficiaryMultiStepResponse
                .getBeneficiary()
                .setStatus(CreateBeneficiaryStatus.CREATED);
        return createBeneficiaryMultiStepResponse;
    }

    private void initBeneficiaryAuthentication() throws BeneficiaryException {
        AccessibilityGridResponse accessibilityGrid = apiClient.getAccessibilityGrid();
        RSAPublicKey publicKey = preparePublicKey(accessibilityGrid.getPublicKey());
        String mappedAccountCode = mapAccountCode(accessibilityGrid.getSequence());
        AuthenticateRequest request = createPrimaryAuthRequest(mappedAccountCode, publicKey);

        AuthenticateResponse response = apiClient.authenticate(request);
        if (!response.isResponseOK()) {
            throw new BeneficiaryException("Unknown error: " + response.getErrorString());
        }
    }

    private RSAPublicKey preparePublicKey(String publicKey) {
        persistentStorage.put(CreditAgricoleConstants.StorageKey.PUBLIC_KEY, publicKey);
        return CreditAgricoleAuthUtil.getPublicKey(publicKey);
    }

    private String mapAccountCode(String numpadSequence) {
        return CreditAgricoleAuthUtil.mapAccountCodeToNumpadSequence(
                numpadSequence,
                persistentStorage.get(CreditAgricoleConstants.StorageKey.USER_ACCOUNT_CODE));
    }

    private AuthenticateRequest createPrimaryAuthRequest(
            String mappedAccountCode, RSAPublicKey publicKey) {
        return AuthenticateRequest.createPrimaryAuthRequest(
                CreditAgricoleAuthUtil.createEncryptedAccountCode(mappedAccountCode, publicKey),
                persistentStorage.get(CreditAgricoleConstants.StorageKey.USER_ID),
                persistentStorage.get(CreditAgricoleConstants.StorageKey.USER_ACCOUNT_NUMBER));
    }

    private String getBeneficiaryOtp() throws BeneficiaryException {
        try {
            return supplementalInformationHelper.waitForOtpInput();
        } catch (SupplementalInfoException e) {
            throw new BeneficiaryException(e.getMessage(), e);
        }
    }
}
