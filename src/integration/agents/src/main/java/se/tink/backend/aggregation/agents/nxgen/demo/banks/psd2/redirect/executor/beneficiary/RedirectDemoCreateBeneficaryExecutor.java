package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.beneficiary;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import se.tink.libraries.payment.rpc.Beneficiary;

public class RedirectDemoCreateBeneficaryExecutor implements CreateBeneficiaryExecutor {
    private Logger log = LoggerFactory.getLogger(RedirectDemoCreateBeneficaryExecutor.class);

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

        log.info("Creating beneficiary step");
        createBeneficiaryResponse =
                new CreateBeneficiaryResponse(
                        createBeneficiaryRequest.getBeneficiary(), dummyStorage);
        createBeneficiaryResponse.getBeneficiary().setStatus(CreateBeneficiaryStatus.INITIATED);
        log.info("Done with Creating beneficiary step");
        return this.createBeneficiaryResponse;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse sign(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryException, AuthenticationException {
        log.info("Signing beneficiary step");
        switch (createBeneficiaryMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                log.info("Signing init");
                return init(createBeneficiaryMultiStepRequest);
            case RedirectAuthenticationDemoAgentConstants.Step.AUTHORIZE:
                log.info("Signing authorize");
                return authorized(createBeneficiaryMultiStepRequest);
            case RedirectAuthenticationDemoAgentConstants.Step.CREATE_BENEFICIARY:
                log.info("Signing create");
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
                "Done with adding beneficiary, name: {}, type: {}, account number: {}, owner account number: {}",
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
        log.info("Init step");
        switch (createBeneficiaryMultiStepRequest.getBeneficiary().getStatus()) {
            case INITIATED:
                log.info("Init step created");
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
            log.info("Authorize step");
            thirdPartyAppAuthenticationController.authenticate(credentials);
            this.createBeneficiaryResponse
                    .getBeneficiary()
                    .setStatus(CreateBeneficiaryStatus.SIGNED);
            log.info("Done authorizing");
            return new CreateBeneficiaryMultiStepResponse(
                    createBeneficiaryMultiStepRequest, Step.CREATE_BENEFICIARY, new ArrayList<>());
        } catch (AuthenticationException | AuthorizationException e) {
            return new CreateBeneficiaryMultiStepResponse(
                    createBeneficiaryMultiStepRequest, Step.AUTHORIZE, new ArrayList<>());
        }
    }
}
