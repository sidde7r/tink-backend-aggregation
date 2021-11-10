package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

@Slf4j
public class SignPaymentStrategyFactory {

    private static final Long WAIT_FOR_RESPONSE_IN_SECONDS = 350L;

    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 59;

    private SignPaymentStrategyFactory() {}

    public static SignPaymentStrategy buildSignPaymentRedirectStrategy(
            SibsBaseApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper) {
        log.info(
                "Start to Get Payment Status every {} Seconds for a total of {} times.",
                SLEEP_TIME,
                RETRY_ATTEMPTS);
        TimeUnit unit = TimeUnit.SECONDS;
        SibsRedirectCallbackHandler redirectCallbackHandler =
                new SibsRedirectCallbackHandler(
                        supplementalInformationHelper, WAIT_FOR_RESPONSE_IN_SECONDS, unit);
        return new SibsRedirectSignPaymentStrategy(
                apiClient,
                redirectCallbackHandler,
                SibsUtils.getPaymentStatusRetryer(SLEEP_TIME, RETRY_ATTEMPTS));
    }
}
