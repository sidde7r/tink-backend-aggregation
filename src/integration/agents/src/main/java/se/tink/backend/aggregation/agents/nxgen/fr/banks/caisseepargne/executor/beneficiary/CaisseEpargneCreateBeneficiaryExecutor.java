package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryInvalidAccountTypeException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryRejectedException;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.Step;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.IdentificationRoutingResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.SamlAuthnResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.rpc.CaisseEpargneCreateBeneficiaryRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.rpc.CaisseEpargneCreateBeneficiaryResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;

public class CaisseEpargneCreateBeneficiaryExecutor implements CreateBeneficiaryExecutor {
    private static final Logger LOG =
            LoggerFactory.getLogger(CaisseEpargneCreateBeneficiaryExecutor.class);

    private final CaisseEpargneApiClient apiClient;
    private CreateBeneficiaryResponse createBeneficiaryResponse;
    private final SupplementalInformationProvider supplementalInformationProvider;
    private CaisseEpargneCreateBeneficiaryResponse apiResponse;
    private final Storage instanceStorage;

    public CaisseEpargneCreateBeneficiaryExecutor(
            CaisseEpargneApiClient apiClient,
            SupplementalInformationProvider supplementalInformationProvider,
            Storage instanceStorage) {
        this.apiClient = apiClient;
        this.supplementalInformationProvider = supplementalInformationProvider;
        this.instanceStorage = instanceStorage;
    }

    @Override
    public CreateBeneficiaryResponse createBeneficiary(
            CreateBeneficiaryRequest createBeneficiaryRequest) throws BeneficiaryException {
        apiClient.getBeneficiaries();
        apiResponse =
                apiClient.createBeneficiary(
                        CaisseEpargneCreateBeneficiaryRequest.of(createBeneficiaryRequest)
                                .orElseThrow(
                                        () ->
                                                new BeneficiaryInvalidAccountTypeException(
                                                        Type.IBAN)));

        createBeneficiaryResponse =
                new CreateBeneficiaryResponse(createBeneficiaryRequest.getBeneficiary());
        if (apiResponse.isNeedExtendedAuthenticationError()) {
            createBeneficiaryResponse.getBeneficiary().setStatus(CreateBeneficiaryStatus.INITIATED);
        } else if (apiResponse.isErrorResponse()) {
            createBeneficiaryResponse.getBeneficiary().setStatus(CreateBeneficiaryStatus.REJECTED);
            LOG.error("Beneficiary request was rejected: {}", apiResponse.getErrorCode());
        } else {
            createBeneficiaryResponse.getBeneficiary().setStatus(CreateBeneficiaryStatus.CREATED);
        }
        return createBeneficiaryResponse;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse sign(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryException, AuthenticationException {
        switch (createBeneficiaryMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                return init(createBeneficiaryMultiStepRequest);
            case Step.AUTHORIZE:
                return authorized(createBeneficiaryMultiStepRequest);
            case Step.CREATE_BENEFICIARY:
                return createBeneficiary(createBeneficiaryMultiStepRequest);
            default:
                throw new IllegalStateException(
                        String.format(
                                "Unknown step %s", createBeneficiaryMultiStepRequest.getStep()));
        }
    }

    private CreateBeneficiaryMultiStepResponse authorized(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryException, LoginException {
        IdentificationRoutingResponse identificationRoutingResponse =
                instanceStorage
                        .get(
                                StorageKeys.IDENTIFICATION_ROUTING_RESPONSE,
                                IdentificationRoutingResponse.class)
                        .orElseThrow(
                                () ->
                                        new BeneficiaryAuthorizationException(
                                                "IdentificationRoutingResponse missing from storage."));
        String samlTransactionId =
                apiClient.oAuth2AuthorizeRedirect(
                        identificationRoutingResponse.getUserCode(),
                        identificationRoutingResponse.getBankId(),
                        identificationRoutingResponse.getMembershipTypeValue(),
                        apiResponse.getIdTokenHint());
        String samlTransactionPath =
                Urls.SAML_TRANSACTION_PATH.concat("/").concat(samlTransactionId);
        SamlAuthnResponse samlAuthnResponse = apiClient.samlAuthorize(samlTransactionPath);
        samlAuthnResponse.throwBeneficiaryExceptionIfFailedAuthentication();
        String validationId =
                samlAuthnResponse
                        .getValidationId()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Not able to determine validation id."));
        String validationUnitId =
                samlAuthnResponse
                        .getValidationUnitId()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Not able to determine validation unit id."));
        SamlAuthnResponse otpResponse =
                apiClient.submitOtp(
                        validationId, validationUnitId, getBeneficiaryOtp(), samlTransactionPath);
        otpResponse.throwBeneficiaryExceptionIfFailedAuthentication();
        apiClient.oAuth2Consume(
                otpResponse
                        .getSaml2PostAction()
                        .orElseThrow(
                                () -> new IllegalStateException("SAML action URL is missing.")),
                otpResponse
                        .getSamlResponseValue()
                        .orElseThrow(() -> new IllegalStateException("SAML response missing.")));
        return new CreateBeneficiaryMultiStepResponse(
                createBeneficiaryMultiStepRequest, Step.CREATE_BENEFICIARY, new ArrayList<>());
    }

    private CreateBeneficiaryMultiStepResponse init(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryException {
        switch (createBeneficiaryMultiStepRequest.getBeneficiary().getStatus()) {
            case INITIATED:
                return new CreateBeneficiaryMultiStepResponse(
                        createBeneficiaryMultiStepRequest, Step.AUTHORIZE, new ArrayList<>());
            case CREATED:
                return new CreateBeneficiaryMultiStepResponse(
                        createBeneficiaryMultiStepRequest,
                        AuthenticationStepConstants.STEP_FINALIZE,
                        new ArrayList<>());
            case REJECTED:
                throw new BeneficiaryRejectedException();
            default:
                throw new IllegalStateException(
                        String.format(
                                "Unknown status %s",
                                createBeneficiaryMultiStepRequest.getBeneficiary().getStatus()));
        }
    }

    private CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryInvalidAccountTypeException {
        CaisseEpargneCreateBeneficiaryResponse response =
                apiClient.createBeneficiary(
                        CaisseEpargneCreateBeneficiaryRequest.of(createBeneficiaryMultiStepRequest)
                                .orElseThrow(
                                        () ->
                                                new BeneficiaryInvalidAccountTypeException(
                                                        Type.IBAN)));
        CreateBeneficiaryMultiStepResponse createBeneficiaryMultiStepResponse =
                new CreateBeneficiaryMultiStepResponse(
                        createBeneficiaryMultiStepRequest,
                        AuthenticationStepConstants.STEP_FINALIZE,
                        new ArrayList<>());
        if (response.isErrorResponse()) {
            createBeneficiaryMultiStepResponse
                    .getBeneficiary()
                    .setStatus(CreateBeneficiaryStatus.REJECTED);
            LOG.error("Beneficiary request was rejected: {}", response.getErrorCode());
        } else {
            createBeneficiaryMultiStepResponse
                    .getBeneficiary()
                    .setStatus(CreateBeneficiaryStatus.CREATED);
        }
        return createBeneficiaryMultiStepResponse;
    }

    private String getBeneficiaryOtp() throws BeneficiaryException {
        try {
            return supplementalInformationProvider
                    .getSupplementalInformationHelper()
                    .waitForOtpInput();
        } catch (SupplementalInfoException e) {
            throw new BeneficiaryException(e.getMessage(), e);
        }
    }
}
