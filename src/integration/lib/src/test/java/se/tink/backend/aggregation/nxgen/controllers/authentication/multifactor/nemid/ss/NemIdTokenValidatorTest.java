package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.asList;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NemIdErrorCodes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

@RunWith(JUnitParamsRunner.class)
public class NemIdTokenValidatorTest {

    private static final String SAMPLE_TOKEN_BASE_64 = "-- SAMPLE TOKEN --";

    private NemIdTokenParser tokenParser;
    private NemIdTokenValidator tokenValidator;

    @Before
    public void setup() {
        tokenParser = mock(NemIdTokenParser.class);
        tokenValidator = new NemIdTokenValidator(tokenParser);
    }

    @Test
    @Parameters(method = "validTokenStatuses")
    public void should_not_throw_on_success_token_status_code_regardless_of_status_message(
            NemIdTokenStatus validTokenStatus) {
        // given
        when(tokenParser.extractNemIdTokenStatus(SAMPLE_TOKEN_BASE_64))
                .thenReturn(validTokenStatus);

        // when
        tokenValidator.verifyTokenIsValid(SAMPLE_TOKEN_BASE_64);

        // then
        verify(tokenParser).extractNemIdTokenStatus(SAMPLE_TOKEN_BASE_64);
    }

    @SuppressWarnings("unused")
    private static Object[] validTokenStatuses() {
        return invalidTokenStatusWithExpectedException().stream()
                .map(
                        testCase ->
                                asList(
                                        setTestCaseTokenStatusCode(testCase, "success"),
                                        setTestCaseTokenStatusCode(
                                                testCase, "success".toUpperCase()),
                                        setTestCaseTokenStatusCode(
                                                testCase, "@#%!@111suCCess111DF@#")))
                .flatMap(List::stream)
                .map(TokenValidatorTestCase::getTokenStatus)
                .toArray(Object[]::new);
    }

    @Test
    @Parameters(method = "invalidTokenStatusesWithExpectedException")
    public void should_throw_correct_exception_on_invalid_token_status(
            TokenValidatorTestCase testCase) {
        // given
        when(tokenParser.extractNemIdTokenStatus(SAMPLE_TOKEN_BASE_64))
                .thenReturn(testCase.getTokenStatus());

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                tokenValidator.throwInvalidTokenExceptionWithoutValidation(
                                        SAMPLE_TOKEN_BASE_64));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, testCase.getExpectedException());
        verify(tokenParser).extractNemIdTokenStatus(SAMPLE_TOKEN_BASE_64);
    }

    @SuppressWarnings("unused")
    private static Object[] invalidTokenStatusesWithExpectedException() {
        return invalidTokenStatusWithExpectedException().stream()
                .map(
                        testCase ->
                                asList(
                                        setTestCaseTokenStatusCode(testCase, "sucess"),
                                        setTestCaseTokenStatusCode(testCase, "fail"),
                                        setTestCaseTokenStatusCode(testCase, "2512352")))
                .flatMap(List::stream)
                .toArray(Object[]::new);
    }

    @Test
    @Parameters(method = "tokenStatusWithExpectedExceptionToAlwaysThrow")
    public void should_throw_correct_exception_regardless_of_token_status_code(
            TokenValidatorTestCase testCase) {
        // given
        when(tokenParser.extractNemIdTokenStatus(SAMPLE_TOKEN_BASE_64))
                .thenReturn(testCase.getTokenStatus());

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                tokenValidator.throwInvalidTokenExceptionWithoutValidation(
                                        SAMPLE_TOKEN_BASE_64));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, testCase.getExpectedException());
        verify(tokenParser).extractNemIdTokenStatus(SAMPLE_TOKEN_BASE_64);
    }

    @SuppressWarnings("unused")
    private static Object[] tokenStatusWithExpectedExceptionToAlwaysThrow() {
        return invalidTokenStatusWithExpectedException().stream()
                .map(
                        testCase ->
                                asList(
                                        setTestCaseTokenStatusCode(testCase, "success"),
                                        setTestCaseTokenStatusCode(testCase, "fail"),
                                        setTestCaseTokenStatusCode(testCase, "sSDT#$T@#")))
                .flatMap(List::stream)
                .toArray(Object[]::new);
    }

    private static List<TokenValidatorTestCase> invalidTokenStatusWithExpectedException() {
        return Stream.of(
                        TokenValidatorTestCase.of(
                                NemIdTokenStatus.builder()
                                        .code("fail")
                                        .message(NemIdErrorCodes.REJECTED)
                                        .build(),
                                NemIdError.REJECTED.exception()),
                        TokenValidatorTestCase.of(
                                NemIdTokenStatus.builder()
                                        .code("fail")
                                        .message(NemIdErrorCodes.TIMEOUT)
                                        .build(),
                                NemIdError.TIMEOUT.exception()),
                        TokenValidatorTestCase.of(
                                NemIdTokenStatus.builder()
                                        .code("fail")
                                        .message("SOME_UNKNOWN123")
                                        .build(),
                                LoginError.CREDENTIALS_VERIFICATION_ERROR.exception()))
                .map(
                        testCase ->
                                asList(
                                        modifyTestCaseTokenStatusMessage(
                                                testCase, String::toUpperCase),
                                        modifyTestCaseTokenStatusMessage(
                                                testCase, String::toLowerCase),
                                        modifyTestCaseTokenStatusMessage(
                                                testCase,
                                                message -> "prefix" + message + "suffix")))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private static TokenValidatorTestCase setTestCaseTokenStatusCode(
            TokenValidatorTestCase testCase, String code) {
        return TokenValidatorTestCase.of(
                NemIdTokenStatus.builder()
                        .code(code)
                        .message(testCase.getTokenStatus().getMessage())
                        .build(),
                testCase.getExpectedException());
    }

    private static TokenValidatorTestCase modifyTestCaseTokenStatusMessage(
            TokenValidatorTestCase testCase, Function<String, String> messageModifier) {
        return TokenValidatorTestCase.of(
                NemIdTokenStatus.builder()
                        .code(testCase.getTokenStatus().getCode())
                        .message(messageModifier.apply(testCase.getTokenStatus().getMessage()))
                        .build(),
                testCase.getExpectedException());
    }

    @Data
    @Builder
    private static class TokenValidatorTestCase {
        private final NemIdTokenStatus tokenStatus;
        private final AgentException expectedException;

        static TokenValidatorTestCase of(
                NemIdTokenStatus tokenStatus, AgentException expectedException) {
            return TokenValidatorTestCase.builder()
                    .tokenStatus(tokenStatus)
                    .expectedException(expectedException)
                    .build();
        }
    }
}
