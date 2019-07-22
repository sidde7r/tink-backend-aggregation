package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign;

import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class SignPaymentStrategyFactory {

    private SignPaymentStrategyFactory() {}

    public static SignPaymentStrategy buildSignPaymentRedirectStrategy(
            SibsBaseApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper) {
        long waitFor = 60;
        TimeUnit unit = TimeUnit.SECONDS;
        SibsRedirectCallbackHandler redirectCallbackHandler =
                new SibsRedirectCallbackHandler(supplementalInformationHelper, waitFor, unit);
        SibsRedirectSignPaymentStrategy redirectSignPaymentStrategy =
                new SibsRedirectSignPaymentStrategy(apiClient, redirectCallbackHandler);
        return redirectSignPaymentStrategy;
    }

    public static SignPaymentStrategy buildSignPaymentDecoupledStrategy(
            SibsBaseApiClient apiClient, Credentials credentials) {
        SibsDecoupledSignPaymentStrategy redirectSignPaymentStrategy =
                new SibsDecoupledSignPaymentStrategy(apiClient, credentials);
        return redirectSignPaymentStrategy;
    }
}
