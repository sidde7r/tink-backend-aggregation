package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class MockRandomValueGeneratorTest {

    private final RandomValueGenerator generator = new MockRandomValueGenerator();

    @Test
    @Parameters({"1", "10", "100"})
    public void shouldGenerateRandomAlphanumeric(int size) {

        // when
        String randomString = generator.generateRandomAlphanumeric(size);

        // then
        assertThat(chars(randomString)).hasSize(size).containsOnly('A');
    }

    @Test
    @Parameters
    public void shouldGenerateRandomAlphanumericBasedOnCustomAlphabet(int size, String alphabet) {

        // when
        String randomString = generator.generateRandomAlphanumeric(size, alphabet);

        // then
        assertThat(randomString).hasSize(size);

        // and
        assertThat(chars(alphabet)).contains(chars(randomString));
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldGenerateRandomAlphanumericBasedOnCustomAlphabet() {
        return new Object[] {
            new Object[] {
                1, "def-_",
            },
            new Object[] {
                10, "abc",
            },
            new Object[] {
                100, "abc-def01234",
            }
        };
    }

    private static char[] chars(String chars) {
        return chars.toCharArray();
    }
}
