package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RunWith(Parameterized.class)
public class URLFormatterTest {
    private String httpVerb;
    private String urlAsString;
    private String formattedUrlAsString;

    public URLFormatterTest(String httpVerb, String urlAsString, String formattedUrlAsString) {
        this.httpVerb = httpVerb;
        this.urlAsString = urlAsString;
        this.formattedUrlAsString = formattedUrlAsString;
    }

    @Test
    public void shouldGetUrlFormattedProperly() {
        // when
        String formattedURL = URLFormatter.formatToString(httpVerb, new URL(urlAsString));

        // then
        assertEquals(formattedUrlAsString, formattedURL);
    }

    @Parameterized.Parameters(name = "{index}: Test with httpVerb={0}, url={1}, result: {2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                    {
                        "post",
                        "https://external-api.chebanca.io/authorize/foo/bar",
                        "post /authorize/foo/bar"
                    },
                    {
                        "get",
                        "https://external-api.chebanca.io/authorize/foo/bar",
                        "get /authorize/foo/bar"
                    },
                    {
                        "put",
                        "https://external-api.chebanca.io/authorize/foo/bar",
                        "put /authorize/foo/bar"
                    },
                    {
                        "post",
                        "https://external-api.chebanca.io/authorize?foo=bar",
                        "post /authorize?foo=bar"
                    },
                    {
                        "get",
                        "https://external-api.chebanca.io/authorize?foo=bar",
                        "get /authorize?foo=bar"
                    },
                    {
                        "put",
                        "https://external-api.chebanca.io/authorize?foo=bar",
                        "put /authorize?foo=bar"
                    },
                });
    }
}
