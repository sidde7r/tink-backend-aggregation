package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.beneficiary;

import java.util.ArrayList;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.Step;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectDemoAgentUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.AddBeneficiaryExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.AddBeneficiaryRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.AddBeneficiaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.payment.enums.AddBeneficiaryStatus;

public class RedirectDemoAddBeneficaryExecutor implements AddBeneficiaryExecutor {

    private final ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController;
    private final Credentials credentials;
    private AddBeneficiaryResponse addBeneficiaryResponse;

    public RedirectDemoAddBeneficaryExecutor(
            Credentials credentials,
            ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController) {
        this.credentials = credentials;
        this.thirdPartyAppAuthenticationController = thirdPartyAppAuthenticationController;
    }

    @Override
    public AddBeneficiaryResponse createBeneficiary(AddBeneficiaryRequest addBeneficiaryRequest)
            throws PaymentException {
        try {
            thirdPartyAppAuthenticationController.authenticate(credentials);
        } catch (AuthenticationException | AuthorizationException e) {
            e.printStackTrace();
        }

        // Do not use the real PersistentStorage because we don't want to overwrite the
        // AIS auth token.
        PersistentStorage dummyStorage = new PersistentStorage();

        addBeneficiaryResponse =
                new AddBeneficiaryResponse(addBeneficiaryRequest.getBeneficiary(), dummyStorage);
        addBeneficiaryResponse.getBeneficiary().setStatus(AddBeneficiaryStatus.CREATED);
        return this.addBeneficiaryResponse;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse sign(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws PaymentException, AuthenticationException {
        switch (createBeneficiaryMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                return init(createBeneficiaryMultiStepRequest);
            case RedirectAuthenticationDemoAgentConstants.Step.AUTHORIZE:
                return authorized(createBeneficiaryMultiStepRequest);
            case RedirectAuthenticationDemoAgentConstants.Step.ADD_BENEFICIARY:
                return addBeneficiary(createBeneficiaryMultiStepRequest);
            default:
                throw new IllegalStateException(
                        String.format(
                                "Unknown step %s", createBeneficiaryMultiStepRequest.getStep()));
        }
    }

    private CreateBeneficiaryMultiStepResponse addBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        String providerName = credentials.getProviderName();
        // This block handles PIS only business use case as source-account will be null in request
        RedirectDemoAgentUtils.throwIfFailStateProvider(providerName);

        CreateBeneficiaryMultiStepResponse createBeneficiaryMultiStepResponse =
                new CreateBeneficiaryMultiStepResponse(
                        createBeneficiaryMultiStepRequest,
                        AuthenticationStepConstants.STEP_FINALIZE,
                        new ArrayList<>());
        createBeneficiaryMultiStepResponse.getBeneficiary().setStatus(AddBeneficiaryStatus.ADDED);
        return createBeneficiaryMultiStepResponse;
    }

    private CreateBeneficiaryMultiStepResponse init(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws PaymentAuthorizationException {

        switch (createBeneficiaryMultiStepRequest.getBeneficiary().getStatus()) {
            case CREATED:
                return new CreateBeneficiaryMultiStepResponse(
                        createBeneficiaryMultiStepRequest, Step.AUTHORIZE, new ArrayList<>());
            case REJECTED:
                throw new PaymentAuthorizationException(
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
            this.addBeneficiaryResponse.getBeneficiary().setStatus(AddBeneficiaryStatus.SIGNED);
            return new CreateBeneficiaryMultiStepResponse(
                    createBeneficiaryMultiStepRequest, Step.ADD_BENEFICIARY, new ArrayList<>());
        } catch (AuthenticationException | AuthorizationException e) {
            return new CreateBeneficiaryMultiStepResponse(
                    createBeneficiaryMultiStepRequest, Step.AUTHORIZE, new ArrayList<>());
        }
    }
}
