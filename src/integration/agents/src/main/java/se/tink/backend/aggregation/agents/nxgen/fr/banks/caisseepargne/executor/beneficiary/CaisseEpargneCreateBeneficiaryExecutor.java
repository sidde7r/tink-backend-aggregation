package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary;

import java.util.ArrayList;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryInvalidAccountTypeException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryRejectedException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.Step;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.IdentificationRoutingResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.rpc.CaisseEpargneCreateBeneficiaryRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.rpc.CaisseEpargneCreateBeneficiaryResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.storage.CaisseEpargneStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.AuthTransactionResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.PhaseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.entities.AuthResponseStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.entities.MembershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.BpceValidationHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;

@Slf4j
@RequiredArgsConstructor
public class CaisseEpargneCreateBeneficiaryExecutor implements CreateBeneficiaryExecutor {

    private final CaisseEpargneApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private CaisseEpargneCreateBeneficiaryResponse apiResponse;
    private final CaisseEpargneStorage caisseEpargneStorage;
    private final BpceValidationHelper validationHelper;

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
                                                        AccountIdentifierType.IBAN)));

        CreateBeneficiaryResponse createBeneficiaryResponse =
                new CreateBeneficiaryResponse(createBeneficiaryRequest.getBeneficiary());
        if (apiResponse.isNeedExtendedAuthenticationError()) {
            createBeneficiaryResponse.getBeneficiary().setStatus(CreateBeneficiaryStatus.INITIATED);
        } else if (apiResponse.isErrorResponse()) {
            createBeneficiaryResponse.getBeneficiary().setStatus(CreateBeneficiaryStatus.REJECTED);
            log.error("Beneficiary request was rejected: {}", apiResponse.getErrorCode());
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
                caisseEpargneStorage.getIdRoutingResponse();

        MembershipType membershipType =
                MembershipType.fromString(identificationRoutingResponse.getMembershipTypeCode());

        String samlTransactionId =
                apiClient.oAuth2AuthorizeRedirect(
                        identificationRoutingResponse.getUserCode(),
                        identificationRoutingResponse.getBankId(),
                        membershipType,
                        apiResponse.getIdTokenHint());
        String samlTransactionPath =
                Urls.SAML_TRANSACTION_PATH.concat("/").concat(samlTransactionId);
        AuthTransactionResponseDto authTransactionResponseDto =
                apiClient.getAuthTransaction(samlTransactionPath);

        validateAuthTransactionResponseDto(authTransactionResponseDto);

        String validationId = validationHelper.getValidationId(authTransactionResponseDto);

        String validationUnitId =
                validationHelper.getValidationUnitId(authTransactionResponseDto, validationId);

        AuthTransactionResponseDto otpResponse =
                apiClient.sendOtp(
                        validationId, validationUnitId, samlTransactionPath, getBeneficiaryOtp());

        validateAuthTransactionResponseDto(otpResponse);

        apiClient.oAuth2Consume(otpResponse.getResponse().getSaml2Post());

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
                                                        AccountIdentifierType.IBAN)));
        CreateBeneficiaryMultiStepResponse createBeneficiaryMultiStepResponse =
                new CreateBeneficiaryMultiStepResponse(
                        createBeneficiaryMultiStepRequest,
                        AuthenticationStepConstants.STEP_FINALIZE,
                        new ArrayList<>());
        if (response.isErrorResponse()) {
            createBeneficiaryMultiStepResponse
                    .getBeneficiary()
                    .setStatus(CreateBeneficiaryStatus.REJECTED);
            log.error("Beneficiary request was rejected: {}", response.getErrorCode());
        } else {
            createBeneficiaryMultiStepResponse
                    .getBeneficiary()
                    .setStatus(CreateBeneficiaryStatus.CREATED);
        }
        return createBeneficiaryMultiStepResponse;
    }

    private String getBeneficiaryOtp() throws BeneficiaryException {
        try {
            return supplementalInformationHelper.waitForOtpInput();
        } catch (SupplementalInfoException e) {
            throw new BeneficiaryException(e.getMessage(), e);
        }
    }

    private static void validateAuthTransactionResponseDto(
            AuthTransactionResponseDto authTransactionResponseDto)
            throws BeneficiaryAuthorizationException {

        final PhaseDto phaseDto = authTransactionResponseDto.getPhase();

        if (Objects.isNull(phaseDto)) {
            return;
        }

        if (AuthResponseStatus.FAILED_AUTHENTICATION
                        .getName()
                        .equalsIgnoreCase(phaseDto.getPreviousResult())
                && phaseDto.getRetryCounter() == 1) {
            throw LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT.exception();
        } else if (AuthResponseStatus.FAILED_AUTHENTICATION
                .getName()
                .equalsIgnoreCase(phaseDto.getPreviousResult())) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        if (Objects.isNull(authTransactionResponseDto.getResponse())
                || Objects.isNull(authTransactionResponseDto.getResponse().getStatus())) {
            return;
        }

        final String status = authTransactionResponseDto.getResponse().getStatus();

        if (!AuthResponseStatus.AUTHENTICATION_SUCCESS.getName().equalsIgnoreCase(status)) {
            log.error("Authentication failed with status: " + status);

            if (AuthResponseStatus.AUTHENTICATION_LOCKED.getName().equalsIgnoreCase(status)) {
                throw new BeneficiaryAuthorizationException(
                        AuthorizationError.ACCOUNT_BLOCKED.userMessage().toString());
            } else if (AuthResponseStatus.AUTHENTICATION_FAILED
                    .getName()
                    .equalsIgnoreCase(status)) {
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
            } else {
                throw new BeneficiaryAuthorizationException(
                        AuthorizationError.UNAUTHORIZED.userMessage().toString());
            }
        }
    }
}
