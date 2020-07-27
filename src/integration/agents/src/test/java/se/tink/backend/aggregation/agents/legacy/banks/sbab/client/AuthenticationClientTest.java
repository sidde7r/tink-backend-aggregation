package se.tink.backend.aggregation.agents.banks.sbab.client;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;

public class AuthenticationClientTest {
    private static String TEST_DATA_PATH = "data/test/agents/sbab/";

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

    @Test
    public void hasKYCPopup_findsKYC() throws Exception {
        String html =
                new String(
                        Files.readAllBytes(
                                Paths.get(TEST_DATA_PATH, "overview-with-kyc-popup.html")),
                        StandardCharsets.UTF_8);
        Document document = Jsoup.parse(html);

        assertTrue(AuthenticationClient.hasKYCPopup(document));
    }
}
