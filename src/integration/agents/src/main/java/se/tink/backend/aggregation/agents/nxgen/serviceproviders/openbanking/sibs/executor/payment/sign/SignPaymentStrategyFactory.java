package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign;

import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;

public class SignPaymentStrategyFactory {

    private static final Long WAIT_FOR_RESPONSE_IN_SECONDS = 600L;

    private SignPaymentStrategyFactory() {}

    public static SignPaymentStrategy buildSignPaymentRedirectStrategy(
            SibsBaseApiClient apiClient, SupplementalRequester supplementalRequester) {
        TimeUnit unit = TimeUnit.SECONDS;
        SibsRedirectCallbackHandler redirectCallbackHandler =
                new SibsRedirectCallbackHandler(
                        supplementalRequester, WAIT_FOR_RESPONSE_IN_SECONDS, unit);
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
