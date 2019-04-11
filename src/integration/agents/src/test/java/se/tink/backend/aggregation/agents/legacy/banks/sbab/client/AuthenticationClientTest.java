package se.tink.backend.aggregation.agents.banks.sbab.client;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class AuthenticationClientTest {

    /** The Bearer token is fetched from a script tag in the overview page. */
    @Test
    public void getBearerToken_ReturnsCorrectToken() throws Exception {
        String html =
                "<script type=\"text/javascript\">\n"
                        + "var SBAB = SBAB || {};\n"
                        + "SBAB.BearerToken = 'this is the bearer token';\n"
                        + "</script><script>var test = 'another variable which should not be fetched';</script>";

        Optional<String> token = AuthenticationClient.parseBearerToken(html);

        Assert.assertEquals("this is the bearer token", token.get());
    }
}
