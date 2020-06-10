package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.beneficiary;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryException;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.Step;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectDemoAgentUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
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

public abstract class BaseCreateBeneficiaryExecutor implements CreateBeneficiaryExecutor {
    private Logger log = LoggerFactory.getLogger(BaseCreateBeneficiaryExecutor.class);

    final ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController;
    final Credentials credentials;
    CreateBeneficiaryResponse createBeneficiaryResponse;
    final SupplementalInformationHelper supplementalInformationHelper;

    BaseCreateBeneficiaryExecutor(
            Credentials credentials,
            ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.credentials = credentials;
        this.thirdPartyAppAuthenticationController = thirdPartyAppAuthenticationController;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public CreateBeneficiaryResponse createBeneficiary(
            CreateBeneficiaryRequest createBeneficiaryRequest) {
        // Do not use the real PersistentStorage because we don't want to overwrite the
        // AIS auth token.
        PersistentStorage dummyStorage = new PersistentStorage();
        createBeneficiaryResponse =
                new CreateBeneficiaryResponse(
                        createBeneficiaryRequest.getBeneficiary(), dummyStorage);
        createBeneficiaryResponse.getBeneficiary().setStatus(CreateBeneficiaryStatus.INITIATED);
        return this.createBeneficiaryResponse;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse sign(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryException, AuthenticationException {
        switch (createBeneficiaryMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                return init(createBeneficiaryMultiStepRequest);
            case RedirectAuthenticationDemoAgentConstants.Step.AUTHORIZE:
                return authorized(createBeneficiaryMultiStepRequest);
            case RedirectAuthenticationDemoAgentConstants.Step.CREATE_BENEFICIARY:
                return createBeneficiary(createBeneficiaryMultiStepRequest);
            default:
                throw new IllegalStateException(
                        String.format(
                                "Unknown step %s", createBeneficiaryMultiStepRequest.getStep()));
        }
    }

    private CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        log.info("Creating beneficiary");
        String providerName = credentials.getProviderName();
        // This block handles PIS only business use case as source-account will be null in request
        RedirectDemoAgentUtils.throwIfFailStateProvider(providerName);

        CreateBeneficiaryMultiStepResponse createBeneficiaryMultiStepResponse =
                new CreateBeneficiaryMultiStepResponse(
                        createBeneficiaryMultiStepRequest,
                        AuthenticationStepConstants.STEP_FINALIZE,
                        new ArrayList<>());
        Beneficiary beneficiary =
                createBeneficiaryMultiStepResponse.getBeneficiary().getBeneficiary();
        createBeneficiaryMultiStepResponse
                .getBeneficiary()
                .setStatus(CreateBeneficiaryStatus.CREATED);
        log.info(
                "Done with beneficiary creation, name: {}, type: {}, account number: {}, owner account number: {}",
                beneficiary.getName(),
                beneficiary.getAccountNumberType(),
                StringUtils.overlay(
                        beneficiary.getAccountNumber(),
                        StringUtils.repeat(
                                '*',
                                beneficiary.getAccountNumber().length()
                                        - (beneficiary.getAccountNumber().length() > 4 ? 4 : 0)),
                        0,
                        beneficiary.getAccountNumber().length()
                                - (beneficiary.getAccountNumber().length() > 4 ? 4 : 0)),
                StringUtils.overlay(
                        createBeneficiaryMultiStepResponse.getBeneficiary().getOwnerAccountNumber(),
                        StringUtils.repeat(
                                '*',
                                createBeneficiaryMultiStepResponse
                                                .getBeneficiary()
                                                .getOwnerAccountNumber()
                                                .length()
                                        - -(beneficiary.getAccountNumber().length() > 4 ? 4 : 0)),
                        0,
                        beneficiary.getAccountNumber().length()
                                - (beneficiary.getAccountNumber().length() > 4 ? 4 : 0)));
        return createBeneficiaryMultiStepResponse;
    }

    private CreateBeneficiaryMultiStepResponse init(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryAuthorizationException {
        switch (createBeneficiaryMultiStepRequest.getBeneficiary().getStatus()) {
            case INITIATED:
                return new CreateBeneficiaryMultiStepResponse(
                        createBeneficiaryMultiStepRequest, Step.AUTHORIZE, new ArrayList<>());
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

    abstract CreateBeneficiaryMultiStepResponse authorized(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest);
}
