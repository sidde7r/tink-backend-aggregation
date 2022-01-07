package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback.CmcicCallbackStatus.ERROR;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback.CmcicCallbackStatus.MULTIPLE_MATCH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback.CmcicCallbackStatus.SUCCESS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback.CmcicCallbackStatus.UNKNOWN;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CmcicCallbackStatusTest {

    private final Set<String> parameters;
    private final CmcicCallbackStatus expectedStatus;

    @Parameters(name = "Should return status {1} with parameters: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                    {ImmutableSet.of("code", "state"), SUCCESS},
                    {ImmutableSet.of("error", "error_description", "state"), ERROR},
                    {
                        ImmutableSet.of("code", "state", "error", "error_description", "state"),
                        MULTIPLE_MATCH
                    },
                    {ImmutableSet.of("error_uri"), UNKNOWN},
                    {ImmutableSet.of("state", "error"), UNKNOWN},
                    {ImmutableSet.of(""), UNKNOWN}
                });
    }

    public CmcicCallbackStatusTest(Set<String> parameters, CmcicCallbackStatus expectedStatus) {
        this.parameters = parameters;
        this.expectedStatus = expectedStatus;
    }

    @Test
    public void shouldReturnCorrectCallbackStatus() {
        CmcicCallbackStatus callbackStatus = CmcicCallbackStatus.extract(parameters);
        assertThat(callbackStatus).isEqualTo(expectedStatus);
    }
}
