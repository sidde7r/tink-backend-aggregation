package se.tink.backend.aggregation.agents.utils;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.signicat.SignicatParsingUtils;

public class SignicatParsingUtilsTest {

    @Test
    public void validServiceUrlShouldBeFound() {
        String htmlContents = "signicat.assumeRemoteDevice = false;\n"
                + "signicat.showSpinner = true;\n"
                + "signicat.serviceUrl = 'https://id.banknorwegian.se/std/method/banknorwegian.se/27b69985f22/';\n"
                + "signicat.target = decodeURIComponent('foo-bar');\n"
                + "signicat.wpUARegex = 'Windows Phone';";

        String serviceUrl = SignicatParsingUtils.parseBankIdServiceUrl(htmlContents);

        Assert.assertEquals(serviceUrl, "https://id.banknorwegian.se/std/method/banknorwegian.se/27b69985f22/");

    }
}
