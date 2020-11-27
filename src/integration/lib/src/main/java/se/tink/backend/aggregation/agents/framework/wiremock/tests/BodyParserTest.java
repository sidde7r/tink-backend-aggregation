package se.tink.backend.aggregation.agents.framework.wiremock.tests;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.google.common.collect.ImmutableList;
import javax.ws.rs.core.MediaType;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.parsing.BodyParser;
import se.tink.backend.aggregation.agents.framework.wiremock.parsing.BodyParserImpl;

public final class BodyParserTest {

    @Test
    public void canParseURLEncodedForm() {

        final BodyParser bodyParser = new BodyParserImpl();
        final String formString =
                "scope=accounts&redirect_uri=https%3A%2F%2Fapi.tink.se%2Fapi%2Fv1%2Fcredentials%2Fthird-party%2Fcallback";
        final ImmutableList<StringValuePattern> expectedResult =
                ImmutableList.<StringValuePattern>builder()
                        .add(
                                WireMock.containing(
                                        "redirect_uri=https%3A%2F%2Fapi.tink.se%2Fapi%2Fv1%2Fcredentials%2Fthird-party%2Fcallback"))
                        .add(WireMock.containing("scope=accounts"))
                        .build();

        ImmutableList<StringValuePattern> patternResultList =
                bodyParser.getStringValuePatterns(
                        formString, MediaType.APPLICATION_FORM_URLENCODED);

        Assert.assertThat(
                patternResultList,
                IsIterableContainingInAnyOrder.containsInAnyOrder(expectedResult.toArray()));
    }

    @Test
    public void canParseJson() {

        final BodyParser bodyParser = new BodyParserImpl();
        final String jsonString = "{ \"key\" : \"value\", \"someArr\" : \"[1,2,3]\" }";
        final ImmutableList<StringValuePattern> expectedResult =
                ImmutableList.<StringValuePattern>builder()
                        .add(
                                WireMock.equalToJson(
                                        "{ \"key\" : \"value\", \"someArr\" : \"[1,2,3]\" }"))
                        .build();

        ImmutableList<StringValuePattern> patternResultList =
                bodyParser.getStringValuePatterns(jsonString, MediaType.APPLICATION_JSON);

        // Expected rules are returned
        Assert.assertThat(
                patternResultList,
                IsIterableContainingInAnyOrder.containsInAnyOrder(expectedResult.toArray()));

        // Rules are agnostic towards ordering (not covered by equals above)
        patternResultList.forEach(pattern -> Assert.assertTrue(ignoreOrderAndExtras(pattern)));
    }

    @Test
    public void canParsePlainText() {

        final BodyParser bodyParser = new BodyParserImpl();
        final String text = "ping";
        final ImmutableList<StringValuePattern> expectedResult =
                ImmutableList.<StringValuePattern>builder().add(WireMock.equalTo("ping")).build();

        ImmutableList<StringValuePattern> patternResultList =
                bodyParser.getStringValuePatterns(text, MediaType.TEXT_PLAIN);

        Assert.assertThat(
                patternResultList,
                IsIterableContainingInAnyOrder.containsInAnyOrder(expectedResult.toArray()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unsupportedParsingType_ThrowException() {

        final BodyParser bodyParser = new BodyParserImpl();
        final String aPerfectlyValidBody = "{}";
        final String unsupportedMediaType = "not-a-media-type";

        bodyParser.getStringValuePatterns(aPerfectlyValidBody, unsupportedMediaType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidJson_ThrowException() {

        final BodyParser bodyParser = new BodyParserImpl();
        final String aPerfectlyValidBody = "not-json";

        bodyParser.getStringValuePatterns(aPerfectlyValidBody, MediaType.APPLICATION_JSON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidForm_emptyKey_ThrowException() {

        final BodyParser bodyParser = new BodyParserImpl();
        final String aPerfectlyValidBody = "=not-form";

        bodyParser.getStringValuePatterns(
                aPerfectlyValidBody, MediaType.APPLICATION_FORM_URLENCODED);
    }

    @Test
    public void correctForm_emptyValue_ThrowException() {

        final BodyParser bodyParser = new BodyParserImpl();
        final String aPerfectlyValidBody = "not-form";

        bodyParser.getStringValuePatterns(
                aPerfectlyValidBody, MediaType.APPLICATION_FORM_URLENCODED);
    }

    private boolean ignoreOrderAndExtras(final StringValuePattern pattern) {

        if (pattern instanceof EqualToJsonPattern) {
            EqualToJsonPattern jsonPattern = (EqualToJsonPattern) pattern;
            return jsonPattern.isIgnoreArrayOrder() && jsonPattern.isIgnoreExtraElements();
        }
        return false;
    }
}
