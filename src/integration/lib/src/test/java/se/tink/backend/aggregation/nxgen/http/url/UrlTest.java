package se.tink.backend.aggregation.nxgen.http.url;

import com.sun.jersey.core.util.StringKeyIgnoreCaseMultivaluedMap;
import java.util.Arrays;
import java.util.HashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Assert;
import org.junit.Test;

public class UrlTest {
    private static final String BASE_URL = "https://www.tink.se";

    @Test
    public void ensureParameterCharsAreEscaped() {
        String url = new URL(BASE_URL + "/{.*}").parameter(".*", "value").get();
        Assert.assertEquals(BASE_URL + "/value", url);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenParameter_doesNotMatchUrlVariable() {
        new URL(BASE_URL + "/{param}").parameter("key", "value");
    }

    @Test
    public void ensureMultipleParameters_isAllowed() {
        String url =
                new URL(BASE_URL + "/{param1}/{param2}")
                        .parameter("param1", "value1")
                        .parameter("param2", "value2")
                        .get();

        Assert.assertEquals(BASE_URL + "/value1/value2", url);
    }

    @Test
    public void ensureParameterCanBeUsed_moreThanOnce() {
        String url =
                new URL(BASE_URL + "/{param1}/{param2}/{param1}")
                        .parameter("param1", "value1")
                        .parameter("param2", "value2")
                        .get();

        Assert.assertEquals(BASE_URL + "/value1/value2/value1", url);
    }

    @Test
    public void ensureEscapedParameters() {
        URL url =
                new URL(BASE_URL + "/{param}")
                        .parameter("param", "param&/")
                        .queryParam("key&", "val&");
        Assert.assertEquals(BASE_URL + "/param%26%2F?key%26=val%26", url.get());
    }

    @Test
    public void ensureQueryParams_withNullKey_isRemoved() {
        Assert.assertEquals(BASE_URL, new URL(BASE_URL).queryParam(null, "val").get());
    }

    @Test
    public void ensureQueryParams_withNullValue_isRemoved() {
        Assert.assertEquals(BASE_URL, new URL(BASE_URL).queryParam("key", null).get());
    }

    @Test
    public void ensureQueryParams_withEmptyKey_isRemoved() {
        Assert.assertEquals(BASE_URL, new URL(BASE_URL).queryParam("", "val").get());
    }

    @Test
    public void ensureQueryParams_withEmptyValue_isAdded() {
        Assert.assertEquals(BASE_URL + "?key=", new URL(BASE_URL).queryParam("key", "").get());
    }

    @Test
    public void ensurePredefinedQueryParams_areSeparated() {
        String rawUrl = BASE_URL + "?key1=val1";
        Assert.assertEquals(rawUrl, new URL(rawUrl).get());
        Assert.assertEquals(
                rawUrl + "&key2=val2", new URL(rawUrl).queryParam("key2", "val2").get());
    }

    @Test
    public void ensureMultipleQueryParams_areAllowed() {
        String rawUrl = BASE_URL + "?key1=val1&key2=val2&key3=val3";
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("key1", "val1");
        queryParams.put("key2", "val2");
        queryParams.put("key3", "val3");

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureMultipleQueryParams_withNullKeys_areRemoved() {
        String rawUrl = BASE_URL + "?key3=val3";
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put(null, "val2");
        queryParams.put("key3", "val3");

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureMultipleQueryParams_withNullValues_areRemoved() {
        String rawUrl = BASE_URL + "?key3=val3";
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("key1", null);
        queryParams.put("key2", null);
        queryParams.put("key3", "val3");

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureMultipleQueryParams_withEmptyKeys_areRemoved() {
        String rawUrl = BASE_URL + "?key1=val1&key3=val3";
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("key1", "val1");
        queryParams.put("", "val2");
        queryParams.put("key3", "val3");

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureMultipleQueryParams_withEmptyValues_areAdded() {
        String rawUrl = BASE_URL + "?key1=&key2=&key3=val3";
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("key1", "");
        queryParams.put("key2", "");
        queryParams.put("key3", "val3");

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureSingleMultiValuedQueryParams_areAllowed() {
        String rawUrl = BASE_URL + "?key1=val1&key2=val2&key3=val3";
        MultivaluedMap<String, String> queryParams = new StringKeyIgnoreCaseMultivaluedMap<>();
        queryParams.putSingle("key1", "val1");
        queryParams.putSingle("key2", "val2");
        queryParams.putSingle("key3", "val3");

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureSingleMultiValuedQueryParams_withNullKeys_areRemoved() {
        String rawUrl = BASE_URL + "?key3=val3";
        MultivaluedMap<String, String> queryParams = new StringKeyIgnoreCaseMultivaluedMap<>();
        queryParams.putSingle(null, "val1");
        queryParams.putSingle(null, "val2");
        queryParams.putSingle("key3", "val3");

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureSingleMultiValuedQueryParams_withNullValues_areRemoved() {
        String rawUrl = BASE_URL + "?key3=val3";
        MultivaluedMap<String, String> queryParams = new StringKeyIgnoreCaseMultivaluedMap<>();
        queryParams.putSingle("key1", null);
        queryParams.putSingle("key2", null);
        queryParams.putSingle("key3", "val3");

        String newUrl = new URL(BASE_URL).queryParams(queryParams).get();
        Assert.assertEquals(rawUrl, newUrl);
    }

    @Test
    public void ensureSingleMultiValuedQueryParams_withEmptyKeys_areRemoved() {
        String rawUrl = BASE_URL + "?key1=val1&key3=val3";
        MultivaluedMap<String, String> queryParams = new StringKeyIgnoreCaseMultivaluedMap<>();
        queryParams.putSingle("key1", "val1");
        queryParams.putSingle("", "val2");
        queryParams.putSingle("key3", "val3");

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureSingleMultiValuedQueryParams_withEmptyValues_areAdded() {
        String rawUrl = BASE_URL + "?key1=&key2=&key3=val3";
        MultivaluedMap<String, String> queryParams = new StringKeyIgnoreCaseMultivaluedMap<>();
        queryParams.putSingle("key1", "");
        queryParams.putSingle("key2", "");
        queryParams.putSingle("key3", "val3");

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureMultipleMultiValuedQueryParams_areAllowed() {
        String rawUrl = BASE_URL + "?key1=val1&key1=val2&key2=val1&key2=val2&key3=val1&key3=val2";
        MultivaluedMap<String, String> queryParams = new StringKeyIgnoreCaseMultivaluedMap<>();
        queryParams.put("key1", Arrays.asList("val1", "val2"));
        queryParams.put("key2", Arrays.asList("val1", "val2"));
        queryParams.put("key3", Arrays.asList("val1", "val2"));

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureMultipleMultiValuedQueryParams_withNullKeys_areRemoved() {
        String rawUrl = BASE_URL + "?key3=val1&key3=val2";
        MultivaluedMap<String, String> queryParams = new StringKeyIgnoreCaseMultivaluedMap<>();
        queryParams.put(null, Arrays.asList("val1", "val2"));
        queryParams.put(null, Arrays.asList("val1", "val2"));
        queryParams.put("key3", Arrays.asList("val1", "val2"));

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureMultipleMultiValuedQueryParams_withNullValues_areRemoved() {
        String rawUrl = BASE_URL + "?key2=val2&key3=val1&key3=val2";
        MultivaluedMap<String, String> queryParams = new StringKeyIgnoreCaseMultivaluedMap<>();
        queryParams.put("key1", Arrays.asList(null, null));
        queryParams.put("key2", Arrays.asList(null, "val2"));
        queryParams.put("key3", Arrays.asList("val1", "val2"));

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureMultipleMultiValuedQueryParams_withEmptyKeys_areRemoved() {
        String rawUrl = BASE_URL + "?key1=val1&key1=val2&key3=val1&key3=val2";
        MultivaluedMap<String, String> queryParams = new StringKeyIgnoreCaseMultivaluedMap<>();
        queryParams.put("key1", Arrays.asList("val1", "val2"));
        queryParams.put("", Arrays.asList("val1", "val2"));
        queryParams.put("key3", Arrays.asList("val1", "val2"));

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureMultipleMultiValuedQueryParams_withEmptyValues_areAdded() {
        String rawUrl = BASE_URL + "?key1=val1&key1=val2&key2=val1&key2=val2&key3=val1&key3=val2";
        MultivaluedMap<String, String> queryParams = new StringKeyIgnoreCaseMultivaluedMap<>();
        queryParams.put("key1", Arrays.asList("val1", "val2"));
        queryParams.put("key2", Arrays.asList("val1", "val2"));
        queryParams.put("key3", Arrays.asList("val1", "val2"));

        Assert.assertEquals(rawUrl, new URL(BASE_URL).queryParams(queryParams).get());
    }

    @Test
    public void ensureGet_doesNotThrowException_ifParametersHaveNotBeenPopulated() {
        new URL(BASE_URL + "/my/{param}").get();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureToUri_throwsException_ifParametersHaveNotBeenPopulated() {
        new URL(BASE_URL + "/my/{param}").toUri();
    }

    @Test
    public void ensureImmutability() {
        String rawPath = "/my/{param}";
        URL rawUrl = new URL(BASE_URL + rawPath);

        URL url = rawUrl.parameter("param", "path");
        Assert.assertEquals(BASE_URL + rawPath, rawUrl.get());
        Assert.assertEquals(BASE_URL + "/my/path", url.get());

        url = rawUrl.queryParam("key", "val");
        Assert.assertEquals(BASE_URL + rawPath, rawUrl.get());
        Assert.assertEquals(BASE_URL + rawPath + "?key=val", url.get());
    }

    @Test
    public void ensureNoParameters_isAllowed() {
        String url = new URL(BASE_URL).get();

        Assert.assertEquals(BASE_URL, url);
    }

    @Test
    public void shouldParseCorrectUrlWithNestedParametrizedUrlInQueryParam() {
        // given
        final String urlToParse =
                "https://openbanking.unicre.pt/applinks?loginURL=https://openbanking.unicre.pt/Login/Login?eA0I8VcRRNZwZ9Okfk_q6G2-LkIPEbNHfVT1ZG3noPvD6f_W_hAZ9C7JasXHDFWEeqcP9TggbWUW1uhBJru9NdRRimL6Bq8L_KRoeIFqr_b4nC5DycC_kl1QmrJAnf2ZeQUZJce2-uilTmQEgWCIJu-4q-4ADiIDarq5ySZWkYEYo6viwFIQpEBBJ3matPCY&redirectURL=https://main.production.cardiff.tink.se:8443/api/v1/credentials/third-party/callback?state=8cb781cc-6ce4-4098-8265-e9b59fdbfeed";

        // when
        URL result = new URL(urlToParse);

        // then
        Assert.assertEquals(urlToParse, result.toString());
    }
}
