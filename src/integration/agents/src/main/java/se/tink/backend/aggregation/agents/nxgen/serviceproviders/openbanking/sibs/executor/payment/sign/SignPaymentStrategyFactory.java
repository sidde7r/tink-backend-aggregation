package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign;

import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class SignPaymentStrategyFactory {

    private static final Long WAIT_FOR_RESPONSE_IN_SECONDS = 350L;

    private SignPaymentStrategyFactory() {}

    public static SignPaymentStrategy buildSignPaymentRedirectStrategy(
            SibsBaseApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper) {
        TimeUnit unit = TimeUnit.SECONDS;
        SibsRedirectCallbackHandler redirectCallbackHandler =
                new SibsRedirectCallbackHandler(
                        supplementalInformationHelper, WAIT_FOR_RESPONSE_IN_SECONDS, unit);
        return new SibsRedirectSignPaymentStrategy(apiClient, redirectCallbackHandler);
    }
}
