package se.tink.backend.aggregation.nxgen.http;

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
        String url = new URL(BASE_URL + "/{param1}/{param2}").parameter("param1", "value1")
                .parameter("param2", "value2").get();

        Assert.assertEquals(BASE_URL + "/value1/value2", url);
    }

    @Test
    public void ensureParameterCanBeUsed_moreThanOnce() {
        String url = new URL(BASE_URL + "/{param1}/{param2}/{param1}").parameter("param1", "value1")
                .parameter("param2", "value2").get();

        Assert.assertEquals(BASE_URL + "/value1/value2/value1", url);
    }

    @Test
    public void ensureEscapedParameters() {
        URL url = new URL(BASE_URL + "/{param}")
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
        Assert.assertEquals(rawUrl + "&key2=val2", new URL(rawUrl).queryParam("key2", "val2").get());
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
}
