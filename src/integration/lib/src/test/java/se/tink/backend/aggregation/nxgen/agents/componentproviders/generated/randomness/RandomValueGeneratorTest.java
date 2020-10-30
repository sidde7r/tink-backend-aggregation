package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class RandomValueGeneratorTest {

    @Test
    @Parameters(method = "parametersToTestRandom")
    public void generateRandomAlphanumeric(
            RandomValueGenerator randomValueGenerator, int size, String pattern) {
        // when
        String randomString = randomValueGenerator.generateRandomAlphanumeric(size);

        // then
        assertThat(randomString).hasSize(size).matches(pattern);
    }

    private Object[] parametersToTestRandom() {
        return new Object[] {
            new Object[] {
                new RandomValueGeneratorImpl(), 10, "[A-Za-z0-9]+",
            },
            new Object[] {
                new MockRandomValueGenerator(), 20, "A+",
            }
        };
    }
}
