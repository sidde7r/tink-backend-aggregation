package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class BackoffProviderTest {

    private final BackOffProvider backoffProvider = new BackOffProvider(2000);

    @Test
    @Parameters({"0, 2000", "1, 4000", "2, 8000"})
    public void shouldCalculateWithBackOff(String retry, String result) {
        long backoff = backoffProvider.calculate(Integer.parseInt(retry));

        assertThat(backoff).isEqualTo(Long.parseLong(result));
    }
}
