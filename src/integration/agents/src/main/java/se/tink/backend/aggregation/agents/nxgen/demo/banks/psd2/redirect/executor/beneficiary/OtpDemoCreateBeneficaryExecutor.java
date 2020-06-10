package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.executor.beneficiary;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.Step;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;

public class OtpDemoCreateBeneficaryExecutor extends BaseCreateBeneficiaryExecutor {
    private Logger log = LoggerFactory.getLogger(OtpDemoCreateBeneficaryExecutor.class);

    public OtpDemoCreateBeneficaryExecutor(
            Credentials credentials,
            ThirdPartyAppAuthenticationController thirdPartyAppAuthenticationController,
            SupplementalInformationHelper supplementalInformationHelper) {
        super(credentials, thirdPartyAppAuthenticationController, supplementalInformationHelper);
    }

    @Override
    CreateBeneficiaryMultiStepResponse authorized(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        try {
            log.info("Waiting for otp.");
            supplementalInformationHelper.waitForOtpInput();
            log.info("Authorizing beneficiary request.");
            this.createBeneficiaryResponse
                    .getBeneficiary()
                    .setStatus(CreateBeneficiaryStatus.SIGNED);
            return new CreateBeneficiaryMultiStepResponse(
                    createBeneficiaryMultiStepRequest, Step.CREATE_BENEFICIARY, new ArrayList<>());
        } catch (SupplementalInfoException e) {
            return new CreateBeneficiaryMultiStepResponse(
                    createBeneficiaryMultiStepRequest, Step.AUTHORIZE, new ArrayList<>());
        }
    }
}
