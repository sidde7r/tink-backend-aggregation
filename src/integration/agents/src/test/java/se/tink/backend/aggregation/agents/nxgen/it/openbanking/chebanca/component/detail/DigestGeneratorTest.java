package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.component.detail;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.DigestGenerator;

@RunWith(Parameterized.class)
public class DigestGeneratorTest {

    private String payload;
    private String expectedDigest;

    public DigestGeneratorTest(String payload, String expectedDigest) {
        this.payload = payload;
        this.expectedDigest = expectedDigest;
    }

    @Test
    public void testDigestCalculatedProperly() {
        // when
        String digest = DigestGenerator.generateDigest(payload);

        // then
        assertEquals(expectedDigest, digest);
    }

    @Parameterized.Parameters(name = "{index}: Test with payload={0}, result: {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                    {
                        "exemplary body to be signed",
                        "SHA-256=WiKGYz3sUxuDKbmaLEWL/HxmPAkaEnMYAuOehOL6lGY="
                    },
                    {
                        "{\n" + " \"data\": {\n" + "  \"customerid\": \"4450923\"\n" + " }",
                        "SHA-256=yZNtwgSR72V+IdbtoqLy+grRvR8igg3aZf6BuwEqKxA="
                    }
                });
    }
}
