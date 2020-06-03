package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.rpc.IbanValidationResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;
import se.tink.libraries.payment.rpc.Beneficiary;

public class CreditAgricoleAddBeneficiaryExecutor implements CreateBeneficiaryExecutor {
    private final CreditAgricoleApiClient apiClient;
    private final SupplementalInformationProvider supplementalInformationProvider;
    private CreateBeneficiaryResponse createBeneficiaryResponse;

    public CreditAgricoleAddBeneficiaryExecutor(
            CreditAgricoleApiClient apiClient,
            SupplementalInformationProvider supplementalInformationProvider) {
        this.apiClient = apiClient;
        this.supplementalInformationProvider = supplementalInformationProvider;
    }

    @Override
    public CreateBeneficiaryResponse createBeneficiary(
            CreateBeneficiaryRequest createBeneficiaryRequest) throws BeneficiaryException {
        createBeneficiaryResponse =
                new CreateBeneficiaryResponse(createBeneficiaryRequest.getBeneficiary());
        createBeneficiaryResponse.getBeneficiary().setStatus(CreateBeneficiaryStatus.CREATED);

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
            case CREATED:
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

        //        Getting number grid (France is sending numbers in random order) [170]
        //        Sending password based on grid [175]

        apiClient.otpInit();
        String otp;
        try {
            otp =
                    supplementalInformationProvider
                            .getSupplementalInformationHelper()
                            .waitForOtpInput();
        } catch (SupplementalInfoException e) {
            throw new BeneficiaryException(e.getMessage(), e);
        }
        try {
            apiClient.otpAuthenticate(otp);

            this.createBeneficiaryResponse
                    .getBeneficiary()
                    .setStatus(CreateBeneficiaryStatus.SIGNED);
            return new CreateBeneficiaryMultiStepResponse(
                    createBeneficiaryMultiStepRequest,
                    CreditAgricoleConstants.Step.ADD_BENEFICIARY,
                    new ArrayList<>());
        } catch (SupplementalInfoException e) {
            return new CreateBeneficiaryMultiStepResponse(
                    createBeneficiaryMultiStepRequest,
                    CreditAgricoleConstants.Step.AUTHORIZE,
                    new ArrayList<>());
        }
    }

    private CreateBeneficiaryMultiStepResponse addBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryException {
        Beneficiary beneficiary =
                createBeneficiaryMultiStepRequest.getBeneficiary().getBeneficiary();
        IbanValidationResponse ibanValidationResponse =
                apiClient.validateIban(beneficiary.getAccountNumber());
        try {
            apiClient.addBeneficiary(
                    beneficiary.getName(),
                    beneficiary.getAccountNumber(),
                    ibanValidationResponse.getBic());
        } catch (IllegalStateException ise) {
            throw new BeneficiaryException("addBeneficiary failed", ise);
        }

        CreateBeneficiaryMultiStepResponse createBeneficiaryMultiStepResponse =
                new CreateBeneficiaryMultiStepResponse(
                        createBeneficiaryMultiStepRequest,
                        AuthenticationStepConstants.STEP_FINALIZE,
                        new ArrayList<>());

        createBeneficiaryMultiStepResponse
                .getBeneficiary()
                .setStatus(CreateBeneficiaryStatus.ADDED);
        return createBeneficiaryMultiStepResponse;
    }
}
