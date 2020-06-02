package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.beneficiary;

import java.util.ArrayList;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
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
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;

public class RedirectDemoCreateBeneficaryExecutor implements CreateBeneficiaryExecutor {

    private final ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController;
    private final Credentials credentials;
    private CreateBeneficiaryResponse createBeneficiaryResponse;

    public RedirectDemoCreateBeneficaryExecutor(
            Credentials credentials,
            ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController) {
        this.credentials = credentials;
        this.thirdPartyAppAuthenticationController = thirdPartyAppAuthenticationController;
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
        createBeneficiaryResponse.getBeneficiary().setStatus(CreateBeneficiaryStatus.CREATED);
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
        String providerName = credentials.getProviderName();
        // This block handles PIS only business use case as source-account will be null in request
        RedirectDemoAgentUtils.throwIfFailStateProvider(providerName);

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

    private CreateBeneficiaryMultiStepResponse init(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryAuthorizationException {

        switch (createBeneficiaryMultiStepRequest.getBeneficiary().getStatus()) {
            case CREATED:
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

    private CreateBeneficiaryMultiStepResponse authorized(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        try {
            thirdPartyAppAuthenticationController.authenticate(credentials);
            this.createBeneficiaryResponse
                    .getBeneficiary()
                    .setStatus(CreateBeneficiaryStatus.SIGNED);
            return new CreateBeneficiaryMultiStepResponse(
                    createBeneficiaryMultiStepRequest, Step.CREATE_BENEFICIARY, new ArrayList<>());
        } catch (AuthenticationException | AuthorizationException e) {
            return new CreateBeneficiaryMultiStepResponse(
                    createBeneficiaryMultiStepRequest, Step.AUTHORIZE, new ArrayList<>());
        }
    }
}
