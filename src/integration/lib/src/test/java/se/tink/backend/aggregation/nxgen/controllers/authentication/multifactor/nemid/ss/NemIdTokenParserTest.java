package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.asArray;

import java.util.ArrayList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

@RunWith(JUnitParamsRunner.class)
public class NemIdTokenParserTest {

    private final NemIdTokenParser parser = new NemIdTokenParser();

    @Test
    @Parameters(method = "tokenBase64WithExpectedTokenStatus")
    public void should_parse_base_64_token_string(
            String tokenBase64, NemIdTokenStatus expectedStatus) {
        // when
        NemIdTokenStatus tokenStatus = parser.extractNemIdTokenStatus(tokenBase64);

        // then
        assertThat(tokenStatus).isEqualTo(expectedStatus);
    }

    @SuppressWarnings("unused")
    private static Object[] tokenBase64WithExpectedTokenStatus() {
        List<Object[]> args = new ArrayList<>();

        args.add(asArray(toBase64(""), NemIdTokenStatus.builder().code("").message("").build()));

        args.add(
                asArray(
                        toBase64("<irrelevant></irrelevant>"),
                        NemIdTokenStatus.builder().code("").message("").build()));

        args.add(
                asArray(
                        toBase64(
                                "<irrelevant123>"
                                        + "<sp:StatusMessage>"
                                        + " AUTH007     "
                                        + "</sp:StatusMessage>"
                                        + "</irrelevant123>"),
                        NemIdTokenStatus.builder().code("").message("AUTH007").build()));

        args.add(
                asArray(
                        toBase64(
                                "<ir relevant123>"
                                        + "<whatever>"
                                        + "<sp:StatusMessage>"
                                        + " AUTH007     "
                                        + "</sp:StatusMessage>"
                                        + "</whatever>"
                                        + "</irrelevant123>"),
                        NemIdTokenStatus.builder().code("").message("AUTH007").build()));

        args.add(
                asArray(
                        toBase64(
                                "<irrelevant>"
                                        + "<sp:StatusCode>"
                                        + "</sp:StatusCode>"
                                        + "<sp:StatusMessage>"
                                        + " AUTH007     "
                                        + "</sp:StatusMessage>"
                                        + "</irrelevant>"),
                        NemIdTokenStatus.builder().code("").message("AUTH007").build()));

        args.add(
                asArray(
                        toBase64(
                                "<irrelevant>"
                                        + "<sp:StatusCode Value=\"123success123\">"
                                        + "</sp:StatusCode>"
                                        + "<sp:StatusMessage>"
                                        + " AUTH008     "
                                        + "</sp:StatusMessage>"
                                        + "</irrelevant>"),
                        NemIdTokenStatus.builder()
                                .code("123success123")
                                .message("AUTH008")
                                .build()));

        args.add(
                asArray(
                        toBase64(
                                "<irrelevant>"
                                        + "<whatever>"
                                        + "<sp:StatusCode Value=\"123success123\">"
                                        + "</sp:StatusCode>"
                                        + "</whatever>"
                                        + "<sp:StatusMessage>"
                                        + " AUTH008     "
                                        + "</sp:StatusMessage>"
                                        + "</irrelevant>"),
                        NemIdTokenStatus.builder()
                                .code("123success123")
                                .message("AUTH008")
                                .build()));

        args.add(
                asArray(
                        toBase64(
                                "<irrelevant>"
                                        + "<sp:StatusCode Value=\"123success123\">"
                                        + "</sp:StatusCode>"
                                        + "<sp:StatusMessage>"
                                        + "      "
                                        + "</sp:StatusMessage>"
                                        + "</irrelevant>"),
                        NemIdTokenStatus.builder().code("123success123").message("").build()));

        args.add(
                asArray(
                        toBase64(
                                "<irrelevant>"
                                        + "<sp:StatusCode Value=\"123success123\">"
                                        + "</sp:StatusCode>"
                                        + "</irrelevant>"),
                        NemIdTokenStatus.builder().code("123success123").message("").build()));

        return args.toArray(new Object[0]);
    }

    private static String toBase64(String value) {
        return EncodingUtils.encodeAsBase64String(value);
    }
}
