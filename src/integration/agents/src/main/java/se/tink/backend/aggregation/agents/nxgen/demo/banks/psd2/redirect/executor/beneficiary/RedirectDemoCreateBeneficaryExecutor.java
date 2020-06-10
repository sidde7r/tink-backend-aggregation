package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.beneficiary;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.Step;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;

public class RedirectDemoCreateBeneficaryExecutor extends BaseCreateBeneficiaryExecutor {
    private Logger log = LoggerFactory.getLogger(RedirectDemoCreateBeneficaryExecutor.class);

    public RedirectDemoCreateBeneficaryExecutor(
            Credentials credentials,
            ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController,
            SupplementalInformationHelper supplementalInformationHelper) {
        super(credentials, thirdPartyAppAuthenticationController, supplementalInformationHelper);
    }

    @Override
    CreateBeneficiaryMultiStepResponse authorized(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        try {
            log.info("Signing beneficiary request.");
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
